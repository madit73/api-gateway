package com.madi.common.utility.rest.security;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanitizeUtilTest
{

    // ========== sanitizeAllowCommonFormatWithLinks Tests ==========

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldAllowValidLinks()
    {
        String input = "<a href=\"https://example.com\" title=\"Example\">Click here</a>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(input);
        assertEquals("<a href=\"https://example.com\" title=\"Example\">Click here</a>", result);
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldAllowCommonInlineFormatting()
    {
        String input = "<b>Bold</b> <i>Italic</i> <strong>Strong</strong> <em>Emphasis</em>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(input);
        assertTrue(result.contains("Bold"));
        assertTrue(result.contains("Italic"));
        assertTrue(result.contains("Strong"));
        assertTrue(result.contains("Emphasis"));
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldRemoveScriptTags()
    {
        String input = "<script>alert('XSS')</script>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(input);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert"));
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldRemoveJavascriptProtocol()
    {
        String input = "<a href=\"javascript:alert('XSS')\">Click</a>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(input);
        assertFalse(result.contains("javascript:"));
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(null);
        assertNull(result);
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldHandleEmptyString()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks("");
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowCommonFormatWithLinks_shouldRemoveDisallowedAttributes()
    {
        String input = "<a href=\"https://example.com\" onclick=\"alert('XSS')\">Click</a>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(input);
        assertFalse(result.contains("onclick"));
    }

    // ========== sanitizeAllowCommonFormat Tests ==========

    @Test
    void sanitizeAllowCommonFormat_shouldAllowInlineFormatting()
    {
        String input = "<b>Bold</b> <i>Italic</i> <u>Underline</u>";
        String result = SanitizeUtil.sanitizeAllowCommonFormat(input);
        assertTrue(result.contains("Bold"));
        assertTrue(result.contains("Italic"));
        assertTrue(result.contains("Underline"));
    }

    @Test
    void sanitizeAllowCommonFormat_shouldRemoveLinks()
    {
        String input = "<a href=\"https://example.com\">Link</a>";
        String result = SanitizeUtil.sanitizeAllowCommonFormat(input);
        assertFalse(result.contains("<a"));
        assertTrue(result.contains("Link"));
    }

    @Test
    void sanitizeAllowCommonFormat_shouldRemoveScriptTags()
    {
        String input = "<script>alert('XSS')</script>Safe text";
        String result = SanitizeUtil.sanitizeAllowCommonFormat(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("Safe text"));
    }

    @Test
    void sanitizeAllowCommonFormat_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormat(null);
        assertNull(result);
    }

    // ========== sanitizeAllowUrls Tests ==========

    @Test
    void sanitizeAllowUrls_shouldReturnPlainText()
    {
        String input = "https://example.com";
        String result = SanitizeUtil.sanitizeAllowUrls(input);
        assertEquals("https://example.com", result);
    }

    @Test
    void sanitizeAllowUrls_shouldRemoveHtmlElements()
    {
        String input = "<a href=\"https://example.com\">Link</a>";
        String result = SanitizeUtil.sanitizeAllowUrls(input);
        assertFalse(result.contains("<a"));
        assertTrue(result.contains("Link"));
    }

    @Test
    void sanitizeAllowUrls_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowUrls(null);
        assertNull(result);
    }

    @Test
    void sanitizeAllowUrls_shouldStripScriptTags()
    {
        String input = "<script>alert('XSS')</script>Text";
        String result = SanitizeUtil.sanitizeAllowUrls(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("Text"));
    }

    // ========== sanitizeAllowCommonFormatIgnoreChars Tests ==========

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldReplaceEncodedEquals()
    {
        String input = "key&#61;value";
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars(input);
        assertEquals("key=value", result);
    }

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldReplaceEncodedQuotes()
    {
        String input = "&#34;quoted&#34;";
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars(input);
        assertEquals("\"quoted\"", result);
    }

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars(null);
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldHandleEmptyString()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars("");
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldHandleWhitespaceOnly()
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars("   ");
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowCommonFormatIgnoreChars_shouldAllowFormattingAndReplaceChars()
    {
        String input = "<b>key&#61;&#34;value&#34;</b>";
        String result = SanitizeUtil.sanitizeAllowCommonFormatIgnoreChars(input);
        assertTrue(result.contains("key=\"value\""));
    }

    // ========== sanitizeAllowEqual Tests ==========

    @Test
    void sanitizeAllowEqual_shouldReplaceEncodedEquals()
    {
        String input = "key&#61;value";
        String result = SanitizeUtil.sanitizeAllowEqual(input);
        assertTrue(result.contains("="));
    }

    @Test
    void sanitizeAllowEqual_shouldRemoveHtmlElements()
    {
        String input = "<script>alert('XSS')</script>key&#61;value";
        String result = SanitizeUtil.sanitizeAllowEqual(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("key=value"));
    }

    @Test
    void sanitizeAllowEqual_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowEqual(null);
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowEqual_shouldHandleEmptyString()
    {
        String result = SanitizeUtil.sanitizeAllowEqual("");
        assertEquals("", result);
    }

    // ========== sanitizeAllowDropDownOption Tests ==========

    @Test
    void sanitizeAllowDropDownOption_shouldAllowOptionElements()
    {
        String input = "<option value=\"1\" selected>Option 1</option>";
        String result = SanitizeUtil.sanitizeAllowDropDownOption(input);
        assertTrue(result.contains("<option"));
        assertTrue(result.contains("value=\"1\""));
        assertTrue(result.contains("selected"));
    }

    @Test
    void sanitizeAllowDropDownOption_shouldAllowOptgroupElements()
    {
        String input = "<optgroup label=\"Group 1\" disabled><option value=\"1\">Option 1</option></optgroup>";
        String result = SanitizeUtil.sanitizeAllowDropDownOption(input);
        assertTrue(result.contains("<optgroup"));
        assertTrue(result.contains("label=\"Group 1\""));
        assertTrue(result.contains("disabled"));
    }

    @Test
    void sanitizeAllowDropDownOption_shouldAllowAllowedAttributes()
    {
        String input = "<option value=\"1\" disabled label=\"Label\" title=\"Title\">Text</option>";
        String result = SanitizeUtil.sanitizeAllowDropDownOption(input);
        assertTrue(result.contains("value=\"1\""));
        assertTrue(result.contains("disabled"));
        assertTrue(result.contains("label=\"Label\""));
        assertTrue(result.contains("title=\"Title\""));
    }

    @Test
    void sanitizeAllowDropDownOption_shouldRemoveScriptTags()
    {
        String input = "<option value=\"1\"><script>alert('XSS')</script>Option</option>";
        String result = SanitizeUtil.sanitizeAllowDropDownOption(input);
        assertFalse(result.contains("<script>"));
    }

    @Test
    void sanitizeAllowDropDownOption_shouldRemoveDisallowedAttributes()
    {
        String input = "<option value=\"1\" onclick=\"alert('XSS')\">Option</option>";
        String result = SanitizeUtil.sanitizeAllowDropDownOption(input);
        assertFalse(result.contains("onclick"));
    }

    @Test
    void sanitizeAllowDropDownOption_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeAllowDropDownOption(null);
        assertNull(result);
    }

    // ========== sanitizeNoElementContent Tests ==========

    @Test
    void sanitizeNoElementContent_shouldRemoveAllHtmlElements()
    {
        String input = "<b>Bold</b><i>Italic</i><script>alert('XSS')</script>";
        String result = SanitizeUtil.sanitizeNoElementContent(input);
        assertFalse(result.contains("<b>"));
        assertFalse(result.contains("<i>"));
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("Bold"));
        assertTrue(result.contains("Italic"));
    }

    @Test
    void sanitizeNoElementContent_shouldHandlePlainText()
    {
        String input = "Plain text with no HTML";
        String result = SanitizeUtil.sanitizeNoElementContent(input);
        assertEquals("Plain text with no HTML", result);
    }

    @Test
    void sanitizeNoElementContent_shouldHandleNullInput()
    {
        String result = SanitizeUtil.sanitizeNoElementContent(null);
        assertNull(result);
    }

    @Test
    void sanitizeNoElementContent_shouldEncodeSpecialCharacters()
    {
        String input = "<div>Test & Content</div>";
        String result = SanitizeUtil.sanitizeNoElementContent(input);
        assertFalse(result.contains("<div>"));
        assertTrue(result.contains("Test"));
        assertTrue(result.contains("Content"));
    }

    // ========== sanitizeAllowRichContent Tests ==========

    @Test
    void sanitizeAllowRichContent_shouldAllowParagraphElements()
    {
        String input = "<p>Paragraph text</p>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<p>"));
        assertTrue(result.contains("Paragraph text"));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowListElements()
    {
        String input = "<ul><li>Item 1</li><li>Item 2</li></ul>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("<li>"));
        assertTrue(result.contains("Item 1"));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowTableElements()
    {
        String input = "<table><thead><tr><th>Header</th></tr></thead><tbody><tr><td>Data</td></tr></tbody></table>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<thead>"));
        assertTrue(result.contains("<tbody>"));
        assertTrue(result.contains("<tr>"));
        assertTrue(result.contains("<th>"));
        assertTrue(result.contains("<td>"));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowLinksWithAttributes()
    {
        String input = "<a href=\"https://example.com\" target=\"_blank\" data-mce-href=\"test\">Link</a>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<a"));
        assertTrue(result.contains("href=\"https://example.com\""));
        assertTrue(result.contains("target=\"_blank\""));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowStyling()
    {
        String input = "<span style=\"color: red;\">Styled text</span>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<span"));
        assertTrue(result.contains("Styled text"));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowDataMceAttributes()
    {
        String input = "<p data-mce-style=\"text-align: center;\">Centered</p>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("data-mce-style"));
    }

    @Test
    void sanitizeAllowRichContent_shouldAllowBreakWithDataMceBogus()
    {
        String input = "<br data-mce-bogus=\"1\">";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(result.contains("<br"));
        assertTrue(result.contains("data-mce-bogus"));
    }

    @Test
    void sanitizeAllowRichContent_shouldRemoveScriptTags()
    {
        String input = "<p>Safe content</p><script>alert('XSS')</script>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("Safe content"));
    }

    @Test
    void sanitizeAllowRichContent_shouldRemoveDisallowedElements()
    {
        String input = "<div>Div content</div><p>Para content</p>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertFalse(result.contains("<div>"));
        assertTrue(result.contains("<p>"));
    }

    @Test
    void sanitizeAllowRichContent_shouldHandleNullInput()
    {
        assertNull(SanitizeUtil.sanitizeAllowRichContent(null));
    }

    @Test
    void sanitizeAllowRichContent_shouldHandleEmptyString()
    {
        String result = SanitizeUtil.sanitizeAllowRichContent("");
        assertEquals("", result);
    }

    @Test
    void sanitizeAllowRichContent_shouldHandleWhitespaceOnly()
    {
        String result = SanitizeUtil.sanitizeAllowRichContent("   ");
        assertEquals("   ", result);
    }

    @Test
    void sanitizeAllowRichContent_shouldRemoveJavascriptProtocol()
    {
        String input = "<a href=\"javascript:alert('XSS')\">Click</a>";
        String result = SanitizeUtil.sanitizeAllowRichContent(input);
        assertFalse(result.contains("javascript:"));
    }

    // ========== Cross-cutting concerns ==========

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<img src=x onerror=alert('XSS')>",
            "<body onload=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "<iframe src=\"javascript:alert('XSS')\"></iframe>"
        }
    )
    void sanitizeAllowCommonFormatWithLinks_shouldRemoveCommonXssVectors(String xssAttempt)
    {
        String result = SanitizeUtil.sanitizeAllowCommonFormatWithLinks(xssAttempt);
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("onload"));
        assertFalse(result.contains("<iframe"));
        assertFalse(result.contains("<svg"));
        assertFalse(result.contains("alert"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "<body onload=alert('XSS')>"
        }
    )
    void sanitizeNoElementContent_shouldRemoveCommonXssVectors(String xssAttempt)
    {
        String result = SanitizeUtil.sanitizeNoElementContent(xssAttempt);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("<img"));
        assertFalse(result.contains("<body"));
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("onload"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>"
        }
    )
    void sanitizeAllowRichContent_shouldRemoveCommonXssVectors(String xssAttempt)
    {
        String result = SanitizeUtil.sanitizeAllowRichContent(xssAttempt);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("<img"));
        assertFalse(result.contains("<svg"));
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("onload"));
    }

    @Test
    void sanitizeMethods_shouldHandleComplexNestedStructures()
    {
        String input = "<div><p><b>Bold <i>and italic</i></b></p></div>";

        String richResult = SanitizeUtil.sanitizeAllowRichContent(input);
        assertTrue(richResult.contains("<p>"));
        assertTrue(richResult.contains("<b>"));
        assertTrue(richResult.contains("<i>"));

        String commonResult = SanitizeUtil.sanitizeAllowCommonFormat(input);
        assertFalse(commonResult.contains("<div>"));
        assertFalse(commonResult.contains("<p>"));
        assertTrue(commonResult.contains("Bold"));
        assertTrue(commonResult.contains("and italic"));
    }

    @Test
    void sanitizeMethods_shouldHandleSpecialCharacters()
    {
        String input = "Text with & < > \" ' characters";

        String result1 = SanitizeUtil.sanitizeNoElementContent(input);
        assertTrue(result1.contains("&"));

        String result2 = SanitizeUtil.sanitizeAllowCommonFormat(input);
        assertTrue(result2.contains("&"));
    }

    @Test
    void sanitizeMethods_shouldHandleUnicodeCharacters()
    {
        String input = "Unicode: \u00E9 \u4E2D \u0623";

        String result = SanitizeUtil.sanitizeNoElementContent(input);
        assertTrue(result.contains("\u00E9"));
        assertTrue(result.contains("\u4E2D"));
        assertTrue(result.contains("\u0623"));
    }
}
