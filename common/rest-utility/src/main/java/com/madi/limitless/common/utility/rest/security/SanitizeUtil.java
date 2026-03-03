package com.madi.limitless.common.utility.rest.security;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamRenderer;
import org.owasp.html.PolicyFactory;
import org.springframework.util.StringUtils;

@Slf4j
@UtilityClass
public class SanitizeUtil
{
    /**
     * This method can be used to remediate XSS issues. It accepts String content and "sanitizes" the content by using
     * the approved OWASP JAVA HTML SANITIZER PROJECT by encoding and allowing common html format content.
     */
    public static String sanitizeAllowCommonFormatWithLinks(String content)
    {
        HtmlPolicyBuilder policyBuilderWithLinks = new HtmlPolicyBuilder().allowElements("a")
                                                                          .allowCommonInlineFormattingElements()
                                                                          .allowAttributes("title")
                                                                          .globally()
                                                                          .allowAttributes("href")
                                                                          .onElements("a")
                                                                          .allowStandardUrlProtocols();
        return sanitize(content, policyBuilderWithLinks);
    }

    public static String sanitizeAllowCommonFormat(String content)
    {
        HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder().allowCommonInlineFormattingElements();
        return sanitize(content, policyBuilder);
    }

    public static String sanitizeAllowUrls(String content)
    {
        HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder().allowStandardUrlProtocols();
        return sanitize(content, policyBuilder);
    }

    public static String sanitizeAllowCommonFormatIgnoreChars(String content)
    {
        HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder().allowCommonInlineFormattingElements();
        String s = sanitize(content, policyBuilder);
        if (!StringUtils.hasText(s))
        {
            s = "";
        }
        s = s.replace("&#61;", "=");
        return s.replace("&#34;", "\"");
    }

    public static String sanitizeAllowEqual(String content)
    {
        String s = sanitizeNoElementContent(content);
        if (!StringUtils.hasText(s))
        {
            s = "";
        }
        return s.replace("&#61;", "=");
    }

    public static String sanitizeAllowDropDownOption(String content)
    {
        HtmlPolicyBuilder optionPolicy = new HtmlPolicyBuilder()
            .allowCommonInlineFormattingElements()
            .allowElements("option", "optgroup")
            .allowAttributes("title", "value", "disabled", "label", "selected")
            .onElements("option")
            .allowAttributes("label", "disabled")
            .onElements("optgroup");
        return sanitize(content, optionPolicy);
    }

    /**
     * This method can be used to remediate XSS issues. It accepts aString content and "sanitizes" the content by using
     * the approved OWASP JAVA HTML SANITIZER PROJECT by encoding/removing any HTML content.
     */
    public static String sanitizeNoElementContent(String content)
    {
        HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder().allowElements("");
        return sanitize(content, policyBuilder);
    }

    private static String sanitize(String content, HtmlPolicyBuilder policyBuilder)
    {
        if (content == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        HtmlStreamRenderer renderer = HtmlStreamRenderer.create(
            sb, errorMessage -> log.error("Error occurred while attempting to render content: {}", errorMessage));

        HtmlSanitizer.Policy policy = policyBuilder.toFactory().apply(renderer);
        HtmlSanitizer.sanitize(content, policy);

        return sb.toString();
    }

    private static String removeLogInjection(String unstrippedLog)
    {
        return StringEscapeUtils.escapeJava(unstrippedLog);
    }

    //private static String encodeHTMLContent(String content)
    //{
    //    return ESAPI.encoder().encodeForHTML(content);
    //}

    // **
    // * This method can be used to remediate Log Forging related findings. It accepts an object to be sent to a log,
    // * removes blacklisted characters, encodes the message, and then identifies if the message has been encoded
    // */
    //public static String filterLogContent(Object objectToLog)
    //{
    //    if (objectToLog == null)
    //    {
    //        return "";
    //    }
    //
    //    String logStr = objectToLog.toString();
    //    String filteredLog = encodeHTMLContent(removeLogInjection(logStr));
    //    if (!logStr.equals(filteredLog))
    //    {
    //        filteredLog += " (Log was Encoded)";
    //    }
    //    return filteredLog;
    //}

    // **
    // * This method can be used to remediate Log Forging related findings. It accepts an object to be sent to a log,
    // * removes blacklisted characters
    // */
    //public static Map<String, Object> filterJsonLogContent(Map<String, Object> objectToLog)
    //{
    //    if (objectToLog == null)
    //    {
    //        return new HashMap<>();
    //    }
    //
    //    return objectToLog.entrySet().stream().collect(Collectors.toMap(
    //        Map.Entry::getKey,
    //        entry -> encodeHTMLContent(removeLogInjection(entry.getValue().toString()))
    //    ));
    //}

    public static String sanitizeAllowRichContent(String content)
    {
        if (!StringUtils.hasText(content))
        {
            return content;
        }

        PolicyFactory policy = new HtmlPolicyBuilder().allowStyling()
                                                      .allowStandardUrlProtocols()
                                                      .allowAttributes("data-mce-style")
                                                      .globally()
                                                      .allowAttributes("href")
                                                      .onElements("a")
                                                      .allowStandardUrlProtocols()
                                                      .allowAttributes("target")
                                                      .onElements("a")
                                                      .allowAttributes("data-mce-href")
                                                      .onElements("a")
                                                      .allowAttributes("data-mce-bogus")
                                                      .onElements("br")
                                                      .allowElements(
                                                          "a",
                                                          "p", "i", "b", "u", "strong", "em", "small", "big", "br",
                                                          "col", "span",
                                                          "ul", "ol", "li", "tbody", "thead", "tfoot",
                                                          "table", "td", "th", "tr"
                                                      )
                                                      .toFactory();
        return policy.sanitize(content);
    }
}