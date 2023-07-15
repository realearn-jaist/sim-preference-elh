package io.github.xlives.service;

import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.util.OWLOntologyUtil;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ValidationService {

    @Autowired
    private OWLServiceContext owlServiceContext;

    public boolean validateIfOWLClassNamesExist(String... conceptNames) {
        if (conceptNames == null) {
            return false;
        }

        else {
            for (String conceptName : conceptNames) {
                OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlServiceContext.getOwlDataFactory(), owlServiceContext.getOwlOntologyManager(), owlServiceContext.getOwlOntology(), conceptName);

                if (!OWLOntologyUtil.containClassName(owlServiceContext.getOwlOntology(), owlClass)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateIfLatestOWLFile(String owlFilePath) {
        if (owlFilePath == null) {
            return false;
        }

        if (owlServiceContext.getOwlFile() == null) {
            return false;
        }

        LastModifiedFileComparator lastModifiedFileComparator = new LastModifiedFileComparator();
        if (lastModifiedFileComparator.compare(new File(owlFilePath), owlServiceContext.getOwlFile()) > 0) {
            return true;
        }

        return false;
    }
}
