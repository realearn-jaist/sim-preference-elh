package io.github.xlives.framework.descriptiontree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeNodeTests {

    private TreeNode<String> node;

    @Before
    public void init() {
        node = new TreeNode<String>(null, "Test", 0);
    }

    @After
    public void clear() {
        node = null;
    }

    @Test
    public void testTreeNodeConstructor() {
        assertThat(node.getData()).isEqualTo("Test");
    }

    @Test
    public void testAddChildWithString() {
        TreeNode<String> node = new TreeNode<String>(null, "Test", 0);

        TreeNode<String> firstChild = node.addChild("", "First Child", 1);
        assertThat(node.getChildren()).contains(firstChild);

        TreeNode<String> secondChild = node.addChild("", "Second Child", 2);
        assertThat(node.getChildren()).contains(secondChild);

        assertThat(node.getChildren().size()).isEqualTo(2);
    }

    @Test
    public void testAddChildWithSetOfString() {
        Set<String> rootStr = new HashSet<String>();
        rootStr.add("root1 1");
        TreeNode<Set<String>> root = new TreeNode<Set<String>>(null, rootStr, 0);

        Set<String> node1Str = new HashSet<String>();
        node1Str.add("node1 1");
        node1Str.add("node1 2");
        TreeNode<Set<String>> node1 = root.addChild("", node1Str, 1);

        assertThat(node1.getData()).containsOnly("node1 1", "node1 2");
    }
}
