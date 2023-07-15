package io.github.xlives.framework.reasoner;

import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.descriptiontree.Tree;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
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
@SpringApplicationConfiguration(classes = {TopDownSimPiReasonerImpl.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        SuperRoleUnfolderManchesterSyntax.class, PreferenceProfile.class, KRSSServiceContext.class,
        OWLServiceContext.class, TreeBuilder.class, SuperRoleUnfolderKRSSSyntax.class
})
public class TopDownSimPiReasonerImplTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_FEMALE = "Female'";
    private static final String FRESH_PRIMITIVE_CONCEPT_NAME_MALE = "Male'";

    private static final String MANCHESTER_CONCEPT_FEMALE = "Female' and (Sex' and Thing)";
    private static final String MANCHESTER_CONCEPT_MAN = "(Male' and (Sex' and Thing)) and Person";
    private static final String MANCHESTER_CONCEPT_SON = "((Male' and (Sex' and Thing)) and Person) and (hasParent some Person)";
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

    private static final String EXISTENTIAL_SON = "(hasParent some Person)";
    private static final String EXISTENTIAL_SON_IN_LAW = "(hasParent some (Person and (isSpouseOf some Person)))";
    private static final String EXISTENTIAL_GRAND_FATHER = "(isFatherOf some (Person and (isParentOf some Person)))";

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
    private TopDownSimPiReasonerImpl topDownSimPiReasoner;

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
    public void testMuPi() {
        Tree<Set<String>> manTree = treeBuilder.constructAccordingToManchesterSyntax("Man", MANCHESTER_CONCEPT_MAN);
        BigDecimal muPiMan = topDownSimPiReasoner.muPi(manTree.getNodes().get(0));
        assertThat(muPiMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        BigDecimal muPiSon = topDownSimPiReasoner.muPi(sonTree.getNodes().get(0));
        assertThat(muPiSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.71429");
    }

    @Test
    public void testPhdPi() {
        Tree<Set<String>> femaleTree = treeBuilder.constructAccordingToManchesterSyntax("Female", MANCHESTER_CONCEPT_FEMALE);
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        BigDecimal phdPiFemaleSon = topDownSimPiReasoner.phdPi(femaleTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phdPiFemaleSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.60000");
        BigDecimal phdPiSonFemale = topDownSimPiReasoner.phdPi(sonTree.getNodes().get(0), femaleTree.getNodes().get(0));
        assertThat(phdPiSonFemale.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.48000");

        Tree<Set<String>> existSonTree = treeBuilder.constructAccordingToManchesterSyntax("Existential Son", EXISTENTIAL_SON);
        BigDecimal phdPiExistSonSon = topDownSimPiReasoner.phdPi(existSonTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phdPiExistSonSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");
        BigDecimal phdPiSonExistSon = topDownSimPiReasoner.phdPi(sonTree.getNodes().get(0), existSonTree.getNodes().get(0));
        assertThat(phdPiSonExistSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        Tree<Set<String>> topTree = treeBuilder.constructAccordingToManchesterSyntax("Top", "owl:Thing");
        BigDecimal phdTopSon = topDownSimPiReasoner.phdPi(topTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phdTopSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");
    }

    @Test
    public void testGammaPi() {
        BigDecimal gammaHasChildHasSon = topDownSimPiReasoner.gammaPi(ROLE_HAS_CHILD, ROLE_HAS_SON);
        assertThat(gammaHasChildHasSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal gammaHasSonHasChild = topDownSimPiReasoner.gammaPi(ROLE_HAS_SON, ROLE_HAS_CHILD);
        assertThat(gammaHasSonHasChild.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal gammaHasParentIsFatherOf = topDownSimPiReasoner.gammaPi(ROLE_HAS_PARENT, ROLE_IS_FATHER_OF);
        assertThat(gammaHasParentIsFatherOf.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.48000");

        BigDecimal gammaIsFatherOfHasParent = topDownSimPiReasoner.gammaPi(ROLE_IS_FATHER_OF, ROLE_HAS_PARENT);
        assertThat(gammaIsFatherOfHasParent.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.60000");
    }

    @Test
    public void testEHdPi() {
        Tree<Set<String>> existSonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son", EXISTENTIAL_SON_IN_LAW);
        Tree<Set<String>> existGrandfatherTree = treeBuilder.constructAccordingToManchesterSyntax("Grandfather", EXISTENTIAL_GRAND_FATHER);
        BigDecimal ehd1Direction1 = topDownSimPiReasoner.eHdPi(existSonInLawTree.getNodes().get(0).getChildren().get(0), existGrandfatherTree.getNodes().get(0).getChildren().get(0));
        assertThat(ehd1Direction1.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.28800");
        BigDecimal ehd1Direction2 = topDownSimPiReasoner.eHdPi(existGrandfatherTree.getNodes().get(0).getChildren().get(0), existSonInLawTree.getNodes().get(0).getChildren().get(0));
        assertThat(ehd1Direction2.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.42000");
    }

    @Test
    public void testESetHdPi() {
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON_IN_LAW);
        Tree<Set<String>> grandfatherTree = treeBuilder.constructAccordingToManchesterSyntax("Grandfather", MANCHESTER_CONCEPT_GRANDFATHER);
        BigDecimal eSetHdSonInLawGrandfather = topDownSimPiReasoner.eSetHdPi(sonInLawTree.getNodes().get(0), grandfatherTree.getNodes().get(0));
        assertThat(eSetHdSonInLawGrandfather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.28800");
        BigDecimal eSetHdGrandfatherSonInLaw = topDownSimPiReasoner.eSetHdPi(grandfatherTree.getNodes().get(0), sonInLawTree.getNodes().get(0));
        assertThat(eSetHdGrandfatherSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.42000");

        Tree<Set<String>> manTree = treeBuilder.constructAccordingToManchesterSyntax("Man", MANCHESTER_CONCEPT_MAN);
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        BigDecimal eSetHdManSon = topDownSimPiReasoner.eSetHdPi(manTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(eSetHdManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");
        BigDecimal eSetHdSonMan = topDownSimPiReasoner.eSetHdPi(sonTree.getNodes().get(0), manTree.getNodes().get(0));
        assertThat(eSetHdSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTManchesterSyntax() {
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son in Law", MANCHESTER_CONCEPT_SON_IN_LAW);
        Tree<Set<String>> grandFatherTree = treeBuilder.constructAccordingToManchesterSyntax("GrandFather", MANCHESTER_CONCEPT_GRANDFATHER);
        BigDecimal simSonInLawGrandFather = topDownSimPiReasoner.measureDirectedSimilarity(sonInLawTree, grandFatherTree);
        assertThat(simSonInLawGrandFather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.79657");
        BigDecimal simGrandFatherSonInLaw = topDownSimPiReasoner.measureDirectedSimilarity(grandFatherTree, sonInLawTree);
        assertThat(simGrandFatherSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90333");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTKRSSSyntax() {
        topDownSimPiReasoner.setRoleUnfoldingStrategy(superRoleUnfolderKRSSSyntax);

        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToKRSSSyntax("Son in Law", KRSS_CONCEPT_SON_IN_LAW);
        Tree<Set<String>> grandFatherTree = treeBuilder.constructAccordingToKRSSSyntax("GrandFather", KRSS_CONCEPT_GRANDFATHER);
        BigDecimal simSonInLawGrandFather = topDownSimPiReasoner.measureDirectedSimilarity(sonInLawTree, grandFatherTree);
        assertThat(simSonInLawGrandFather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.79657");
        BigDecimal simGrandFatherSonInLaw = topDownSimPiReasoner.measureDirectedSimilarity(grandFatherTree, sonInLawTree);
        assertThat(simGrandFatherSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.90333");
    }
}
