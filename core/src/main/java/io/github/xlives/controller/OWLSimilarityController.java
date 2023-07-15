package io.github.xlives.controller;

import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.service.SimilarityService;
import io.github.xlives.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class OWLSimilarityController {

    private static final Logger logger = LoggerFactory.getLogger(OWLSimilarityController.class);

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SimilarityService similarityService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void validateInputs(String conceptName1, String conceptName2) {
        if (!validationService.validateIfOWLClassNamesExist(conceptName1, conceptName2)) {
            throw new JSimPiException("Unable to measure similarity with OWL sim as conceptName1["
                    + conceptName1 + "] and conceptName2[" + conceptName2 + "] are invalid names.", ErrorCode.OwlSimilarityController_InvalidConceptNames);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public BigDecimal measureSimilarityWithTopDownSim(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down Sim as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        BigDecimal value = similarityService.measureOWLConcetpsWithTopDownSim(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithTopDownSimPi(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down SimPi as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        BigDecimal value = similarityService.measureOWLConceptsWithTopDownSimPi(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithDynamicProgrammingSim(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down Sim as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        BigDecimal value = similarityService.measureOWLConceptsWithDynamicProgrammingSim(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithDynamicProgrammingSimPi(String conceptName1, String conceptName2) {
        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down SimPi as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        BigDecimal value = similarityService.measureOWLConceptsWithDynamicProgrammingSimPi(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Benchmarks //////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<String, Map<String, List<String>>> getTopDownSimExecutionMap() {
        return similarityService.getTopDownSimExecutionMap();
    }

    public Map<String, Map<String, List<String>>> getTopDownSimPiExecutionMap() {
        return similarityService.getTopDownSimPiExecutionMap();
    }

    public Map<String, Map<String, List<String>>> getDynamicProgrammingSimExecutionMap() {
        return similarityService.getDynamicProgrammingSimExecutionMap();
    }

    public Map<String, Map<String, List<String>>> getDynamicProgrammingSimPiExecutionMap() {
        return similarityService.getDynamicProgrammingSimPiExecutionMap();
    }
}
