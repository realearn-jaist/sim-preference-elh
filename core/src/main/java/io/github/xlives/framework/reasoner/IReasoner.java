package io.github.xlives.framework.reasoner;

import io.github.xlives.framework.descriptiontree.Tree;
import io.github.xlives.framework.unfolding.IRoleUnfolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface IReasoner {

    BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2);

    void setRoleUnfoldingStrategy(IRoleUnfolder iRoleUnfolder);

    List<String> getExecutionTimes();

}
