package io.github.xlives.framework;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(KRSSServiceContext.class)
public class KRSSServiceContextTests {

    private static final String KRSS_FILE_PATH = "snomed.krss";

    private static final String FULL_CONCEPT_1 = "(define-concept 1001000'\n" +
            "    (and 58985002 2742009\n" +
            "      (some roleGroup (and\n" +
            "        (some 260686004 129392009)\n" +
            "        (some 363704007 87176006)))))";

    private static final String PRIMITIVE_CONCEPT_1 = "(define-primitive-concept 10007009'\n" +
            "    (and 91138005 88425004 82354003\n" +
            "      (some roleGroup (and\n" +
            "        (some 363698007 12738006)\n" +
            "        (some 116676008 112635002)))\n" +
            "      (some roleGroup (some 246454002 255399007))))";

    private static final String FULL_ROLE_1 = "(define-role relative' top)";

    private static final String PRIMITIVE_ROLE_1 = "(define-primitive-role 363701004' top :right-identity 127489000)";

    @Autowired
    private KRSSServiceContext krssServiceContext;

    @Test
    public void testInstantiateAFullConceptDefinition() {
        boolean isFullConcept1 = krssServiceContext.instantiateAFullConceptDefinition(FULL_CONCEPT_1);
        assertThat(isFullConcept1).isTrue();
        Map<String, String> fullConceptDefinitionMap = krssServiceContext.getFullConceptDefinitionMap();
        assertThat(fullConceptDefinitionMap.containsKey("1001000'")).isTrue();
        assertThat(fullConceptDefinitionMap.get("1001000'")).isEqualTo("(and 58985002 2742009 (some roleGroup (and " +
                "(some 260686004 129392009) (some 363704007 87176006))))");
    }

    @Test
    public void testInstantiateAPrimitiveConceptDefinition() {
        boolean isPrimitiveConcept1 = krssServiceContext.instantiateAPrimitiveConceptDefinition(PRIMITIVE_CONCEPT_1);
        assertThat(isPrimitiveConcept1).isTrue();
        Map<String, String> primitiveConceptDefinitionMap = krssServiceContext.getPrimitiveConceptDefinitionMap();
        assertThat(primitiveConceptDefinitionMap.containsKey("10007009'")).isTrue();
        assertThat(primitiveConceptDefinitionMap.get("10007009'")).isEqualTo("(and 10007009'' (and 91138005 88425004 82354003 " +
                "(some roleGroup (and (some 363698007 12738006) (some 116676008 112635002))) " +
                "(some roleGroup (some 246454002 255399007))))");
    }

    @Test
    public void testInstantiateAFullRoleDefinition() {
        boolean isFullRole1 = krssServiceContext.instantiateAFullRoleDefinition(FULL_ROLE_1);
        assertThat(isFullRole1).isTrue();
        Map<String, String> fullRoleDefinitionMap = krssServiceContext.getFullRoleDefinitionMap();
        assertThat(fullRoleDefinitionMap.containsKey("relative'")).isTrue();
        assertThat(fullRoleDefinitionMap.get("relative'")).isEqualTo("top");
    }

    @Test
    public void testInstantiateAPrimitiveRoleDefinition() {
        boolean isPrimitiveRole1 = krssServiceContext.instantiateAPrimitiveRoleDefinition(PRIMITIVE_ROLE_1);
        assertThat(isPrimitiveRole1).isTrue();
        Map<String, String> primitiveRoleDefinitionMap = krssServiceContext.getPrimitiveRoleDefinitionMap();
        assertThat(primitiveRoleDefinitionMap.containsKey("363701004'")).isTrue();
        assertThat(primitiveRoleDefinitionMap.get("363701004'")).isEqualTo("(and 363701004'' top :right-identity 127489000)");
    }

    @Test
    public void testReadKRSSFile() {
        boolean isRead = krssServiceContext.readKRSSFile(KRSS_FILE_PATH);
        assertThat(isRead).isTrue();
        Map<String, String> fullConceptDefinitionMap = krssServiceContext.getFullConceptDefinitionMap();
        Map<String, String> fullRoleDefinitionMap = krssServiceContext.getFullRoleDefinitionMap();
        Map<String, String> primitiveConceptDefinitionMap = krssServiceContext.getPrimitiveConceptDefinitionMap();
        Map<String, String> primitiveRoleDefinitionMap = krssServiceContext.getPrimitiveRoleDefinitionMap();
        assertThat(fullConceptDefinitionMap.size()).isPositive();
        assertThat(fullRoleDefinitionMap.size()).isZero();
        assertThat(primitiveConceptDefinitionMap.size()).isPositive();
        assertThat(primitiveRoleDefinitionMap.size()).isPositive();
    }
}
