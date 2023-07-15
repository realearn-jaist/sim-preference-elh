package io.github.xlives.framework.unfolding;

import io.github.xlives.enumeration.OWLConstant;
import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.framework.OWLServiceContext;
import io.github.xlives.util.OWLOntologyUtil;
import io.github.xlives.util.ParserUtils;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component("superRoleUnfolderManchesterSyntax")
public class SuperRoleUnfolderManchesterSyntax implements IRoleUnfolder {

    @Autowired
    private OWLServiceContext OWLServiceContext;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(OWLObjectProperty owlObjectProperty, Set<String> roles) {
        Set<OWLObjectPropertyExpression> owlObjectPropertyExpressions = owlObjectProperty.getSuperProperties(OWLServiceContext.getOwlOntology());

        // When a role has no defined hierarchy.
        if (owlObjectPropertyExpressions.isEmpty()) {
            roles.add(owlObjectProperty.getIRI().getFragment());
        }

        else {
            roles.add(ParserUtils.generateFreshName(owlObjectProperty.getIRI().getFragment()));

            for (OWLObjectPropertyExpression propertyExpression : owlObjectPropertyExpressions) {
                OWLObjectProperty superObjectProperty = propertyExpression.asOWLObjectProperty();

                unfold(superObjectProperty, roles);
            }
        }

        return roles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold role hierarchy as roleName is null.", ErrorCode.SuperRoleUnfolderManchesterSyntax_IllegalArguments);
        }

        Set<String> roles = new HashSet<String>();
        if (roleName.equals(OWLConstant.TOP_ROLE.getOwlSyntax())) {
            return roles;
        }

        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), roleName);

        return unfold(owlObjectProperty, roles);
    }
}
