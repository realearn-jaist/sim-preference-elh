package io.github.xlives.util.syntaxanalyzer.manchester;

import io.github.xlives.enumeration.OWLConstant;
import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import io.github.xlives.util.syntaxanalyzer.Handler;
import io.github.xlives.util.syntaxanalyzer.HandlerContextImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManchesterConceptSetHandler extends Handler {

    private static final Logger logger = LoggerFactory.getLogger(ManchesterConceptSetHandler.class);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke concept set handler as context is null.", ErrorCode.ManchesterConceptSetHandler_IllegalArguments);
        };

        if (logger.isDebugEnabled()) {
            logger.debug("ManchesterConceptSetHandler" +
                    " - context topLevelDescription[" + context.getTopLevelDescription() + "]");
        }

        if (context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_1.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_2.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_3.getOwlSyntax())) {
            // Do nothing
        }

        else {
            String[] elements = StringUtils.splitByWholeSeparator(context.getTopLevelDescription(), "and");

            if (logger.isDebugEnabled()) {
                logger.debug("ManchesterConceptSetHandler - elements length[" + elements.length + "]");
            }

            for (String element : elements) {

                if (!StringUtils.containsAny(element, '<', '>') && StringUtils.isNotBlank(element)) {
                    context.addToPrimitiveConceptSet(StringUtils.trim(element));
                }
            }
        }

        ChainOfResponsibilityHandler nextHandler = getNextHandler();
        if (nextHandler != null) {
            nextHandler.invoke(context);
        }
    }
}
