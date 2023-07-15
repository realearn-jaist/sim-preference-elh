package io.github.xlives.util.syntaxanalyzer.manchester;

import io.github.xlives.exception.JSimPiException;
import io.github.xlives.util.syntaxanalyzer.HandlerContextImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ManchesterTopLevelParserHandler.class})
public class TopLevelParserHandlerTests {

    private static final String CONCEPT_CASE_0 = "Female";

    private static final String CONCEPT_CASE_1 = "Female' and Sex and Person and isSiblingOf some (Person and (isParentOf some Person))";
    private static final String CONCEPT_CASE_2 = "Person and (isParentOf some Person1) and (isParentOf some Person2)";
    private static final String CONCEPT_CASE_3 = "((Female' and (Sex' and Thing)) and Person) and (isSiblingOf some (Person and (isParentOf some Person)))";
    private static final String CONCEPT_CASE_4 = "( Female' and ( Sex' and Thing ) ) and Person";
    private static final String CONCEPT_CASE_5 = "Person and isParentOf some Person1 and isParentOf some Person2";
    private static final String CONCEPT_CASE_6 = "Person and isParentOf somePerson";
    private static final String CONCEPT_CASE_7 = "Female' and Sex' and Thing and Person";

    @Autowired
    private ManchesterTopLevelParserHandler manchesterTopLevelParserHandler;

    private HandlerContextImpl handlerContext;

    @Before
    public void init() {
        this.handlerContext = new HandlerContextImpl();
    }

    @After
    public void clear() {
        this.handlerContext.clear();
    }

    @Test
    public void testReturnTopLevelConceptStringIfExisted() {
        String concept1 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_1);
        assertThat(concept1).isEqualTo("Female' and Sex and Person and <isSiblingOf>");
        clear();

        String concept2 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_2);
        assertThat(concept2).isEqualTo("Person and <isParentOf> and (isParentOf some Person2)");
        clear();

        String concept3 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_3);
        assertThat(concept3).isEqualTo("(Female' and (Sex' and Thing)) and Person and (isSiblingOf some (Person and (isParentOf some Person)))");

        String concept4 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_4);
        assertThat(concept4).isEqualTo("Female' and (Sex' and Thing) and Person");
        clear();

        String concept5 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_5);
        assertThat(concept5).isEqualTo("Person and <isParentOf> and isParentOf some Person2");
        clear();

        try {
            manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_6);
        }

        catch (JSimPiException e) {
            assertThat(e.toString()).contains("Unable to return top level concept string if available as compactFormat has incorrect syntax.");
        }

        String concept7 = manchesterTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_CASE_7);
        assertThat(concept7).isNull();
    }

    @Test
    public void testInvoke() {
        this.handlerContext.setConceptDescription(CONCEPT_CASE_3);
        manchesterTopLevelParserHandler.invoke(handlerContext);

        String topLevel = handlerContext.getTopLevelDescription();
        assertThat(topLevel).isEqualTo("Female' and Sex' and Thing and Person and <isSiblingOf>");

        Map<String, Set<String>> edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(1);

        Set<String> nestedConcepts = edges.get("isSiblingOf");
        assertThat(nestedConcepts).containsOnly("Person and (isParentOf some Person)");
        clear();

        this.handlerContext.setConceptDescription(CONCEPT_CASE_5);
        manchesterTopLevelParserHandler.invoke(handlerContext);

        topLevel = handlerContext.getTopLevelDescription();

        assertThat(topLevel).isEqualTo("Person and <isParentOf> and <isParentOf>");

        edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(1);

        nestedConcepts = edges.get("isParentOf");
        assertThat(nestedConcepts).containsOnly("Person1", "Person2");
        clear();

        this.handlerContext.setConceptDescription(CONCEPT_CASE_0);
        manchesterTopLevelParserHandler.invoke(handlerContext);

        topLevel = handlerContext.getTopLevelDescription();

        assertThat(topLevel).isEqualTo("Female");

        edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(0);
    }

    @Test
    public void testStoreRoleAndNestedConceptPair() {
        String role = manchesterTopLevelParserHandler.storeRoleAndNestedConceptPair(this.handlerContext, "isParentOf some Person\"");
        assertThat(role).isEqualTo("isParentOf");
        Map<String, Set<String>> edgeConceptsMap = this.handlerContext.getEdgePrimitiveConceptExistentialMap();
        Set<String> concepts = edgeConceptsMap.get("isParentOf");
        assertThat(concepts.size()).isEqualTo(1);
    }
}
