package io.github.xlives.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OWLConceptDefinitionUtilTests {

    private static final IRI DOCUMENT_IRI = IRI.create("file:family.owl");

    private static final String CLASS_NAME_1 = "Woman";
    private static final String CLASS_NAME_2 = "Female";

    private static final String INVALIDE_CLASS_NAME_1 = "Wamon";

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
    public void testGenerateFullConceptDefinitionIfAlreadyFullDefinition() {
        String definition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_1);
        assertThat(definition).isEqualTo("Female and Person");
    }

    @Test
    public void testGenerateFullConceptDefinitionIfPrimitiveDefinition() {
        String definition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(owlDataFactory, owlOntologyManager, owlOntology, CLASS_NAME_2);
        assertThat(definition).isEqualTo("Female' and Sex");
    }

    @Test
    public void testGenerateFullConceptDefinitionIfUndefinedConceptName() {
        String definition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(owlDataFactory, owlOntologyManager, owlOntology, INVALIDE_CLASS_NAME_1);
        assertThat(definition).isNull();
    }
}
