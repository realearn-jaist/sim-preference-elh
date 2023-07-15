package io.github.xlives.util.syntaxanalyzer.krss;

import io.github.xlives.exception.ErrorCode;
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
@SpringApplicationConfiguration(classes = {KRSSTopLevelParserHandler.class})
public class TopLevelParserHandlerTests {

    private static final String CONCEPT_1 = "(and 105592009 87628006)";

    private static final String CONCEPT_2 = "(and 78917001\n" +
            "      (some roleGroup (some 261583007 367561004))\n" +
            "      (some roleGroup (some 405815000 367561004)))";
    private static final String CONCEPT_3 = "78917001 (some roleGroup (some 261583007 367561004)) (some roleGroup (some 405815000 367561004))";

    private static final String CONCEPT_4 = "(and 22252005 257836008\n" +
            "      (some roleGroup (and\n" +
            "        (some 260686004 257867005)\n" +
            "        (some 363699004 8451008)\n" +
            "        (some 261583007 31031000)\n" +
            "        (some 363704007 87342007))))";

    private static final String CONCEPT_5 = "(and (and (and Male' (and Sex' Thing)) Person) (some isFatherOf (and Person (some isParentOf Person))))";

    private static final String INVALID_CONCEPT = "78917001 (someroleGroup (some 261583007 367561004)) (some roleGroup (some 405815000 367561004))";

    @Autowired
    private KRSSTopLevelParserHandler KRSSTopLevelParserHandler;

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
        String concept1 = KRSSTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_1);
        assertThat(concept1).isEqualTo("105592009 87628006");
        clear();

        String concept2 = KRSSTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_2);
        assertThat(concept2).isEqualTo("78917001 (some roleGroup (some 261583007 367561004)) (some roleGroup (some 405815000 367561004))");
        clear();

        String concept3 = KRSSTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_3);
        assertThat(concept3).isEqualTo("78917001 <roleGroup> (some roleGroup (some 405815000 367561004))");
        clear();

        String concept5 = KRSSTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, CONCEPT_5);
        assertThat(concept5).isEqualTo("(and (and Male' (and Sex' Thing)) Person) (some isFatherOf (and Person (some isParentOf Person)))");
        clear();
    }

    @Test
    public void testReturnTopLevelConceptStringIfInvalidSyntax() {
        try {
            KRSSTopLevelParserHandler.returnTopLevelConceptStringIfAvailable(this.handlerContext, INVALID_CONCEPT);
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.KrssTopLevelParserHandler_InvalidSyntaxException);
        }
    }

    @Test
    public void testInvoke() {
        this.handlerContext.setConceptDescription(CONCEPT_1);
        KRSSTopLevelParserHandler.invoke(handlerContext);

        String topLevel = handlerContext.getTopLevelDescription();
        assertThat(topLevel).isEqualTo("105592009 87628006");

        Map<String, Set<String>> edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(0);
        clear();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        this.handlerContext.setConceptDescription(CONCEPT_2);
        KRSSTopLevelParserHandler.invoke(handlerContext);

        topLevel = handlerContext.getTopLevelDescription();

        assertThat(topLevel).isEqualTo("78917001 <roleGroup> <roleGroup>");

        edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(1);

        Set<String> nestedConcepts = edges.get("roleGroup");
        assertThat(nestedConcepts).containsOnly("(some 261583007 367561004)", "(some 405815000 367561004)");
        clear();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        this.handlerContext.setConceptDescription(CONCEPT_4);
        KRSSTopLevelParserHandler.invoke(handlerContext);

        topLevel = handlerContext.getTopLevelDescription();

        assertThat(topLevel).isEqualTo("22252005 257836008 <roleGroup>");

        edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(1);

        nestedConcepts = edges.get("roleGroup");
        assertThat(nestedConcepts).containsOnly("(and (some 260686004 257867005) (some 363699004 8451008) (some 261583007 31031000) (some 363704007 87342007))");
        clear();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        this.handlerContext.setConceptDescription(CONCEPT_5);
        KRSSTopLevelParserHandler.invoke(handlerContext);

        topLevel = handlerContext.getTopLevelDescription();

        assertThat(topLevel).isEqualTo("Male' Sex' Thing Person <isFatherOf>");

        edges = handlerContext.getEdgePrimitiveConceptExistentialMap();
        assertThat(edges.size()).isEqualTo(1);

        nestedConcepts = edges.get("isFatherOf");
        assertThat(nestedConcepts).containsOnly("(and Person (some isParentOf Person))");
        clear();
    }

}
