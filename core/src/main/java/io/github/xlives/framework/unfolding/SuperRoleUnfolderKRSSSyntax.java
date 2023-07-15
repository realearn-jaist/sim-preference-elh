package io.github.xlives.framework.unfolding;

import io.github.xlives.enumeration.KRSSConstant;
import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.framework.KRSSServiceContext;
import io.github.xlives.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import io.github.xlives.util.syntaxanalyzer.HandlerContextImpl;
import io.github.xlives.util.syntaxanalyzer.KRSSHandlerContextImpl;
import io.github.xlives.util.syntaxanalyzer.krss.KRSSConceptSetHandler;
import io.github.xlives.util.syntaxanalyzer.krss.KRSSTopLevelParserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component("superRoleUnfolderKRSSSyntax")
public class SuperRoleUnfolderKRSSSyntax implements IRoleUnfolder {

    @Autowired
    private KRSSServiceContext krssServiceContext;

    private Map<String, String> fullRoleDefinitionMap;
    private Map<String, String> primitiveRoleDefinitionMap;

    private ChainOfResponsibilityHandler<HandlerContextImpl> superRoleHandlerChain;

    @PostConstruct
    private void init() {
        superRoleHandlerChain = new KRSSTopLevelParserHandler()
                .setNextHandler(new KRSSConceptSetHandler()
                );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(String role, Set<String> superRoles) {

        String roleDescription;

        if (this.fullRoleDefinitionMap.containsKey(role)) {
            roleDescription = this.fullRoleDefinitionMap.get(role);
        }

        else if (this.primitiveRoleDefinitionMap.containsKey(role)) {
            roleDescription = this.primitiveRoleDefinitionMap.get(role);
        }

        else {
            roleDescription = role;
        }

        KRSSHandlerContextImpl context = new KRSSHandlerContextImpl();
        context.setConceptDescription(roleDescription);
        superRoleHandlerChain.invoke(context);

        Set<String> roleSet = context.getPrimitiveConceptSet();

        if (roleSet.size() == 1) {
            superRoles.add(role);
        }

        else {
            for (String roleName : roleSet) {
                unfold(roleName, superRoles);
            }
        }

        return superRoles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold role hierarchy due to roleName is null.", ErrorCode.SuperRoleUnfolderKRSSSyntax_IllegalArguments);
        }

        this.fullRoleDefinitionMap = krssServiceContext.getFullRoleDefinitionMap();
        this.primitiveRoleDefinitionMap = krssServiceContext.getPrimitiveRoleDefinitionMap();

        Set<String> roles = new HashSet<String>();
        if (roleName.equals(KRSSConstant.TOP_ROLE.getStr())) {
            return roles;
        }

        return unfold(roleName, roles);
    }
}
