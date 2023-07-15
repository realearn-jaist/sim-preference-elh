package io.github.xlives;

import io.github.xlives.controller.KRSSSimilarityController;
import io.github.xlives.controller.OWLSimilarityController;
import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String HEADER_RESULT_FOR_BU = "concept" + "\t" + "concept" + "\t" + "similarity" + "\t" + "millisecond" + "\t" + "millisecond" + "\t" + "millisecond";
    private static final String HEADER_RESULT_FOR_TD = "concept" + "\t" + "concept" + "\t" + "similarity" + "\t" + "millisecond";

    @Autowired
    private OWLServiceContext owlServiceContext;

    @Autowired
    private OWLSimilarityController owlSimilarityController;
    @Autowired
    private KRSSSimilarityController krssSimilarityController;

    private static final String FINDIING_CONCEPTS_LIST = "/Users/Xlives/HgProjects/jSimPi/core/1st_rand_sct_finding.txt";
    private static final String PROCEDURE_CONCEPTS_LIST = "/Users/Xlives/HgProjects/jSimPi/core/1st_rand_sct_procedure.txt";

    @Autowired
    private KRSSServiceContext krssServiceContext;

    private StringBuilder excludePrimitivelyDefinedDefinition(List<String> list) {

        StringBuilder builder = new StringBuilder();

        Set<String> fullDefinition = krssServiceContext.getFullConceptDefinitionMap().keySet();
        Set<String> primitiveDefinition = krssServiceContext.getPrimitiveConceptDefinitionMap().keySet();

        for (String concept : list) {
            if (fullDefinition.contains(concept) && primitiveDefinition.contains(concept)) {
                throw new JSimPiException("TEST", ErrorCode.TreeBuilder_IllegalArguments);
            }

            else if (primitiveDefinition.contains(concept)) {
                // Do nothing
            }

            else {
                builder.append(concept);
                builder.append("\n");
            }
        }

        return builder;
    }

    private StringBuilder excludeFullDefinition(List<String> list) {

        StringBuilder builder = new StringBuilder();

        Set<String> fullDefinition = krssServiceContext.getFullConceptDefinitionMap().keySet();
        Set<String> primitiveDefinition = krssServiceContext.getPrimitiveConceptDefinitionMap().keySet();

        for (String concept : list) {
            if (fullDefinition.contains(concept) && primitiveDefinition.contains(concept)) {
                throw new JSimPiException("TEST", ErrorCode.TreeBuilder_IllegalArguments);
            }

            else if (fullDefinition.contains(concept)) {
                // Do nothing
            }

            else {
                builder.append(concept);
                builder.append("\n");
            }
        }

        return builder;
    }

    private List<String> readSctDescriptionFile(String filePath) {

        List<String> concepts = new ArrayList<String>();

        BufferedReader bufferedReader = null;

        try {

            bufferedReader = new BufferedReader(new FileReader(filePath));
            String readLine;

            while ((readLine = bufferedReader.readLine()) != null) {
                concepts.add(org.apache.commons.lang3.StringUtils.trim(readLine));
            }

            return concepts;
        } catch (FileNotFoundException e) {

            // throw new JSimPiException("Unable to read krss file from path[" + krssFilePath + "] due to file not found exception.", e, ErrorCode.KRSSServiceContext_FileNotFoundException);
        } catch (IOException e) {
            // throw new JSimPiException("Unable to read krss file from path[" + krssFilePath + "] due to file not found exception.", e, ErrorCode.KRSSServiceContext_IOException);
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // OPTIONS TO RUN //////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void run3(String... args) {
        krssServiceContext.init("/Users/Xlives/HgProjects/jSimPi/core/snomed.krss");

        List<String> lines = readSctDescriptionFile("/Users/Xlives/Desktop/Sampled/defined_finding_finding.txt");

        StringBuilder builder = new StringBuilder();
        builder.append(HEADER_RESULT_FOR_TD + "\n");

        for (String line : lines) {
            String[] each = org.apache.commons.lang3.StringUtils.split(line, "\t");
            BigDecimal degree = krssSimilarityController.measureSimilarityWithTopDownSimPi(each[0], each[1]);
            builder.append(each[0] + "\t" + each[1] +  "\t" + degree);

            List<String> benchmark = krssSimilarityController.getTopDownSimPiExecutionMap().get(each[0] + " tree").get(each[1] + " tree");

            for (String result : benchmark) {
                builder.append("\t");
                builder.append(result);
            }

            builder.append("\n");
        }

        try {
            FileUtils.writeStringToFile(new File("/Users/Xlives/Desktop/table6/td-sim-pi-0/defined_finding_finding/output-13.txt"), builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run2(String... args) {
        krssServiceContext.init("/Users/Xlives/HgProjects/jSimPi/core/snomed.krss");

        List<String> findingConcepts = readSctDescriptionFile(FINDIING_CONCEPTS_LIST);
        List<String> procedureConcepts = readSctDescriptionFile(PROCEDURE_CONCEPTS_LIST);

        StringBuilder primitiveDefinitionFinding = excludeFullDefinition(findingConcepts);
        StringBuilder primitiveDefinitionProcedure = excludeFullDefinition(procedureConcepts);

//        StringBuilder fullDefinitionFinding = excludePrimitivelyDefinedDefinition(findingConcepts);
//        StringBuilder fullDefinitionProcedure = excludePrimitivelyDefinedDefinition(procedureConcepts);

        String[] primitivelyFindingArray = org.apache.commons.lang3.StringUtils.split(primitiveDefinitionFinding.toString(), "\n");
        String[] primitivelyProcedureArray = org.apache.commons.lang3.StringUtils.split(primitiveDefinitionProcedure.toString(), "\n");

        List<String> primitivelyFindingList = Arrays.asList(primitivelyFindingArray);
        List<String> primitivelyProcedureList = Arrays.asList(primitivelyProcedureArray);

        findingConcepts.removeAll(primitivelyFindingList);
        procedureConcepts.removeAll(primitivelyProcedureList);

        StringBuilder fullDefinitionFinding = new StringBuilder();
        StringBuilder fullDefinitionProcedure = new StringBuilder();

        for (String concept : findingConcepts) {
            fullDefinitionFinding.append(concept);
            fullDefinitionFinding.append("\n");
        }

        for (String concept : procedureConcepts) {
            fullDefinitionProcedure.append(concept);
            fullDefinitionProcedure.append("\n");
        }


//        String[] fullFindingArray = org.apache.commons.lang3.StringUtils.split(fullDefinitionFinding.toString(), "\n");
//        String[] fullProcedureArray = org.apache.commons.lang3.StringUtils.split(fullDefinitionProcedure.toString(), "\n");

        StringBuilder primitivelyFindingFinding = new StringBuilder();
        StringBuilder primitivelyFindingProcedure = new StringBuilder();
        StringBuilder primitivelyProcedureProcedure = new StringBuilder();

        StringBuilder definedFindingFinding = new StringBuilder();
        StringBuilder definedFindingProcedure = new StringBuilder();
        StringBuilder definedProcedureProcedure = new StringBuilder();

        // Primitively definition x Primitively definition

        for (int i = 0; i < primitivelyFindingArray.length; i++) {
            for (int j = 0; j < primitivelyFindingArray.length; j++) {
                primitivelyFindingFinding.append(primitivelyFindingArray[i] + "\t" + primitivelyFindingArray[j] + "\n");
            }
        }

        for (int i = 0; i < primitivelyFindingArray.length; i++) {
            for (int j = 0; j < primitivelyProcedureArray.length; j++) {
                primitivelyFindingProcedure.append(primitivelyFindingArray[i] + "\t" + primitivelyProcedureArray[j] + "\n");
            }
        }

        for (int i = 0; i < primitivelyProcedureArray.length; i++) {
            for (int j = 0; j < primitivelyProcedureArray.length; j++) {
                primitivelyProcedureProcedure.append(primitivelyProcedureArray[i] + "\t" + primitivelyProcedureArray[j] + "\n");
            }
        }

        // Full definition x Full definition

        for (int i = 0; i < findingConcepts.size(); i++) {
            for (int j = 0; j < findingConcepts.size(); j++) {
                definedFindingFinding.append(findingConcepts.get(i) + "\t" + findingConcepts.get(j) + "\n");
            }
        }

        for (int i = 0; i < findingConcepts.size(); i++) {
            for (int j = 0; j < procedureConcepts.size(); j++) {
                definedFindingProcedure.append(findingConcepts.get(i) + "\t" + procedureConcepts.get(j) + "\n");
            }
        }

        for (int i = 0; i < procedureConcepts.size(); i++) {
            for (int j = 0; j < procedureConcepts.size(); j++) {
                definedProcedureProcedure.append(procedureConcepts.get(i) + "\t" + procedureConcepts.get(j) + "\n");
            }
        }

        try {
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/primitively_defined_finding.txt"), primitiveDefinitionFinding.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/primitively_defined_procedure.txt"), primitiveDefinitionProcedure.toString());

            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/defined_finding.txt"), fullDefinitionFinding.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/defined_procedure.txt"), fullDefinitionProcedure.toString());

            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/primitively_defined_finding_finding.txt"), primitivelyFindingFinding.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/primitively_defined_finding_procedure.txt"), primitivelyFindingProcedure.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/primitively_defined_procedure_procedure.txt"), primitivelyProcedureProcedure.toString());

            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/defined_finding_finding.txt"), definedFindingFinding.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/defined_finding_procedure.txt"), definedFindingProcedure.toString());
            FileUtils.writeStringToFile(new File("/Users/Xlives/HgProjects/jSimPi/core/defined_procedure_procedure.txt"), definedProcedureProcedure.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run1(String... args) {
        if(logger.isInfoEnabled()) {
            logger.info("jSimPi - similarity measure for ELH concepts - has just started.");
        }

        // Invoke business logic
        try {

            String owlFilepath = StringUtils.trimWhitespace(args[0]);
            String conceptName1 = StringUtils.trimWhitespace(args[1]);
            String conceptName2 = StringUtils.trimWhitespace(args[2]);

            if (logger.isInfoEnabled()) {
                logger.info("Loading OWL knowledge base from path: " + owlFilepath);
                logger.info("Measuring similarity between " + conceptName1 + " and " + conceptName2);
            }

            owlServiceContext.init(owlFilepath);

            BigDecimal value = owlSimilarityController.measureSimilarityWithDynamicProgrammingSim(conceptName1, conceptName2);

            if (logger.isInfoEnabled()) {
                logger.info("Done! The similarity between " + conceptName1 + " and " + conceptName2 + " is " + value.toPlainString() + " %.");
            }
        }

        catch (JSimPiException e) {
            throw new JSimPiException("Unable to run", e, ErrorCode.Application_IllegalArguments);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * teeradaj@20160307: http://stackoverflow.com/questions/23316843/get-command-line-arguments-from-spring-bootrun
     * See the above to understand how arguments are passed to a Spring Boot application via
     * a command line interface.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //run1(args);

        run3(args);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
