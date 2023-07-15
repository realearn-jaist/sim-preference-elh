package io.githib.xlives.batch.krss.dynamicprogramming.simpi;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Import(value= {KRSSSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SuperRoleUnfolderKRSSSyntax.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class, PreferenceProfile.class
})
@EnableBatchProcessing
@SpringBootApplication
public class BatchConfiguration {

    private static final String HEADER_RESULT = "concept" + "\t" + "concept" + "\t" + "similarity" + "\t" + "millisecond" + "\t" + "millisecond" + "\t" + "millisecond";

    private static final File INPUT_CONCEPTS = new File("./input/input");
    private static final File INPUT_PRIMITIVE_CONCEPT_IMPORTANCE = new File("./input/preference-profile/primitive-concept-importance");
    private static final File INPUT_ROLE_IMPORTANCE = new File("./input/preference-profile/role-importance");
    private static final File INPUT_PRIMITIVE_CONCEPTS_SIMILARITY = new File("./input/preference-profile/primitive-concepts-similarity");
    private static final File INPUT_PRIMITIVE_ROLES_SIMILARITY = new File("./input/preference-profile/primitive-roles-similarity");
    private static final File INPUT_ROLE_DISCOUNT_FACTOR = new File("./input/preference-profile/role-discount-factor");

    private static final File OUTPUT_DYNAMICPROGRAMMING_SIMPI = new File("./output/output");

    private static final String PATH_KRSS_ONTOLOGY = "./input/snomed.krss";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private KRSSServiceContext krssServiceContext;
    @Autowired
    private KRSSSimilarityController krssSimilarityController;
    @Autowired
    private PreferenceProfile preferenceProfile;

    private List<String> concept1sToMeasure;
    private List<String> concept2sToMeasure;
    private StringBuilder dynamicProgrammingSimPiResult;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Tasks ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    protected Tasklet taskComputeDynamicProgrammingSimPi() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                dynamicProgrammingSimPiResult = new StringBuilder();
                dynamicProgrammingSimPiResult.append(HEADER_RESULT + "\n");

                for (int i = 0; i < concept1sToMeasure.size(); i++) {
                    dynamicProgrammingSimPiResult.append(concept1sToMeasure.get(i));
                    dynamicProgrammingSimPiResult.append("\t");
                    dynamicProgrammingSimPiResult.append(concept2sToMeasure.get(i));
                    dynamicProgrammingSimPiResult.append("\t");
                    dynamicProgrammingSimPiResult.append(krssSimilarityController.measureSimilarityWithDynamicProgrammingSimPi(concept1sToMeasure.get(i), concept2sToMeasure.get(i)));

                    List<String> benchmark = krssSimilarityController.getDynamicProgrammingSimPiExecutionMap().get(concept1sToMeasure.get(i) + " tree").get(concept2sToMeasure.get(i) + " tree");
                    for (String result : benchmark) {
                        dynamicProgrammingSimPiResult.append("\t");
                        dynamicProgrammingSimPiResult.append(result);
                    }

                    dynamicProgrammingSimPiResult.append("\n");
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
    protected Tasklet taskReadInputKRSSOntology() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                krssServiceContext.init(PATH_KRSS_ONTOLOGY);

                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Tasklet taskReadInputPreferenceProfile() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

                String[] primitiveConceptImportances = StringUtils.split(FileUtils.readFileToString(INPUT_PRIMITIVE_CONCEPT_IMPORTANCE), "\n");
                for (String primitiveConceptImportance : primitiveConceptImportances) {
                    String[] str = StringUtils.split(primitiveConceptImportance);
                    preferenceProfile.addPrimitiveConceptImportance(str[0], new BigDecimal(str[1]));
                }

                String[] roleImportances = StringUtils.split(FileUtils.readFileToString(INPUT_ROLE_IMPORTANCE), "\n");
                for (String roleImportance : roleImportances) {
                    String[] str = StringUtils.split(roleImportance);
                    preferenceProfile.addRoleImportance(str[0], new BigDecimal(str[1]));
                }

                String[] primitiveConceptsSimilarities = StringUtils.split(FileUtils.readFileToString(INPUT_PRIMITIVE_CONCEPTS_SIMILARITY), "\n");
                for (String primitiveConceptsSimilarity : primitiveConceptsSimilarities) {
                    String[] str = StringUtils.split(primitiveConceptsSimilarity);
                    preferenceProfile.addPrimitveConceptsSimilarity(str[0], str[1], new BigDecimal(str[2]));
                }

                String[] primitiveRolesSimilarities = StringUtils.split(FileUtils.readFileToString(INPUT_PRIMITIVE_ROLES_SIMILARITY), "\n");
                for (String primitiveRolesSimilarity : primitiveRolesSimilarities) {
                    String[] str = StringUtils.split(primitiveRolesSimilarity);
                    preferenceProfile.addPrimitiveRolesSimilarity(str[0], str[1], new BigDecimal(str[2]));
                }

                String[] roleDiscountFactors = StringUtils.split(FileUtils.readFileToString(INPUT_ROLE_DISCOUNT_FACTOR), "\n");
                for (String roleDiscountFactor : roleDiscountFactors) {
                    String[] str = StringUtils.split(roleDiscountFactor);
                    preferenceProfile.addRoleDiscountFactor(str[0], new BigDecimal(str[1]));
                }

                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Tasklet taskWriteDynamicProgrammingSimPiToFile() {

        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                FileUtils.writeStringToFile(OUTPUT_DYNAMICPROGRAMMING_SIMPI, dynamicProgrammingSimPiResult.toString(), false);

                return RepeatStatus.FINISHED;
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Steps ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    protected Step stepComputeDynamicProgrammingSimPi() throws Exception {
        return this.stepBuilderFactory.get("stepComputeDynamicProgrammingSimPi").tasklet(taskComputeDynamicProgrammingSimPi()).build();
    }

    @Bean
    protected Step stepReadInputConcepts() throws Exception {
        return this.stepBuilderFactory.get("stepReadInputConcepts").tasklet(taskReadInputConcepts()).build();
    }

    @Bean
    protected Step stepReadInputKRSSOntology() throws Exception {
        return this.stepBuilderFactory.get("stepReadInputKRSSOntology").tasklet(taskReadInputKRSSOntology()).build();
    }

    @Bean
    protected Step stepReadInputPreferenceProfile() throws Exception {
        return this.stepBuilderFactory.get("stepReadInputPreferenceProfile").tasklet(taskReadInputPreferenceProfile()).build();
    }

    @Bean
    protected Step stepWriteDynamicProgrammingSimPiToFile() throws Exception {
        return this.stepBuilderFactory.get("stepWriteDynamicProgrammingSimPiToFile").tasklet(taskWriteDynamicProgrammingSimPiToFile()).build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Job /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    public Job job() throws Exception {
        return this.jobBuilderFactory.get("job")
                .start(stepReadInputConcepts())
                .next(stepReadInputKRSSOntology())
                .next(stepReadInputPreferenceProfile())
                .next(stepComputeDynamicProgrammingSimPi())
                .next(stepWriteDynamicProgrammingSimPiToFile())
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

