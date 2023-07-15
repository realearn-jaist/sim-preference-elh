package io.github.xlives.controller;

import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class KRSSSimilarityController {

    @Autowired
    private SimilarityService similarityService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public BigDecimal measureSimilarityWithTopDownSim(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down Sim as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        BigDecimal value = similarityService.measureKRSSConcetpsWithTopDownSim(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithTopDownSimPi(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down SimPi as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        BigDecimal value = similarityService.measureKRSSConceptsWithTopDownSimPi(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithDynamicProgrammingSim(String conceptName1, String conceptName2) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down Sim as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        BigDecimal value = similarityService.measureKRSSConceptsWithDynamicProgrammingSim(conceptName1, conceptName2);

        return value.setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal measureSimilarityWithDynamicProgrammingSimPi(String conceptName1, String conceptName2) {
        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with top down SimPi as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        BigDecimal value = similarityService.measureKRSSConceptsWithDynamicProgrammingSimPi(conceptName1, conceptName2);

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
