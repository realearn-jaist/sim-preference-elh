package io.githib.xlives.batch.owl.topdown.sim;

import io.github.xlives.controller.KRSSSimilarityController;
import io.github.xlives.controller.OWLSimilarityController;
import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.reasoner.DynamicProgrammingSimPiReasonerImpl;
import io.github.xlives.framework.reasoner.DynamicProgrammingSimReasonerImpl;
import io.github.xlives.framework.reasoner.TopDownSimPiReasonerImpl;
import io.github.xlives.framework.reasoner.TopDownSimReasonerImpl;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderManchesterSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
import io.github.xlives.service.SimilarityService;
import io.github.xlives.service.ValidationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Import(value= {OWLSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class,
        SuperRoleUnfolderKRSSSyntax.class, PreferenceProfile.class
})
@EnableBatchProcessing
@SpringBootApplication
public class BatchConfiguration {

    private static final File INPUT_CONCEPTS = new File("./input/input");
    private static final File OUTPUT_TOPDOWN_SIM = new File("./output/output");

    private static final String PATH_OWL_ONTOLOGY = "./input/family.owl";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private OWLServiceContext owlServiceContext;
    @Autowired
    private OWLSimilarityController owlSimilarityController;

    private List<String> concept1sToMeasure;
    private List<String> concept2sToMeasure;
    private StringBuilder topDownSimResult;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Tasks ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    protected Tasklet taskComputeTopDownSim() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                topDownSimResult = new StringBuilder();

                for (int i = 0; i < concept1sToMeasure.size(); i++) {
                    topDownSimResult.append(concept1sToMeasure.get(i));
                    topDownSimResult.append("\t");
                    topDownSimResult.append(concept2sToMeasure.get(i));
                    topDownSimResult.append("\t");
                    topDownSimResult.append(owlSimilarityController.measureSimilarityWithTopDownSim(concept1sToMeasure.get(i), concept2sToMeasure.get(i)));

                    List<String> benchmark = owlSimilarityController.getTopDownSimExecutionMap().get(concept1sToMeasure.get(i) + " tree").get(concept2sToMeasure.get(i) + " tree");
                    for (String result : benchmark) {
                        topDownSimResult.append("\t");
                        topDownSimResult.append(result);
                    }

                    topDownSimResult.append("\n");
                }

                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Tasklet taskReadInputConcepts() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                String[] lines = StringUtils.split(FileUtils.readFileToString(INPUT_CONCEPTS), "\n");

                concept1sToMeasure = new ArrayList<String>();
                concept2sToMeasure = new ArrayList<String>();

                for (String eachLine : lines) {
                    String[] concepts = StringUtils.split(eachLine);
                    concept1sToMeasure.add(concepts[0]);
                    concept2sToMeasure.add(concepts[1]);
                }

                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Tasklet taskReadInputOWLOntology() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                owlServiceContext.init(PATH_OWL_ONTOLOGY);

                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Tasklet taskWriteTopDownSimToFile() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                FileUtils.writeStringToFile(OUTPUT_TOPDOWN_SIM, topDownSimResult.toString(), false);

                return RepeatStatus.FINISHED;
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Steps ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    protected Step stepComputeTopDownSim() throws Exception {
        return this.stepBuilderFactory.get("stepComputeTopDownSim").tasklet(taskComputeTopDownSim()).build();
    }

    @Bean
    protected Step stepReadInputConcepts() throws Exception {
        return this.stepBuilderFactory.get("stepReadInputConcepts").tasklet(taskReadInputConcepts()).build();
    }

    @Bean
    protected Step stepReadInputOWLOntology() throws Exception {
        return this.stepBuilderFactory.get("stepReadInputOWLOntology").tasklet(taskReadInputOWLOntology()).build();
    }

    @Bean
    protected Step stepWriteTopDownSimToFile() throws Exception {
        return this.stepBuilderFactory.get("stepWriteTopDownSimToFile").tasklet(taskWriteTopDownSimToFile()).build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Job /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    public Job job() throws Exception {
        return this.jobBuilderFactory.get("job")
                .start(stepReadInputConcepts())
                .next(stepReadInputOWLOntology())
                .next(stepComputeTopDownSim())
                .next(stepWriteTopDownSimToFile())
                .build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Main ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws Exception {
        System.exit(SpringApplication
                .exit(SpringApplication.run(BatchConfiguration.class, args)));
    }
}
