package io.github.xlives.framework.unfolding;

import io.github.xlives.framework.KRSSServiceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ConceptDefinitionUnfolderKRSSSyntax.class, KRSSServiceContext.class})
public class ConceptDefinitionUnfolderKRSSSyntaxTests {

    @Mock
    private KRSSServiceContext krssServiceContext;

    @InjectMocks
    private ConceptDefinitionUnfolderKRSSSyntax conceptDefinitionUnfolderKRSSSyntax;

    private Map<String, String> fullConceptDefinitionMap = new HashMap<String, String>();
    private Map<String, String> primitiveConceptDefinitionMap = new HashMap<String, String>();

    @Before
    public void init() {

        // Populate full concept definition map1
        this.fullConceptDefinitionMap.put("Man", "(and Male Person)");
        this.fullConceptDefinitionMap.put("Grandfather", "(and Man (some isFatherOf (and Person (some isParentOf Person))))");

        // Populate primitive concept definition map1
        this.primitiveConceptDefinitionMap.put("Male", "(and Male' Sex)");
        this.primitiveConceptDefinitionMap.put("Sex", "(and Sex' Thing)");
        this.primitiveConceptDefinitionMap.put("Female", "(and Female' Sex)");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUnfoldConceptDefinitionString() {
        when(krssServiceContext.getFullConceptDefinitionMap()).thenReturn(fullConceptDefinitionMap);
        when(krssServiceContext.getPrimitiveConceptDefinitionMap()).thenReturn(primitiveConceptDefinitionMap);

        assertThat(conceptDefinitionUnfolderKRSSSyntax.unfoldConceptDefinitionString("Man"))
                .isEqualTo("(and (and Male' (and Sex' Thing)) Person)");
        assertThat(conceptDefinitionUnfolderKRSSSyntax.unfoldConceptDefinitionString("Grandfather"))
                .isEqualTo("(and (and (and Male' (and Sex' Thing)) Person) (some isFatherOf (and Person (some isParentOf Person))))");
    }
}
