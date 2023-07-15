package io.github.xlives.util.syntaxanalyzer;

import io.github.xlives.util.ParserUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserHandlerTests {

    private static final String ROLE_1 = "isSiblingOf";

    private ParserHandlerImpl parserHandler;

    private HandlerContextImpl handlerContext;

    @Before
    public void init() {
        this.parserHandler = new ParserHandlerImpl();
        this.handlerContext = new HandlerContextImpl();
    }

    @After
    public void clear() {
        this.parserHandler = null;
        this.handlerContext.clear();
    }

    @Test
    public void testConvertToRoleForm() {
        String roleForm = ParserUtils.convertToRoleForm(ROLE_1);
        assertThat(roleForm).isEqualTo("<" + ROLE_1 + ">");
    }

}
