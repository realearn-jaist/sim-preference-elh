package io.github.xlives.framework;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.OutputCapture;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(OWLServiceContext.class)
public class ServiceContextTests {

    private static final String OWL_FILE_PATH = "family.owl";

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Test
    public void testInit() {
        this.OWLServiceContext.init(OWL_FILE_PATH);
        String output = this.outputCapture.toString();
        assertThat(output).contains("Loaded");
    }
}
