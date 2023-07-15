package io.github.xlives.framework.descriptiontree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeTests {

    @Test
    public void testToString() {
        Tree<String> tree = new Tree<String>("Sample Tree");

        TreeNode<String> harry = tree.addNode(null, null, "Harry");
        TreeNode<String> jane = tree.addNode("", harry, "Jane");
        TreeNode<String> bill = tree.addNode("", harry, "Bill");
        TreeNode<String> joe = tree.addNode("", jane, "Joe");
        TreeNode<String> diane = tree.addNode("", jane, "Diane");
        TreeNode<String> george = tree.addNode("", diane, "George");
        TreeNode<String> mary = tree.addNode("", diane, "Mary");
        TreeNode<String> jill = tree.addNode("", george, "Jill");
        TreeNode<String> carol = tree.addNode("", jill, "Carol");
        TreeNode<String> grace = tree.addNode("", bill, "Grace");
        TreeNode<String> mark = tree.addNode("", jane, "Mark");

        StringBuilder expect1 = new StringBuilder();
        expect1.append("\nHarry\n");
        expect1.append("\t<>Jane\n");
        expect1.append("\t\t<>Joe\n");
        expect1.append("\t\t<>Diane\n");
        expect1.append("\t\t\t<>George\n");
        expect1.append("\t\t\t\t<>Jill\n");
        expect1.append("\t\t\t\t\t<>Carol\n");
        expect1.append("\t\t\t<>Mary\n");
        expect1.append("\t\t<>Mark\n");
        expect1.append("\t<>Bill\n");
        expect1.append("\t\t<>Grace\n");
        assertThat(tree.toString(harry.getId())).isEqualTo(expect1.toString());
    }

    @Test
    public void testAddNodeWithSetOfString() {
        Tree<Set<String>> tree = new Tree<Set<String>>("");

        Set<String> rootSet = new HashSet<String>();
        rootSet.add("root1");
        rootSet.add("root2");
        TreeNode<Set<String>> root = tree.addNode("", null, rootSet);

        Set<String> node1Str = new HashSet<String>();
        node1Str.add("node1 1");
        node1Str.add("node1 2");
        TreeNode<Set<String>> node1 = tree.addNode("", root, node1Str);

        assertThat(tree.getNodes().get(0).getData()).containsOnly("root1", "root2");
    }

    @Test
    public void testGetNodes() {
        Tree<Set<String>> tree = new Tree<Set<String>>("");

        Set<String> rootSet = new HashSet<String>();
        rootSet.add("root1");
        rootSet.add("root2");
        TreeNode<Set<String>> root = tree.addNode("", null, rootSet);

        Set<String> node1Str = new HashSet<String>();
        node1Str.add("node1 1");
        node1Str.add("node1 2");
        TreeNode<Set<String>> node1 = tree.addNode("", root, node1Str);

        Set<String> node2Str = new HashSet<String>();
        node2Str.add("node2 1");
        node2Str.add("node2 2");
        TreeNode<Set<String>> node2 = tree.addNode("", node1, node2Str);

        Map<Integer, TreeNode<Set<String>>> integerTreeNodeMap = tree.getNodes();
        assertThat(integerTreeNodeMap.get(0).getData()).containsOnly("root1", "root2");
        assertThat(integerTreeNodeMap.get(1).getData()).containsOnly("node1 1", "node1 2");
        assertThat(integerTreeNodeMap.get(2).getData()).containsOnly("node2 1", "node2 2");
    }
}
