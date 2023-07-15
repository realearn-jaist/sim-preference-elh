package io.github.xlives.framework.reasoner;

import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.descriptiontree.Tree;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TopDownSimReasonerImpl.class,
        SuperRoleUnfolderManchesterSyntax.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        OWLServiceContext.class, KRSSServiceContext.class,
        TreeBuilder.class})
public class TopDownSimReasonerImplTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String MANCHESTER_CONCEPT_0 = "(hasParent some Person)";

    private static final String MANCHESTER_CONCEPT_FEMALE = "Female' and (Sex' and Thing)";
    private static final String MANCHESTER_CONCEPT_MAN = "(Male' and (Sex' and Thing)) and Person";
    private static final String MANCHESTER_CONCEPT_PERSON = "Person";
    private static final String MANCHESTER_CONCEPT_SON = "((Male' and (Sex' and Thing)) and Person) and (hasParent some Person)";
    private static final String MANCHESTER_CONCEPT_SON_IN_LAW = "((Male' and (Sex' and Thing)) and Person) and (hasParent some (Person and (isSpouseOf some Person)))";
    private static final String MANCHESTER_CONCEPT_GRANDFATHER = "((Male' and (Sex' and Thing)) and Person) and (isFatherOf some (Person and (isParentOf some Person)))";

    private static final String ROLE_HAS_CHILD = "hasChild";
    private static final String ROLE_HAS_PARENT = "hasParent";
    private static final String ROLE_HAS_SON = "hasSon";
    private static final String ROLE_IS_FATHER_OF = "isFatherOf";

    private static final String MANCHESTER_EXISTENTIAL_SON = "(hasParent some Person)";
    private static final String MANCHESTER_EXISTENTIAL_GRANDFATHER = "(isFatherOf some (Person and (isParentOf some Person)))";

    private static final String KRSS_CONCEPT_FEMALE = "(and Female' (and Sex' Thing))";
    private static final String KRSS_CONCEPT_PERSON = "Person";
    private static final String KRSS_CONCEPT_MAN = "(and (and Male' (and Sex' Thing)) Person)";
    private static final String KRSS_CONCEPT_SON = "(and (and (and Male' (and Sex' Thing)) Person) (some hasParent Person))";
    private static final String KRSS_CONCEPT_SON_IN_LAW = "(and (and (and Male' (and Sex' Thing)) Person) (some hasParent (and Person (some isSpouseOf Person))))";

    @Autowired
    private TreeBuilder treeBuilder;

    @Autowired
    private TopDownSimReasonerImpl topDownSimReasonerImpl;

    @Autowired
    private ConceptDefinitionUnfolderKRSSSyntax conceptDefinitionUnfolderKRSSSyntax;

    @Autowired
    private OWLServiceContext oWLServiceContext;

    @Before
    public void init() {
        this.oWLServiceContext.init(OWL_FILE_PATH);
    }


    @Test
    public void testMu() {
        Tree<Set<String>> manTree = treeBuilder.constructAccordingToManchesterSyntax("Man", MANCHESTER_CONCEPT_MAN);
        BigDecimal muMan = topDownSimReasonerImpl.mu(manTree.getNodes().get(0));
        assertThat(muMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        BigDecimal muSon = topDownSimReasonerImpl.mu(sonTree.getNodes().get(0));
        assertThat(muSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.80000");
    }

    @Test
    public void testPhd() {
        Tree<Set<String>> manTree = treeBuilder.constructAccordingToManchesterSyntax("Man", MANCHESTER_CONCEPT_MAN);
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        BigDecimal phdManSon = topDownSimReasonerImpl.phd(manTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phdManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal phdSonMan = topDownSimReasonerImpl.phd(sonTree.getNodes().get(0), manTree.getNodes().get(0));
        assertThat(phdSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        Tree<Set<String>> concept0Tree = treeBuilder.constructAccordingToManchesterSyntax("Concept 0", MANCHESTER_CONCEPT_0);
        BigDecimal phd0Son = topDownSimReasonerImpl.phd(concept0Tree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phd0Son.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal phdSon0 = topDownSimReasonerImpl.phd(sonTree.getNodes().get(0), concept0Tree.getNodes().get(0));
        assertThat(phdSon0.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        Tree<Set<String>> topTree = treeBuilder.constructAccordingToManchesterSyntax("Top", "owl:Thing");
        BigDecimal phdTopSon = topDownSimReasonerImpl.phd(topTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(phdTopSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");
    }

    @Test
    public void testGamma() {
        BigDecimal gammaHasChildHasSon = topDownSimReasonerImpl.gamma(ROLE_HAS_CHILD, ROLE_HAS_SON);
        assertThat(gammaHasChildHasSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal gammaHasSonHasChild = topDownSimReasonerImpl.gamma(ROLE_HAS_SON, ROLE_HAS_CHILD);
        assertThat(gammaHasSonHasChild.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.50000");

        BigDecimal gammaHasParentIsFatherOf = topDownSimReasonerImpl.gamma(ROLE_HAS_PARENT, ROLE_IS_FATHER_OF);
        assertThat(gammaHasParentIsFatherOf.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal gammaIsParentOfHasParent = topDownSimReasonerImpl.gamma(ROLE_IS_FATHER_OF, ROLE_HAS_PARENT);
        assertThat(gammaIsParentOfHasParent.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testEHd() {
        Tree<Set<String>> existSonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_EXISTENTIAL_SON);
        Tree<Set<String>> existGrandfatherTree = treeBuilder.constructAccordingToManchesterSyntax("Grandfather", MANCHESTER_EXISTENTIAL_GRANDFATHER);
        BigDecimal ehd = topDownSimReasonerImpl.eHd(existSonTree.getNodes().get(0).getChildren().get(0), existGrandfatherTree.getNodes().get(0).getChildren().get(0));
        assertThat(ehd.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testESetHd() {
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        Tree<Set<String>> grandfatherTree = treeBuilder.constructAccordingToManchesterSyntax("Grandfather", MANCHESTER_CONCEPT_GRANDFATHER);
        BigDecimal eSetHdSonGrandfather = topDownSimReasonerImpl.eSetHd(sonTree.getNodes().get(0), grandfatherTree.getNodes().get(0));
        assertThat(eSetHdSonGrandfather.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son in Law", MANCHESTER_CONCEPT_SON_IN_LAW);
        BigDecimal eSetHdSonSonInLaw = topDownSimReasonerImpl.eSetHd(sonTree.getNodes().get(0), sonInLawTree.getNodes().get(0));
        assertThat(eSetHdSonSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal eSetHdSonInLawSon = topDownSimReasonerImpl.eSetHd(sonInLawTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(eSetHdSonInLawSon.setScale(5, BigDecimal.ROUND_HALF_UP)).isEqualTo("0.70000");

        Tree<Set<String>> topTree = treeBuilder.constructAccordingToManchesterSyntax("Top", "owl:Thing");
        BigDecimal eSetHdTopSon = topDownSimReasonerImpl.eSetHd(topTree.getNodes().get(0), sonTree.getNodes().get(0));
        assertThat(eSetHdTopSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTManchesterSyntax() {
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", MANCHESTER_CONCEPT_SON);
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son in Law", MANCHESTER_CONCEPT_SON_IN_LAW);
        BigDecimal simSonSonInLawManchester = topDownSimReasonerImpl.measureDirectedSimilarity(sonTree, sonInLawTree);
        assertThat(simSonSonInLawManchester.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonInLawSon = topDownSimReasonerImpl.measureDirectedSimilarity(sonInLawTree, sonTree);
        assertThat(simSonInLawSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.94000");

        Tree<Set<String>> manTree =treeBuilder.constructAccordingToManchesterSyntax("Man", MANCHESTER_CONCEPT_MAN);
        BigDecimal simManSon = topDownSimReasonerImpl.measureDirectedSimilarity(manTree, sonTree);
        assertThat(simManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonMan = topDownSimReasonerImpl.measureDirectedSimilarity(sonTree, manTree);
        assertThat(simSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.80000");

        Tree<Set<String>> femaleTree = treeBuilder.constructAccordingToManchesterSyntax("Female", MANCHESTER_CONCEPT_FEMALE);
        Tree<Set<String>> personTree = treeBuilder.constructAccordingToManchesterSyntax("Person", MANCHESTER_CONCEPT_PERSON);
        BigDecimal simFemalePerson = topDownSimReasonerImpl.measureDirectedSimilarity(femaleTree, personTree);
        assertThat(simFemalePerson.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal simPersonFemale = topDownSimReasonerImpl.measureDirectedSimilarity(personTree, femaleTree);
        assertThat(simPersonFemale.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTKRSSSyntax() {
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToKRSSSyntax("Son", KRSS_CONCEPT_SON);
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToKRSSSyntax("Son in Law", KRSS_CONCEPT_SON_IN_LAW);
        BigDecimal simSonSonInLawManchester = topDownSimReasonerImpl.measureDirectedSimilarity(sonTree, sonInLawTree);
        assertThat(simSonSonInLawManchester.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonInLawSon = topDownSimReasonerImpl.measureDirectedSimilarity(sonInLawTree, sonTree);
        assertThat(simSonInLawSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.94000");

        Tree<Set<String>> manTree =treeBuilder.constructAccordingToKRSSSyntax("Man", KRSS_CONCEPT_MAN);
        BigDecimal simManSon = topDownSimReasonerImpl.measureDirectedSimilarity(manTree, sonTree);
        assertThat(simManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonMan = topDownSimReasonerImpl.measureDirectedSimilarity(sonTree, manTree);
        assertThat(simSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.80000");

        Tree<Set<String>> femaleTree = treeBuilder.constructAccordingToKRSSSyntax("Female", KRSS_CONCEPT_FEMALE);
        Tree<Set<String>> personTree = treeBuilder.constructAccordingToKRSSSyntax("Person", KRSS_CONCEPT_PERSON);
        BigDecimal simFemalePerson = topDownSimReasonerImpl.measureDirectedSimilarity(femaleTree, personTree);
        assertThat(simFemalePerson.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal simPersonFemale = topDownSimReasonerImpl.measureDirectedSimilarity(personTree, femaleTree);
        assertThat(simPersonFemale.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }
}
