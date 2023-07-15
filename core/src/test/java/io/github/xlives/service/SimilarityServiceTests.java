package io.github.xlives.service;

import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.reasoner.*;
import io.github.xlives.framework.unfolding.*;
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
@SpringApplicationConfiguration(classes = {SimilarityService.class, SuperRoleUnfolderManchesterSyntax.class,
        TopDownSimReasonerImpl.class, TopDownSimPiReasonerImpl.class,
        DynamicProgrammingSimReasonerImpl.class, DynamicProgrammingSimPiReasonerImpl.class,
        ConceptDefinitionUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        SuperRoleUnfolderKRSSSyntax.class,
        TreeBuilder.class, OWLServiceContext.class, KRSSServiceContext.class,
        PreferenceProfile.class
})
public class SimilarityServiceTests {

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

    private Map<String, String> fullConceptDefinitionMap = new HashMap<String, String>();
    private Map<String, String> primitiveConceptDefinitionMap = new HashMap<String, String>();
    private Map<String, String> primitiveRoleDefinitionMap = new HashMap<String, String>();

    @Autowired
    private SimilarityService similarityService;

    @Mock
    private KRSSServiceContext krssServiceContext;

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Autowired
    private PreferenceProfile preferenceProfile;

    @Resource(name="topDownSimReasonerImpl")
    @InjectMocks
    private IReasoner topDownSimReasonerImpl;

    @Resource(name="topDownSimPiReasonerImpl")
    @InjectMocks
    private IReasoner topDownSimPiReasonerImpl;

    @Resource(name="dynamicProgrammingSimReasonerImpl")
    @InjectMocks
    private IReasoner dynamicProgrammingSimReasonerImpl;

    @Resource(name="dynamicProgrammingSimPiReasonerImpl")
    @InjectMocks
    private IReasoner dynamicProgrammingSimPiReasonerImpl;

    @Resource(name="conceptDefinitionUnfolderKRSSSyntax")
    @InjectMocks
    private IConceptUnfolder conceptDefinitionUnfolderKRSSSyntax;

    @Resource(name="superRoleUnfolderKRSSSyntax")
    @InjectMocks
    private IRoleUnfolder superRoleUnfolderKRSSSyntax;

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

        // Populate full concept definition map
        this.fullConceptDefinitionMap.put("Man", "(and Male Person)");
        this.fullConceptDefinitionMap.put("Son", "(and Man (some hasParent Person))");
        this.fullConceptDefinitionMap.put("SonInLaw", "(and Man (some hasParent (and Person (some isSpouseOf Person))))");
        this.fullConceptDefinitionMap.put("Grandfather", "(and Man (some isFatherOf (and Person (some isParentOf Person))))");

        // Populate primitive concept definition map
        this.primitiveConceptDefinitionMap.put("Male", "(and Male' Sex)");
        this.primitiveConceptDefinitionMap.put("Sex", "(and Sex' Thing)");
        this.primitiveConceptDefinitionMap.put("Female", "(and Female' Sex)");

        // Populate primitive role map
        this.primitiveRoleDefinitionMap.put("hasParent", "(and hasParent' hasAncestor)");
        this.primitiveRoleDefinitionMap.put("hasAncestor", "(and hasAncestor' isBloodRelationOf)");
        this.primitiveRoleDefinitionMap.put("isBloodRelationOf", "(and isBloodRelationOf' isRelationOf)");
        this.primitiveRoleDefinitionMap.put("isSpouseOf", "(and isSpouseOf' isInLawOf)");
        this.primitiveRoleDefinitionMap.put("isInLawOf", "(and isInLawOf' isRelationOf)");

        MockitoAnnotations.initMocks(this);

        when(krssServiceContext.getFullConceptDefinitionMap()).thenReturn(fullConceptDefinitionMap);
        when(krssServiceContext.getPrimitiveConceptDefinitionMap()).thenReturn(primitiveConceptDefinitionMap);
        when(krssServiceContext.getPrimitiveRoleDefinitionMap()).thenReturn(primitiveRoleDefinitionMap);
    }

    @Test
    public void testMeasureOWLConcetpsWithTopDownSim() {
        BigDecimal value1 = similarityService.measureOWLConcetpsWithTopDownSim("Son", "SonInLaw");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.97000");

        BigDecimal value2 = similarityService.measureOWLConcetpsWithTopDownSim("Son", "Man");
        assertThat(value2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90000");

        BigDecimal value3 = similarityService.measureOWLConcetpsWithTopDownSim("Female", "Person");
        assertThat(value3.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureOWLConceptsWithTopDownSimPi() {
        BigDecimal value1 = similarityService.measureOWLConceptsWithTopDownSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }

    @Test
    public void testMeasureOWLConceptsWithDynamicProgrammingSim() {
        BigDecimal value1 = similarityService.measureOWLConceptsWithDynamicProgrammingSim("Son", "SonInLaw");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.97000");

        BigDecimal value2 = similarityService.measureOWLConceptsWithDynamicProgrammingSim("Son", "Man");
        assertThat(value2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90000");

        BigDecimal value3 = similarityService.measureOWLConceptsWithDynamicProgrammingSim("Female", "Person");
        assertThat(value3.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureOWLConceptsWithDynamicProgrammingSimPi() {
        BigDecimal value1 = similarityService.measureOWLConceptsWithDynamicProgrammingSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }

    @Test
    public void testMeasureKRSSConcetpsWithTopDownSim() {
        BigDecimal value1 = similarityService.measureKRSSConcetpsWithTopDownSim("Son", "SonInLaw");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.97000");

        BigDecimal value2 = similarityService.measureKRSSConcetpsWithTopDownSim("Son", "Man");
        assertThat(value2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90000");

        BigDecimal value3 = similarityService.measureKRSSConcetpsWithTopDownSim("Female", "Person");
        assertThat(value3.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureKRSSConceptsWithTopDownSimPi() {
        BigDecimal value1 = similarityService.measureKRSSConceptsWithTopDownSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }

    @Test
    public void testMeasureKRSSConceptsWithDynamicProgrammingSim() {
        BigDecimal value1 = similarityService.measureKRSSConceptsWithDynamicProgrammingSim("Son", "SonInLaw");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.97000");

        BigDecimal value2 = similarityService.measureKRSSConceptsWithDynamicProgrammingSim("Son", "Man");
        assertThat(value2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90000");

        BigDecimal value3 = similarityService.measureKRSSConceptsWithDynamicProgrammingSim("Female", "Person");
        assertThat(value3.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureKRSSConceptsWithDynamicProgrammingSimPi() {
        BigDecimal value1 = similarityService.measureKRSSConceptsWithDynamicProgrammingSimPi("SonInLaw", "Grandfather");
        assertThat(value1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.84995");
    }
}
