package io.github.xlives.framework.unfolding;

import org.springframework.stereotype.Component;

@Component
public interface IConceptUnfolder {

    String unfoldConceptDefinitionString(String conceptName);
}
