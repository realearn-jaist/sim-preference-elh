package io.github.xlives.service;

import io.github.xlives.controller.KRSSSimilarityController;
import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.framework.PreferenceProfile;
import io.github.xlives.framework.descriptiontree.TreeBuilder;
import io.github.xlives.framework.reasoner.DynamicProgrammingSimPiReasonerImpl;
import io.github.xlives.framework.reasoner.DynamicProgrammingSimReasonerImpl;
import io.github.xlives.framework.reasoner.TopDownSimPiReasonerImpl;
import io.github.xlives.framework.reasoner.TopDownSimReasonerImpl;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.ConceptDefinitionUnfolderManchesterSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderKRSSSyntax;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {KRSSServiceContext.class})
public class KRSSServiceContextTests {

    @Autowired
    private KRSSServiceContext krssServiceContext;

    @Before
    public void init() {
        krssServiceContext.init("snomed.krss");
    }

    @Test
    public void testFullConceptDefinitionMap() {
        Map<String, String> map = krssServiceContext.getFullConceptDefinitionMap();
        assertThat(map.get("10001005")).isEqualTo("(and 105592009 87628006)");
        assertThat(map.get("105592009")).isNull();
        assertThat(map.get("87628006")).isEqualTo("(and 40733004 (some roleGroup (some 246075003 41146007)) (some roleGroup (some 47429007 41146007)))");
    }

    @Test
    public void testPrimitiveConceptDefinitionMap() {
        Map<String, String> map = krssServiceContext.getPrimitiveConceptDefinitionMap();
        assertThat(map.get("246075003")).isEqualTo("(and 246075003' 47429007)");
    }
}
