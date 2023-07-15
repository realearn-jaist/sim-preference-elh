package io.github.xlives.util;

import io.github.xlives.enumeration.OWLDocumentFormat;
import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OWLOntologyUtilTests {

    private static final IRI DOCUMENT_IRI = IRI.create("file:family.owl");

    private static final String CLASS_NAME_1 = "Woman";
    private static final String CLASS_NAME_2 = "Female";

    private static final String INVALID_CLASS_NAME_1 = "Wom";

    private static final String VALID_FRESH_CLASS_NAME_1 = "Sex'";

    private static final String INVALID_FRESH_CLASS_NAME_1 = "Sex''";
    private static final String INVALID_FRESH_CLASS_NAME_2 = "Wom'";

    private static final String ROLE_NAME_1 = "hasAuntInLaw";
    private static final String ROLE_NAME_2 = "hasDaughter";

    private static final String INVALID_ROLE_NAME_1 = "has";

    private static final String VALID_FRESH_ROLE_NAME_1 = "hasDaughter'";

    private static final String INVALID_FRESH_ROLE_NAME_1 = "hasDaughter''";
    private static final String INVALID_FRESH_ROLE_NAME_2 = "has'";

    private OWLOntology owlOntology;
    private OWLOntologyManager owlOntologyManager;
    private OWLDataFactory owlDataFactory;

    @Before
    public void init() throws OWLOntologyCreationException {
        this.owlOntologyManager = OWLManager.createOWLOntologyManager();
        this.owlOntology = this.owlOntologyManager.loadOntologyFromOntologyDocument(DOCUMENT_IRI);
        this.owlDataFactory = owlOntologyManager.getOWLDataFactory();
    }

    @After
    public void clear() {
        this.owlOntologyManager = null;
        this.owlOntology = null;
        this.owlDataFactory = null;
    }

    @Test
    public void testContainClassNameIfValidName() {
        OWLClass owlClass1 = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
        boolean class1Exist = OWLOntologyUtil.containClassName(owlOntology, owlClass1);
        assertThat(class1Exist).isTrue();
    }

    @Test
    public void testContainClassNameIfInvalidName() {
        OWLClass owlClass1 = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, INVALID_CLASS_NAME_1);
        boolean class1Exist = OWLOntologyUtil.containClassName(owlOntology, owlClass1);
        assertThat(class1Exist).isFalse();
    }

    @Test
    public void testContainObjectPropertyNameIfValidName() {
        OWLObjectProperty owlObjectProperty1 = OWLOntologyUtil.getOWLObjectProperty(owlDataFactory, owlOntologyManager, owlOntology, ROLE_NAME_1);
        boolean property1Exist = OWLOntologyUtil.containObjectPropertyName(owlOntology, owlObjectProperty1);
        assertThat(property1Exist).isTrue();
    }

    @Test
    public void testContainObjectPropertyNameIfInValidName() {
        OWLObjectProperty owlObjectProperty1 = OWLOntologyUtil.getOWLObjectProperty(owlDataFactory, owlOntologyManager, owlOntology, INVALID_ROLE_NAME_1);
        boolean property1Exist1 = OWLOntologyUtil.containObjectPropertyName(owlOntology, owlObjectProperty1);
        assertThat(property1Exist1).isFalse();
    }

    @Test
    public void testConvertAsOWLDocumentFormat() {
        this.owlOntology = OWLOntologyUtil.convertAsOWLDocumentFormat(owlOntologyManager, owlOntology, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

        OWLOntologyFormat owlOntologyFormat = this.owlOntologyManager.getOntologyFormat(this.owlOntology);
        assertThat(owlOntologyFormat.toString()).contains(OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT.toString());
    }

    @Test
    public void testConvertAsOWLDocumentFormatIfIllegalArguments() {
        try {
            this.owlOntology = OWLOntologyUtil.convertAsOWLDocumentFormat(null, null, null);
            assertThat(this.owlOntology).isNull();
        }

        catch (JSimPiException e) {
            ErrorCode errorCode = e.getErrorCode();
            assertThat(errorCode.getCode()).isEqualTo("OWLOntologyUtil_IllegalArguments");
        }
    }

    @Test
    public void testGenerateFreshName() {
        String freshName = ParserUtils.generateFreshName(CLASS_NAME_2);
        assertThat(freshName).isEqualTo(CLASS_NAME_2 + "'");
    }

    @Test
    public void testGetDescriptionLogicName() {
        String dlName = OWLOntologyUtil.getDescriptionLogicName(owlOntologyManager, owlOntology);

        assertThat(dlName).contains("ALEH");
    }

    @Test
    public void testGetDescriptionLogicNameIfIllegalArguments() {
        try {
            String dlName = OWLOntologyUtil.getDescriptionLogicName(null, null);
            assertThat(dlName).isNull();
        }

        catch (JSimPiException e) {
            ErrorCode errorCode = e.getErrorCode();
            assertThat(errorCode.getCode()).isEqualTo("OWLOntologyUtil_IllegalArguments");
        }
    }

    @Test
    public void testGetPrimitiveConceptDefinition() {
        OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
        String womanDefinition = OWLOntologyUtil.getPrimitiveConceptDefinition(owlOntology, owlClass, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

        assertThat(womanDefinition).isNull();

        owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_2);
        String femaleDefinition = OWLOntologyUtil.getPrimitiveConceptDefinition(owlOntology, owlClass, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

        assertThat(femaleDefinition).isEqualTo("Sex");
    }

    @Test
    public void testGetPrimitiveConceptDefinitionIfIllegalArguments() {
        try {
            OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_2);
            String femaleDefinition = OWLOntologyUtil.getPrimitiveConceptDefinition(null, owlClass, null);

            assertThat(femaleDefinition).isNull();
        }

        catch(JSimPiException e) {
            ErrorCode errorCode = e.getErrorCode();
            assertThat(errorCode.getCode()).isEqualTo("OWLOntologyUtil_IllegalArguments");
        }
    }

    @Test
    public void testGetFullConceptDefinition() {
        OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
        String womanDefinition = OWLOntologyUtil.getFullConceptDefinition(owlOntology, owlClass, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

        assertThat(womanDefinition).isEqualTo("Female and Person");
    }

    @Test
    public void testGetFullConceptDefinitionIfIllegalArguments() {
        try {
            OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
            String womanDefinition = OWLOntologyUtil.getFullConceptDefinition(null, owlClass, null);

            assertThat(womanDefinition).isNull();
        }

        catch(JSimPiException e) {
            ErrorCode errorCode = e.getErrorCode();
            assertThat(errorCode.getCode()).isEqualTo("OWLOntologyUtil_IllegalArguments");
        }
    }

    @Test
    public void testGetOWLClass() {
        OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
        assertThat(owlClass.toString()).contains(CLASS_NAME_1);
    }

    @Test
    public void testGetOWLObjectProperty() {
        OWLObjectProperty owlObjectProperty1 = OWLOntologyUtil.getOWLObjectProperty(owlDataFactory, owlOntologyManager, owlOntology, ROLE_NAME_1);
        assertThat(owlObjectProperty1.toString()).contains(ROLE_NAME_1);

        OWLObjectProperty owlObjectProperty2 = OWLOntologyUtil.getOWLObjectProperty(owlDataFactory, owlOntologyManager, owlOntology, ROLE_NAME_2);
        assertThat(owlObjectProperty2.toString()).contains(ROLE_NAME_2);
    }

    @Test
    public void testIsValidFreshConceptNameIfValid() {
        boolean concept1 = OWLOntologyUtil.isValidFreshConceptName(owlDataFactory, owlOntologyManager, owlOntology, VALID_FRESH_CLASS_NAME_1);
        assertThat(concept1).isTrue();
    }

    @Test
    public void testIsValidFreshConceptNameIfInvalid() {
        boolean concept1 = OWLOntologyUtil.isValidFreshConceptName(owlDataFactory, owlOntologyManager, owlOntology, INVALID_FRESH_CLASS_NAME_1);
        assertThat(concept1).isFalse();

        boolean concept2 = OWLOntologyUtil.isValidFreshConceptName(owlDataFactory, owlOntologyManager, owlOntology, INVALID_FRESH_CLASS_NAME_2);
        assertThat(concept2).isFalse();
    }

    @Test
    public void testIsValidFreshRoleNameIfValid() {
        boolean role1 = OWLOntologyUtil.isValidFreshRoleName(owlDataFactory, owlOntologyManager, owlOntology, VALID_FRESH_ROLE_NAME_1);
        assertThat(role1).isTrue();
    }

    @Test
    public void testIsValidFreshRoleNameIfInvalid() {
        boolean role1 = OWLOntologyUtil.isValidFreshRoleName(owlDataFactory, owlOntologyManager, owlOntology, INVALID_FRESH_ROLE_NAME_1);
        assertThat(role1).isFalse();

        boolean role2 = OWLOntologyUtil.isValidFreshRoleName(owlDataFactory, owlOntologyManager, owlOntology, INVALID_FRESH_ROLE_NAME_2);
        assertThat(role2).isFalse();
    }
}
