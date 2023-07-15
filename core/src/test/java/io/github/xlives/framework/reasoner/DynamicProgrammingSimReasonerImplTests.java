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
@SpringApplicationConfiguration(classes = {DynamicProgrammingSimReasonerImpl.class, ConceptDefinitionUnfolderKRSSSyntax.class,
        SuperRoleUnfolderManchesterSyntax.class, KRSSServiceContext.class,
        OWLServiceContext.class, TreeBuilder.class})
public class DynamicProgrammingSimReasonerImplTests {

    private static final String OWL_FILE_PATH = "family.owl";


    private static final String CONCEPT_FEMALE = "Female' and (Sex' and Thing)";
    private static final String CONCEPT_MAN = "(Male' and (Sex' and Thing)) and Person";
    private static final String CONCEPT_PERSON = "Person";
    private static final String CONCEPT_SON = "((Male' and (Sex' and Thing)) and Person) and (hasParent some Person)";
    private static final String CONCEPT_SON_IN_LAW = "((Male' and (Sex' and Thing)) and Person) and (hasParent some (Person and (isSpouseOf some Person)))";

    private static final String KRSS_CONCEPT_FEMALE = "(and Female' (and Sex' Thing))";
    private static final String KRSS_CONCEPT_PERSON = "Person";
    private static final String KRSS_CONCEPT_MAN = "(and (and Male' (and Sex' Thing)) Person)";
    private static final String KRSS_CONCEPT_SON = "(and (and (and Male' (and Sex' Thing)) Person) (some hasParent Person))";
    private static final String KRSS_CONCEPT_SON_IN_LAW = "(and (and (and Male' (and Sex' Thing)) Person) (some hasParent (and Person (some isSpouseOf Person))))";

    @Autowired
    private TreeBuilder treeBuilder;

    @Autowired

    private DynamicProgrammingSimReasonerImpl dynamicProgrammingSimReasoner;

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Before
    public void init() {
        this.OWLServiceContext.init(OWL_FILE_PATH);
    }

    @Test
    public void testMeasureDirectedSimilarityWRTManchesterSyntax() {
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToManchesterSyntax("Son", CONCEPT_SON);
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToManchesterSyntax("Son in Law", CONCEPT_SON_IN_LAW);
        BigDecimal simSonSonInLaw = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonTree, sonInLawTree);
        assertThat(simSonSonInLaw.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonInLawSon = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonInLawTree, sonTree);
        assertThat(simSonInLawSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.94000");

        Tree<Set<String>> manTree =treeBuilder.constructAccordingToManchesterSyntax("Man", CONCEPT_MAN);
        BigDecimal simManSon = dynamicProgrammingSimReasoner.measureDirectedSimilarity(manTree, sonTree);
        assertThat(simManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonMan = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonTree, manTree);
        assertThat(simSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.80000");

        Tree<Set<String>> femaleTree = treeBuilder.constructAccordingToManchesterSyntax("Female", CONCEPT_FEMALE);
        Tree<Set<String>> personTree = treeBuilder.constructAccordingToManchesterSyntax("Person", CONCEPT_PERSON);
        BigDecimal simFemalePerson = dynamicProgrammingSimReasoner.measureDirectedSimilarity(femaleTree, personTree);
        assertThat(simFemalePerson.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal simPersonFemale = dynamicProgrammingSimReasoner.measureDirectedSimilarity(personTree, femaleTree);
        assertThat(simPersonFemale.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }

    @Test
    public void testMeasureDirectedSimilarityWRTKRSSSyntax() {
        Tree<Set<String>> sonTree = treeBuilder.constructAccordingToKRSSSyntax("Son", KRSS_CONCEPT_SON);
        Tree<Set<String>> sonInLawTree = treeBuilder.constructAccordingToKRSSSyntax("Son in Law", KRSS_CONCEPT_SON_IN_LAW);
        BigDecimal simSonSonInLawManchester = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonTree, sonInLawTree);
        assertThat(simSonSonInLawManchester.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonInLawSon = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonInLawTree, sonTree);
        assertThat(simSonInLawSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.94000");

        Tree<Set<String>> manTree =treeBuilder.constructAccordingToKRSSSyntax("Man", KRSS_CONCEPT_MAN);
        BigDecimal simManSon = dynamicProgrammingSimReasoner.measureDirectedSimilarity(manTree, sonTree);
        assertThat(simManSon.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("1.00000");

        BigDecimal simSonMan = dynamicProgrammingSimReasoner.measureDirectedSimilarity(sonTree, manTree);
        assertThat(simSonMan.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.80000");

        Tree<Set<String>> femaleTree = treeBuilder.constructAccordingToKRSSSyntax("Female", KRSS_CONCEPT_FEMALE);
        Tree<Set<String>> personTree = treeBuilder.constructAccordingToKRSSSyntax("Person", KRSS_CONCEPT_PERSON);
        BigDecimal simFemalePerson = dynamicProgrammingSimReasoner.measureDirectedSimilarity(femaleTree, personTree);
        assertThat(simFemalePerson.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");

        BigDecimal simPersonFemale = dynamicProgrammingSimReasoner.measureDirectedSimilarity(personTree, femaleTree);
        assertThat(simPersonFemale.setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString()).isEqualTo("0.00000");
    }
}
