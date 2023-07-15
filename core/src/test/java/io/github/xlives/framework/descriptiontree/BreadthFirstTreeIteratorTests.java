package io.github.xlives.framework.descriptiontree;


import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BreadthFirstTreeIteratorTests {

    @Test
    public void testBuildList() {

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

        Iterator<TreeNode<String>> breadthFirstTreeIterator = tree.iterator(0);

        LinkedList<String> checkedList = new LinkedList<String>();

        checkedList.add("Harry");
        checkedList.add("Jane");
        checkedList.add("Bill");
        checkedList.add("Joe");
        checkedList.add("Diane");
        checkedList.add("Mark");
        checkedList.add("Grace");
        checkedList.add("George");
        checkedList.add("Mary");
        checkedList.add("Jill");
        checkedList.add("Carol");

        while (breadthFirstTreeIterator.hasNext()) {
            TreeNode<String> item = breadthFirstTreeIterator.next();
            String check = checkedList.poll();
            assertThat(item.toString().equals(check));
        }
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

        Set<String> node2Str = new HashSet<String>();
        node2Str.add("node2 1");
        node2Str.add("ndoe2 2");
        TreeNode<Set<String>> node2 = tree.addNode("", node1, node2Str);

        Set<String> node3Str = new HashSet<String>();
        node3Str.add("node3 1");
        node3Str.add("node3 2");
        TreeNode<Set<String>> node3 = tree.addNode("", node1, node3Str);

        Set<String> node4Str = new HashSet<String>();
        node4Str.add("node4 1");
        node4Str.add("node4 2");
        TreeNode<Set<String>> node4 = tree.addNode("", node2, node4Str);

        Iterator<TreeNode<Set<String>>> breadthFirstTreeIterator = tree.iterator(0);

        LinkedList<Set<String>> checkedList = new LinkedList<Set<String>>();
        checkedList.add(rootSet);
        checkedList.add(node1Str);
        checkedList.add(node2Str);
        checkedList.add(node3Str);
        checkedList.add(node4Str);

        while (breadthFirstTreeIterator.hasNext()) {
            TreeNode<Set<String>> item = breadthFirstTreeIterator.next();
            Set<String> check = checkedList.poll();
            assertThat(item.getData()).isEqualTo(check);
        }
    }

    @Test
    public void testNodesOnEachLevel() {
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
        node2Str.add("ndoe2 2");
        TreeNode<Set<String>> node2 = tree.addNode("", node1, node2Str);

        Set<String> node3Str = new HashSet<String>();
        node3Str.add("node3 1");
        node3Str.add("node3 2");
        TreeNode<Set<String>> node3 = tree.addNode("", node1, node3Str);

        Set<String> node4Str = new HashSet<String>();
        node4Str.add("node4 1");
        node4Str.add("node4 2");
        TreeNode<Set<String>> node4 = tree.addNode("", node2, node4Str);

        BreadthFirstTreeIterator<Set<String>> breadthFirstTreeIterator = (BreadthFirstTreeIterator<Set<String>>) tree.iterator(0);

        Map<Integer, List<TreeNode<Set<String>>>> nodesOnEachLevel = breadthFirstTreeIterator.getNodesOnEachLevel();
        assertThat(nodesOnEachLevel.size()).isEqualTo(4);

        LinkedList<Set<String>> checkedList = new LinkedList<Set<String>>();
        checkedList.add(rootSet);
        checkedList.add(node1Str);
        checkedList.add(node2Str);
        checkedList.add(node3Str);
        checkedList.add(node4Str);

        while (breadthFirstTreeIterator.hasNext()) {
            TreeNode<Set<String>> item = breadthFirstTreeIterator.next();
            Set<String> check = checkedList.poll();
            assertThat(item.getData()).isEqualTo(check);
        }
    }
}
