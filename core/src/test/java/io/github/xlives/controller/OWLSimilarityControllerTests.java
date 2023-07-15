package io.github.xlives.controller;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {OWLSimilarityController.class, ValidationService.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class,
        SuperRoleUnfolderKRSSSyntax.class, PreferenceProfile.class
})
public class OWLSimilarityControllerTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_FEMALE = "Female'";
    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_MALE = "Male'";

    private static final String ROLE_HAS_CHILD = "hasChild";
    private static final String ROLE_HAS_PARENT = "hasParent";
    private static final String ROLE_HAS_SON = "hasSon";
    private static final String ROLE_IS_FATHER_OF = "isFatherOf";

    private static final String PRIMITIVE_ROLE_NAME_HAS_PARENT = "hasParent'";
    private static final String PRIMITIVE_ROLE_NAME_HAS_ANCESTOR = "hasAncestor'";
    private static final String PRIMITIVE_ROLE_NAME_IS_BLOOD_RELATION_OF = "isBloodRelationOf'";
    private static final String PRIMITIVE_ROLE_NAME_IS_RELATION_OF = "isRelationOf";

    @Autowired
    private OWLSimilarityController owlSimilarityController;

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Autowired
    private PreferenceProfile preferenceProfile;

    @Before
    public void init() {
        OWLServiceContext.init(OWL_FILE_PATH);

        // Populate primitive concept importance
        this.preferenceProfile.addPrimitiveConceptImportance(FRESH_PRIMITIVE_CONCEPT_NAME_FEMALE, new BigDecimal("2"));
        this.preferenceProfile.addPrimitiveConceptImportance(FRESH_PRIMITIVE_CONCEPT_NAME_MALE, new BigDecimal("2"));

        // Populate role importance
        this.preferenceProfile.addRoleImportance(ROLE_HAS_CHILD, BigDecimal.ZERO);
        this.preferenceProfile.addRoleImportance(ROLE_HAS_PARENT, new BigDecimal("2"));
        this.preferenceProfile.addRoleImportance(ROLE_HAS_SON, new BigDecimal("2"));
        this.preferenceProfile.addRoleImportance(PRIMITIVE_ROLE_NAME_IS_RELATION_OF, new BigDecimal("2"));

        // Populate primitive concepts similarity
        this.preferenceProfile.addPrimitveConceptsSimilarity("Female'", "Male'", new BigDecimal("0.2"));

        // Populate roles similarity
        this.preferenceProfile.addPrimitiveRolesSimilarity(ROLE_IS_FATHER_OF, PRIMITIVE_ROLE_NAME_HAS_PARENT, new BigDecimal("0.6"));
        this.preferenceProfile.addPrimitiveRolesSimilarity(ROLE_IS_FATHER_OF, PRIMITIVE_ROLE_NAME_HAS_ANCESTOR, new BigDecimal("0.4"));
        this.preferenceProfile.addPrimitiveRolesSimilarity(ROLE_IS_FATHER_OF, PRIMITIVE_ROLE_NAME_IS_BLOOD_RELATION_OF, new BigDecimal("0.2"));
        this.preferenceProfile.addPrimitiveRolesSimilarity(ROLE_IS_FATHER_OF, PRIMITIVE_ROLE_NAME_IS_RELATION_OF, new BigDecimal("0.6"));

        // Populate role discount factor
        this.preferenceProfile.addRoleDiscountFactor(ROLE_HAS_PARENT, new BigDecimal("0.2"));
    }

    @Test
    public void testMeasureSimilarityWithOWLSim() {
        BigDecimal value1 = owlSimilarityController.measureSimilarityWithTopDownSim("Son", "SonInLaw");
        assertThat(value1).isEqualTo(new BigDecimal("0.97000"));

        BigDecimal value2 = owlSimilarityController.measureSimilarityWithTopDownSim("Son", "owl:Thing");
        assertThat(value2).isEqualTo(new BigDecimal("0.50000"));
    }

    @Test
    public void testMeasureSimilarityWithOWLSimPi() {
        BigDecimal value1 = owlSimilarityController.measureSimilarityWithTopDownSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }

    @Test
    public void testMeasureSimilarityWithDynamicProgrammingSim() {
        BigDecimal value1 = owlSimilarityController.measureSimilarityWithDynamicProgrammingSim("Son", "SonInLaw");
        assertThat(value1).isEqualTo(new BigDecimal("0.97000"));
    }

    @Test
    public void testMeasureSimilarityWithDynamicProgrammingSimPi() {
        BigDecimal value1 = owlSimilarityController.measureSimilarityWithDynamicProgrammingSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }
}
