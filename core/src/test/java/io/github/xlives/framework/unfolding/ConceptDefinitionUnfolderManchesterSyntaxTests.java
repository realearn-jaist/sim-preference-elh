package io.github.xlives.framework.unfolding;

import io.github.xlives.enumeration.OWLConstant;
import io.github.xlives.framework.OWLServiceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ConceptDefinitionUnfolderManchesterSyntax.class, OWLServiceContext.class})
public class ConceptDefinitionUnfolderManchesterSyntaxTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String CLASS_NAME_1 = "Woman";
    private static final String CLASS_NAME_2 = "Man";
    private static final String CLASS_NAME_3 = "AuntInLaw";

    @Autowired
    private ConceptDefinitionUnfolderManchesterSyntax conceptDefinitionUnfolderManchesterSyntax;

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Before
    public void init() {
        OWLServiceContext.init(OWL_FILE_PATH);
    }

    @Test
    public void testUnfoldConceptDefinitionStringIfEnteredTopConcept() {
        String top1 = conceptDefinitionUnfolderManchesterSyntax.unfoldConceptDefinitionString(OWLConstant.TOP_CONCEPT_1.getOwlSyntax());
        assertThat(top1).isEqualTo(OWLConstant.TOP_CONCEPT_1.getOwlSyntax());

        String top2 = conceptDefinitionUnfolderManchesterSyntax.unfoldConceptDefinitionString(OWLConstant.TOP_CONCEPT_2.getOwlSyntax());
        assertThat(top2).isEqualTo(OWLConstant.TOP_CONCEPT_2.getOwlSyntax());
    }

    @Test
    public void testUnfoldConceptDefinitionString() {
        String woman = conceptDefinitionUnfolderManchesterSyntax.unfoldConceptDefinitionString(CLASS_NAME_1);
        assertThat(woman).isEqualTo("(Female' and (Sex' and Thing)) and Person");

        String man = conceptDefinitionUnfolderManchesterSyntax.unfoldConceptDefinitionString(CLASS_NAME_2);
        assertThat(man).isEqualTo("(Male' and (Sex' and Thing)) and Person");

        String auntInLaw = conceptDefinitionUnfolderManchesterSyntax.unfoldConceptDefinitionString(CLASS_NAME_3);
        assertThat(auntInLaw).isEqualTo("((Female' and (Sex' and Thing)) and Person) and " +
                "(isWifeOf some (((Male' and (Sex' and Thing)) and Person) and " +
                "(isSiblingOf some (Person and (isParentOf some Person)))))");
    }
}
