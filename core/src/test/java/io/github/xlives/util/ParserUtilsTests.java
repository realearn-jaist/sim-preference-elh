package io.github.xlives.util;

import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserUtilsTests {

    private static final String CONCEPT_1 = "( Female' and ( Sex' and Thing ) ) and Person";
    private static final String INVALID_CONCEPT_1 = "( Female' and ( Sex' and Thing )  and Person";

    @Test
    public void testCompactConceptDescriptionString() {
        String str = ParserUtils.compactConceptDescriptionString(CONCEPT_1);
        assertThat(str).isEqualTo("(Female' and (Sex' and Thing)) and Person");
    }

    @Test
    public void testGetLastMatchedCloseParenthesis() {
        int position = ParserUtils.getLastMatchedCloseParenthesis(CONCEPT_1);
        assertThat(position).isEqualTo(33);

        int invalidPosition = ParserUtils.getLastMatchedCloseParenthesis(INVALID_CONCEPT_1);
        assertThat(invalidPosition).isEqualTo(-1);
    }

    @Test
    public void testGenerateFreshNameIfIllegalArguments() {
        try {
            String freshName = ParserUtils.generateFreshName(null);
            assertThat(freshName).isNull();
        }

        catch(JSimPiException e) {
            ErrorCode errorCode = e.getErrorCode();
            assertThat(errorCode.getCode()).isEqualTo("ParserUtils_IllegalArguments");
        }
    }
}
