package io.github.xlives.framework.descriptiontree;

import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import io.github.xlives.util.syntaxanalyzer.HandlerContextImpl;
import io.github.xlives.util.syntaxanalyzer.krss.KRSSConceptSetHandler;
import io.github.xlives.util.syntaxanalyzer.krss.KRSSTopLevelParserHandler;
import io.github.xlives.util.syntaxanalyzer.manchester.ManchesterConceptSetHandler;
import io.github.xlives.util.syntaxanalyzer.manchester.ManchesterTopLevelParserHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TreeBuilder {

    private ChainOfResponsibilityHandler<HandlerContextImpl> krssHandlerChain;
    private ChainOfResponsibilityHandler<HandlerContextImpl> manchesterHandlerChain;

    @PostConstruct
    public void init() {
        manchesterHandlerChain = new ManchesterTopLevelParserHandler()
                .setNextHandler(new ManchesterConceptSetHandler()
                );

        krssHandlerChain = new KRSSTopLevelParserHandler()
                .setNextHandler(new KRSSConceptSetHandler()
                );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void constructSubTreeWithKrssSyntax(HandlerContextImpl context, Tree<Set<String>> tree, String edge, TreeNode<Set<String>> parentNode, String nestedPrimitiveStr) {

        context.clear();
        context.setConceptDescription(nestedPrimitiveStr);
        krssHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> edgesTop = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());

        TreeNode<Set<String>> child = tree.addNode(edge, parentNode, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : edgesTop.entrySet()) {

            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, nestedEdge, child, nestedConcept);
            }
        }
    }

    private void constructSubTreeWithManchesterSyntax(HandlerContextImpl context, Tree<Set<String>> tree, String edge, TreeNode<Set<String>> parentNode, String nestedPrimitiveStr) {

        context.clear();
        context.setConceptDescription(nestedPrimitiveStr);
        manchesterHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> edgesTop = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());

        TreeNode<Set<String>> child = tree.addNode(edge, parentNode, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : edgesTop.entrySet()) {

            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, nestedEdge, child, nestedConcept);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Tree<Set<String>> constructAccordingToKRSSSyntax(String treeLabel, String concept) {
        if (treeLabel == null || concept == null) {
            throw new JSimPiException("Unable to construct according to krss syntax as treeLabel[" + treeLabel + "] and concept["
                    + concept + "] are null.", ErrorCode.TreeBuilder_IllegalArguments);
        }

        // Invoke business logic
        HandlerContextImpl context = new HandlerContextImpl();
        context.setConceptDescription(concept);
        krssHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> edgesTop = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());

        Tree<Set<String>> tree = new Tree<Set<String>>(treeLabel);

        // Initiate the root
        TreeNode<Set<String>> parent = tree.addNode(null, null, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : edgesTop.entrySet()) {

            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, edge, parent, primitiveSet);
            }
        }

        return tree;
    }

    public Tree<Set<String>> constructAccordingToManchesterSyntax(String treeLabel, String concept) {
        if (treeLabel == null || concept == null) {
            throw new JSimPiException("Unable to construct according to manchester syntax as treeLabel[" + treeLabel + "] and concept["
                    + concept + "] are null.", ErrorCode.TreeBuilder_IllegalArguments);
        }

        // Invoke business logic
        HandlerContextImpl context = new HandlerContextImpl();
        context.setConceptDescription(concept);
        manchesterHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> edgesTop = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());

        Tree<Set<String>> tree = new Tree<Set<String>>(treeLabel);

        // Initiate the root
        TreeNode<Set<String>> parent = tree.addNode(null, null, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : edgesTop.entrySet()) {

            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, edge, parent, primitiveSet);
            }
        }

        return tree;
    }
}
