package io.github.xlives.controller;


import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.reasoner.*;
import io.github.xlives.framework.unfolding.*;
import io.github.xlives.service.SimilarityService;
import io.github.xlives.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {KRSSSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SuperRoleUnfolderKRSSSyntax.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class, PreferenceProfile.class
})
public class KRSSSimilarityControllerSNOMEDTests {


    @Autowired
    private KRSSSimilarityController krssSimilarityController;

    @Autowired
    private KRSSServiceContext krssServiceContext;

    @Before
    public void init() {

        krssServiceContext.init("snomed.krss");
    }

    @Test
    public void testMeasureSimilarityWithOWLSim() {
        BigDecimal value2 = krssSimilarityController.measureSimilarityWithTopDownSim("10001005", "10001005");
        assertThat(value2).isEqualTo(BigDecimal.ONE.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString());

        BigDecimal value3 = krssSimilarityController.measureSimilarityWithTopDownSim("10001005", "10002003");
        assertThat(value3).isEqualTo("0.48016");

        BigDecimal value4 = krssSimilarityController.measureSimilarityWithTopDownSim("10001005", "10006000");
        assertThat(value4).isEqualTo("0.48796");

        BigDecimal value5 = krssSimilarityController.measureSimilarityWithTopDownSim("10001005", "1001000");
        assertThat(value5).isEqualTo("0.51113");

        BigDecimal value6 = krssSimilarityController.measureSimilarityWithTopDownSim("308021002", "199388005");
        assertThat(value6).isEqualTo("0.30804");

        BigDecimal value7 = krssSimilarityController.measureSimilarityWithTopDownSim("308021002", "290065008");
        assertThat(value7).isEqualTo("0.38095");
    }

}

