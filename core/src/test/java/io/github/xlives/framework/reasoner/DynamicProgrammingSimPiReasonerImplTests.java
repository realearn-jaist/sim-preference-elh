package io.github.xlives.framework.reasoner;

import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.descriptiontree.Tree;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.unfolding.IRoleUnfolder;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DynamicProgrammingSimPiReasonerImpl.class,
        SuperRoleUnfolderManchesterSyntax.class, PreferenceProfile.class, KRSSServiceContext.class,

        OWLServiceContext.class, TreeBuilder.class, SuperRoleUnfolderKRSSSyntax.class
})
public class DynamicProgrammingSimPiReasonerImplTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_FEMALE = "Female'";
    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_MALE = "Male'";

    private static final String MANCHESTER_CONCEPT_SON_IN_LAW = "((Male' and (Sex' and Thing)) and Person) and (hasParent some (Person and (isSpouseOf some Person)))";
    private static final String MANCHESTER_CONCEPT_GRANDFATHER = "((Male' and (Sex' and Thing)) and Person) and (isFatherOf some (Person and (isParentOf some Person)))";

    private static final String ROLE_HAS_CHILD = "hasChild";
    private static final String ROLE_HAS_PARENT = "hasParent";
    private static final String ROLE_HAS_SON = "hasSon";
    private static final String ROLE_IS_FATHER_OF = "isFatherOf";

    private static final String PRIMITIVE_ROLE_NAME_HAS_PARENT = "hasParent'";
    private static final String PRIMITIVE_ROLE_NAME_HAS_ANCESTOR = "hasAncestor'";
    private static final String PRIMITIVE_ROLE_NAME_IS_BLOOD_RELATION_OF = "isBloodRelationOf'";
    private static final String PRIMITIVE_ROLE_NAME_IS_RELATION_OF = "isRelationOf";

    private static final String KRSS_CONCEPT_GRANDFATHER = "(and (and (and Male' (and Sex' Thing)) Person) (some isFatherOf (and Person (some isParentOf Person))))";
    private static final String KRSS_CONCEPT_SON_IN_LAW = "(and (and (and Male' (and Sex' Thing)) Person) (some hasParent (and Person (some isSpouseOf Person))))";

    private Map<String, String> primitiveRoleDefinitionMap = new HashMap<String, String>();

    @Autowired
    private TreeBuilder treeBuilder;
    @Autowired
    private PreferenceProfile preferenceProfile;
    @Autowired
    private OWLServiceContext OWLServiceContext;
    @Autowired
    private DynamicProgrammingSimPiReasonerImpl dynamicProgrammingSimPiReasoner;

    @Mock
    private KRSSServiceContext krssServiceContext;

    @Resource(name="superRoleUnfolderKRSSSyntax")
    @InjectMocks
    private IRoleUnfolder superRoleUnfolderKRSSSyntax;

    @Before
    public void init() {
        this.OWLServiceContext.init(OWL_FILE_PATH);

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

        // Populate primitive role map
        this.primitiveRoleDefinitionMap.put("hasParent", "(and hasParent' hasAncestor)");
        this.primitiveRoleDefinitionMap.put("hasAncestor", "(and hasAncestor' isBloodRelationOf)");
        this.primitiveRoleDefinitionMap.put("isBloodRelationOf", "(and isBloodRelationOf' isRelationOf)");
        this.primitiveRoleDefinitionMap.put("isSpouseOf", "(and isSpouseOf' isInLawOf)");
        this.primitiveRoleDefinitionMap.put("isInLawOf", "(and isInLawOf' isRelationOf)");

        MockitoAnnotations.initMocks(this);

        when(krssServiceContext.getPrimitiveRoleDefinitionMap()).thenReturn(primitiveRoleDefinitionMap);
    }
    @Test
    public void testMeasureDirectedSimilarityWRTManchesterSyntax() {
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son in Law", MANCHESTER_CONCEPT_SON_IN_LAW);
        Tree<Set<String>> grandFatherTree = treeBuilder.constructAccordingToManchesterSyntax("GrandFather", MANCHESTER_CONCEPT_GRANDFATHER);
        BigDecimal simSonInLawGrandFather = dynamicProgrammingSimPiReasoner.measureDirectedSimilarity(sonInLawTree, grandFatherTree);
        assertThat(simSonInLawGrandFather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.79657");
        BigDecimal simGrandFatherSonInLaw = dynamicProgrammingSimPiReasoner.measureDirectedSimilarity(grandFatherTree, sonInLawTree);
        assertThat(simGrandFatherSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90333");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTKRSSSyntax() {
        dynamicProgrammingSimPiReasoner.setRoleUnfoldingStrategy(superRoleUnfolderKRSSSyntax);

        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToKRSSSyntax("Son in Law", KRSS_CONCEPT_SON_IN_LAW);
        Tree<Set<String>> grandFatherTree = treeBuilder.constructAccordingToKRSSSyntax("GrandFather", KRSS_CONCEPT_GRANDFATHER);
        BigDecimal simSonInLawGrandFather = dynamicProgrammingSimPiReasoner.measureDirectedSimilarity(sonInLawTree, grandFatherTree);
        assertThat(simSonInLawGrandFather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.79657");
        BigDecimal simGrandFatherSonInLaw = dynamicProgrammingSimPiReasoner.measureDirectedSimilarity(grandFatherTree, sonInLawTree);
        assertThat(simGrandFatherSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90333");
    }
}
