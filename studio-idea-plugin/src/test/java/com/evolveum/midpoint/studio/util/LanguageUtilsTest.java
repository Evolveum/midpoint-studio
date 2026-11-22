package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.StudioPlatformTestCase;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * Tests for LanguageUtils.
 */
public class LanguageUtilsTest extends StudioPlatformTestCase {

    public void testPrismLanguageForSupportedLanguages() {
        assertEquals(PrismContext.LANG_XML, LanguageUtils.toPrismLanguage(XMLLanguage.INSTANCE));
        assertEquals(PrismContext.LANG_JSON, LanguageUtils.toPrismLanguage(JsonLanguage.INSTANCE));
        assertEquals(PrismContext.LANG_YAML, LanguageUtils.toPrismLanguage(YAMLLanguage.INSTANCE));
    }

    public void testPrismLanguageForPlainTextIsNull() {
        assertNull(LanguageUtils.toPrismLanguage(PlainTextLanguage.INSTANCE));
        assertNull(LanguageUtils.toPrismLanguage(null));
    }

    public void testDetectLanguageFallsBackToPlainText() {
        assertSame(PlainTextLanguage.INSTANCE, LanguageUtils.detectLanguage("   \n"));
        assertSame(PlainTextLanguage.INSTANCE, LanguageUtils.detectLanguage("hello world"));
    }

    public void testDetectLanguageRecognizesStructuredInput() {
        assertSame(XMLLanguage.INSTANCE, LanguageUtils.detectLanguage("  <user/>"));
        assertSame(JsonLanguage.INSTANCE, LanguageUtils.detectLanguage("{\"user\": {}}"));
        assertSame(YAMLLanguage.INSTANCE, LanguageUtils.detectLanguage("user:\n  name: x"));
    }
}
