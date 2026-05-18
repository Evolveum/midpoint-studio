package com.evolveum.midpoint.studio.lang.mel;

import com.evolveum.midpoint.studio.lang.mel.impl.MelFileType;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.PsiErrorElementUtil;

public class MelParserTest extends BasePlatformTestCase {

    // ── helpers ───────────────────────────────────────────────────────────────────

    private PsiFile parse(String text) {
        return myFixture.configureByText(MelFileType.INSTANCE, text);
    }

    private boolean hasErrors(String text) {
        PsiFile file = parse(text);
        return PsiErrorElementUtil.hasErrors(getProject(), file.getVirtualFile());
    }

    private int errorCount(String text) {
        PsiFile file = parse(text);
        return PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class).size();
    }

    // ── valid expressions — must produce zero PsiErrorElements ────────────────────

    public void testValidSimpleIdentifier() {
        assertFalse(hasErrors("foo"));
    }

    public void testValidIntegerLiteral() {
        assertFalse(hasErrors("42"));
    }

    public void testValidFloatLiteral() {
        assertFalse(hasErrors("3.14"));
    }

    public void testValidStringLiteral() {
        assertFalse(hasErrors("\"hello\""));
    }

    public void testValidBoolLiterals() {
        assertFalse(hasErrors("true"));
        assertFalse(hasErrors("false"));
        assertFalse(hasErrors("null"));
    }

    public void testValidMemberAccess() {
        assertFalse(hasErrors("request.auth.claims"));
    }

    public void testValidBracketAccess() {
        assertFalse(hasErrors("request.auth.claims[\"email\"]"));
    }

    public void testValidBracketAccessWithSpaces() {
        // regression: whitespace around brackets must not cause parse errors
        assertFalse(hasErrors("request.auth.claims[ \"email\" ]"));
    }

    public void testValidArithmetic() {
        assertFalse(hasErrors("1 + 2 * 3"));
    }

    public void testValidComparison() {
        assertFalse(hasErrors("a == b"));
    }

    public void testValidLogicalAnd() {
        assertFalse(hasErrors("a && b"));
    }

    public void testValidLogicalOr() {
        assertFalse(hasErrors("a || b"));
    }

    public void testValidTernary() {
        assertFalse(hasErrors("a ? b : c"));
    }

    public void testValidNegation() {
        assertFalse(hasErrors("!flag"));
    }

    public void testValidFunctionCall() {
        assertFalse(hasErrors("size(list)"));
    }

    public void testValidNestedExpression() {
        assertFalse(hasErrors("(a + b) * c"));
    }

    public void testValidExpressionWithComment() {
        // comment on hidden channel must not break parsing
        assertFalse(hasErrors("foo // this is fine"));
    }

    public void testValidComplexExpression() {
        assertFalse(hasErrors("request.auth.claims[\"role\"] == \"admin\" && request.time > 0"));
    }

    // ── invalid expressions — must produce at least one PsiErrorElement ───────────

    public void testInvalidDanglingPlus() {
        assertTrue("Expected parse error for 'a +'", hasErrors("a +"));
    }

    public void testInvalidTwoIdentifiersInARow() {
        // parser expects EOF after first expr, finds another IDENTIFIER
        assertTrue("Expected parse error for 'foo bar'", hasErrors("foo bar"));
    }

    public void testInvalidUnclosedParenthesis() {
        assertTrue("Expected parse error for '(a + b'", hasErrors("(a + b"));
    }

    public void testInvalidUnclosedBracket() {
        String s = "a[\"key\"]";
        assertTrue("Expected parse error for 'a[\"key\"'", hasErrors(s.substring(0, s.length() - 1)));
    }

    public void testInvalidBadCharIsHiddenFromParser() {
        assertTrue("';' should not be silently swallowed by parser mode", hasErrors("foo;"));
    }
}