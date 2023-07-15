package io.github.xlives.framework.descriptiontree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TreeBuilder.class})
public class TreeBuilderTests {

    private static final String KRSS_CONCEPT_1 = "(and 22252005 257836008\n" +
            "      (some roleGroup (and\n" +
            "        (some 260686004 257867005)\n" +
            "        (some 363699004 8451008)\n" +
            "        (some 261583007 31031000)\n" +
            "        (some 363704007 87342007))))";

    private static final String KRSS_CONCEPT_2 = "(and 78917001\n" +
            "      (some roleGroup (some 261583007 367561004))\n" +
            "      (some roleGroup (some 405815000 367561004)))";

    private static final String MANCHESTER_CONCEPT_1 = "Female' and Sex and Person and isSiblingOf some (Person and (isParentOf some Person))";
    private static final String MANCHESTER_CONCEPT_2 = "Female' and Sex and Person1 and isSiblingOf some (Male' and Sex and Person2 and (isParentOf some (Male' and Sex and Person3)))";
    private static final String MANCHESTER_CONCEPT_3 = "owl:Thing";

    @Autowired
    private TreeBuilder treeBuilder;

    @Test
    public void testConstructAccordingToKrssSyntax() {
        Tree<Set<String>> tree1 = treeBuilder.constructAccordingToKRSSSyntax("Concept 1", KRSS_CONCEPT_1);

        StringBuilder expected1 = new StringBuilder("\n");
        expected1.append("{22252005 257836008}\n");
        expected1.append("\t<roleGroup>{}\n");
        expected1.append("\t\t<260686004>{257867005}\n");
        expected1.append("\t\t<261583007>{31031000}\n");
        expected1.append("\t\t<363704007>{87342007}\n");
        expected1.append("\t\t<363699004>{8451008}\n");

        assertThat(tree1.toString(0)).isEqualTo(expected1.toString());

        Tree<Set<String>> tree2 = treeBuilder.constructAccordingToKRSSSyntax("Concept 1", KRSS_CONCEPT_2);

        StringBuilder expected2 = new StringBuilder("\n");
        expected2.append("{78917001}\n");
        expected2.append("\t<roleGroup>{}\n");
        expected2.append("\t\t<261583007>{367561004}\n");
        expected2.append("\t<roleGroup>{}\n");
        expected2.append("\t\t<405815000>{367561004}\n");

        assertThat(tree2.toString(0)).isEqualTo(expected2.toString());
    }

    @Test
    public void testConstructAccordingToManchesterSyntax() {
        Tree<Set<String>> tree1 = treeBuilder.constructAccordingToManchesterSyntax("Concept 1", MANCHESTER_CONCEPT_1);

        StringBuilder expected1a = new StringBuilder("\n");
        expected1a.append("{Female' Sex Person}\n");
        expected1a.append("\t<isSiblingOf>{Person}\n");
        expected1a.append("\t\t<isParentOf>{Person}\n");

        StringBuilder expected1b = new StringBuilder("\n");
        expected1b.append("{Sex Person Female'}\n");
        expected1b.append("\t<isSiblingOf>{Person}\n");
        expected1b.append("\t\t<isParentOf>{Person}\n");

        StringBuilder expected1c = new StringBuilder("\n");
        expected1c.append("{Person Female' Sex}\n");
        expected1c.append("\t<isSiblingOf>{Person}\n");
        expected1c.append("\t\t<isParentOf>{Person}\n");

        assertThat(tree1.toString(0)).isIn(expected1a.toString(), expected1b.toString(), expected1c.toString());

        Tree<Set<String>> tree2 = treeBuilder.constructAccordingToManchesterSyntax("Woman Tree", MANCHESTER_CONCEPT_2);

        StringBuilder expected2a = new StringBuilder("\n");
        expected2a.append("{Female' Person1 Sex}\n");
        expected2a.append("\t<isSiblingOf>{Male' Person2 Sex}\n");
        expected2a.append("\t\t<isParentOf>{Male' Person3 Sex}\n");

        StringBuilder expected2b = new StringBuilder("\n");
        expected2b.append("{Person1 Sex Female'}\n");
        expected2b.append("\t<isSiblingOf>{Male' Person2 Sex}\n");
        expected2b.append("\t\t<isParentOf>{Male' Person3 Sex}\n");

        StringBuilder expected2c = new StringBuilder("\n");
        expected2c.append("{Sex Female' Person1}\n");
        expected2c.append("\t<isSiblingOf>{Male' Person2 Sex}\n");
        expected2c.append("\t\t<isParentOf>{Male' Person3 Sex}\n");

        StringBuilder expected2d = new StringBuilder("\n");
        expected2d.append("{Female' Person1 Sex}\n");
        expected2d.append("\t<isSiblingOf>{Person2 Sex Male'}\n");
        expected2d.append("\t\t<isParentOf>{Male' Person3 Sex}\n");

        StringBuilder expected2e = new StringBuilder("\n");
        expected2e.append("{Female' Person1 Sex}\n");
        expected2e.append("\t<isSiblingOf>{Sex Male' Person2}\n");
        expected2e.append("\t\t<isParentOf>{Male' Person3 Sex}\n");

        StringBuilder expected2f = new StringBuilder("\n");
        expected2f.append("{Female' Person1 Sex}\n");
        expected2f.append("\t<isSiblingOf>{Sex Male' Person2}\n");
        expected2f.append("\t\t<isParentOf>{Person3 Sex Male'}\n");

        StringBuilder expected2g = new StringBuilder("\n");
        expected2g.append("{Female' Person1 Sex}\n");
        expected2g.append("\t<isSiblingOf>{Sex Male' Person2}\n");
        expected2g.append("\t\t<isParentOf>{Sex Male' Person3}\n");

        assertThat(tree2.toString(0)).isIn(expected2a.toString(), expected2b.toString(),
                expected2c.toString(), expected2d.toString(), expected2e.toString(),
                expected2f.toString(), expected2g.toString());

        Tree<Set<String>> tree3 = treeBuilder.constructAccordingToManchesterSyntax("Top", MANCHESTER_CONCEPT_3);
        assertThat(tree3.toString(0)).isEqualTo("\n{}\n");
    }
}
