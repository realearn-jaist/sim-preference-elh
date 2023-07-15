package io.github.xlives.util.syntaxanalyzer.krss;

import io.github.xlives.util.syntaxanalyzer.HandlerContextImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {KRSSConceptSetHandler.class, HandlerContextImpl.class})
public class ConceptSetHandlerTests {

    private static final String TOP_LEVEL_1 = "78917001 <roleGroup> <roleGroup>";
    private static final String TOP_LEVEL_2 = "22252005 257836008 <roleGroup>";

    @Autowired
    private KRSSConceptSetHandler concseptSetHandler;

    @Autowired
    private HandlerContextImpl handlerContext;


    @Test
    public void testInvoke() {
        this.handlerContext.setTopLevelDescription(TOP_LEVEL_1);
        concseptSetHandler.invoke(handlerContext);

        Set<String> concepts = this.handlerContext.getPrimitiveConceptSet();
        assertThat(concepts).containsOnly("78917001");
        handlerContext.clear();

        this.handlerContext.setTopLevelDescription(TOP_LEVEL_2);
        concseptSetHandler.invoke(handlerContext);

        concepts = this.handlerContext.getPrimitiveConceptSet();
        assertThat(concepts).containsOnly("22252005", "257836008");
    }
}
