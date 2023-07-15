package io.github.xlives.framework.unfolding;

import io.github.xlives.framework.OWLServiceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SuperRoleUnfolderManchesterSyntax.class, OWLServiceContext.class})
public class SuperRoleUnfolderManchesterSyntaxTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String ROLE_NAME_1 = "hasSon";
    private static final String ROLE_NAME_2 = "hasBrother";

    @Autowired
    private SuperRoleUnfolderManchesterSyntax superRoleUnfolderManchesterSyntax;

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Before
    public void init() {
        OWLServiceContext.init(OWL_FILE_PATH);
    }

    @Test
    public void testUnfoldRoleHierarchy() {
        Set<String> roleSet1 =  superRoleUnfolderManchesterSyntax.unfoldRoleHierarchy(ROLE_NAME_1);
        assertThat(roleSet1).containsOnly("hasSon'", "hasChild");

        Set<String> roleSet2 =  superRoleUnfolderManchesterSyntax.unfoldRoleHierarchy(ROLE_NAME_2);
        assertThat(roleSet2).containsOnly("hasBrother");
    }
}
