package com.madi.common.utility.rest.controller.errors.general;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorUtilTest
{

    @Test
    void createErrorResponse_withExplanationAndAction_shouldCreateErrorResponse()
    {
        Exception ex = new RuntimeException("Test exception");
        String explanation = "Custom explanation";
        String action = "Custom action";

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, explanation, action);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("RuntimeException", detail.getMessageId());
        assertEquals("Test exception", detail.getMessage());
        assertEquals("Custom explanation", detail.getExplanation());
        assertEquals("Custom action", detail.getAction());
    }

    @Test
    void createErrorResponse_withNullExplanation_shouldUseDefaultExplanation()
    {
        Exception ex = new IllegalArgumentException("Invalid argument");
        String action = "Custom action";

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, null, action);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("IllegalArgumentException", detail.getMessageId());
        assertEquals("Invalid argument", detail.getMessage());
        assertEquals("An error occurred processing your request.", detail.getExplanation());
        assertEquals("Custom action", detail.getAction());
    }

    @Test
    void createErrorResponse_withNullAction_shouldUseDefaultAction()
    {
        Exception ex = new NullPointerException("Null value");
        String explanation = "Custom explanation";

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, explanation, null);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("NullPointerException", detail.getMessageId());
        assertEquals("Null value", detail.getMessage());
        assertEquals("Custom explanation", detail.getExplanation());
        assertEquals("Please check the request and try again.", detail.getAction());
    }

    @Test
    void createErrorResponse_withNullExplanationAndAction_shouldUseDefaults()
    {
        Exception ex = new IllegalStateException("Invalid state");

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, null, null);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("IllegalStateException", detail.getMessageId());
        assertEquals("Invalid state", detail.getMessage());
        assertEquals("An error occurred processing your request.", detail.getExplanation());
        assertEquals("Please check the request and try again.", detail.getAction());
    }

    @Test
    void createErrorResponse_withExceptionOnly_shouldUseDefaultMessages()
    {
        Exception ex = new RuntimeException("Test exception");

        ErrorResponse response = ErrorUtil.createErrorResponse(ex);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("RuntimeException", detail.getMessageId());
        assertEquals("Test exception", detail.getMessage());
        assertEquals("An error occurred processing your request.", detail.getExplanation());
        assertEquals("Please check the request and try again.", detail.getAction());
    }

    @Test
    void createErrorResponse_withEmptyExceptionMessage_shouldHandleGracefully()
    {
        Exception ex = new RuntimeException("");

        ErrorResponse response = ErrorUtil.createErrorResponse(ex);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("RuntimeException", detail.getMessageId());
        assertEquals("", detail.getMessage());
        assertEquals("An error occurred processing your request.", detail.getExplanation());
    }

    @Test
    void createErrorResponse_withNullExceptionMessage_shouldHandleGracefully()
    {
        Exception ex = new RuntimeException((String) null);

        ErrorResponse response = ErrorUtil.createErrorResponse(ex);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("RuntimeException", detail.getMessageId());
        assertNull(detail.getMessage());
    }

    @Test
    void createErrorResponse_withDifferentExceptionTypes_shouldGenerateCorrectMessageId()
    {
        Exception ex1 = new IllegalArgumentException("Arg error");
        Exception ex2 = new NullPointerException("Null error");
        Exception ex3 = new UnsupportedOperationException("Unsupported error");

        ErrorResponse response1 = ErrorUtil.createErrorResponse(ex1);
        ErrorResponse response2 = ErrorUtil.createErrorResponse(ex2);
        ErrorResponse response3 = ErrorUtil.createErrorResponse(ex3);

        assertEquals("IllegalArgumentException", response1.getErrors().getFirst().getMessageId());
        assertEquals("NullPointerException", response2.getErrors().getFirst().getMessageId());
        assertEquals("UnsupportedOperationException", response3.getErrors().getFirst().getMessageId());
    }

    @Test
    void createErrorResponse_withCustomException_shouldUseCustomExceptionName()
    {
        class CustomException extends Exception
        {
            public CustomException(String message)
            {
                super(message);
            }
        }

        Exception ex = new CustomException("Custom error");

        ErrorResponse response = ErrorUtil.createErrorResponse(ex);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals("CustomException", response.getErrors().getFirst().getMessageId());
        assertEquals("Custom error", response.getErrors().getFirst().getMessage());
    }

    @Test
    void createErrorResponse_withEmptyStrings_shouldAcceptEmptyValues()
    {
        Exception ex = new RuntimeException("Test");
        String explanation = "";
        String action = "";

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, explanation, action);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals("", detail.getExplanation());
        assertEquals("", detail.getAction());
    }

    @Test
    void createErrorResponse_withLongMessages_shouldHandleLongStrings()
    {
        String longMessage = "This is a very long error message ".repeat(50);
        Exception ex = new RuntimeException(longMessage);
        String longExplanation = "Long explanation ".repeat(100);
        String longAction = "Long action ".repeat(100);

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, longExplanation, longAction);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());

        ErrorDetail detail = response.getErrors().getFirst();
        assertEquals(longMessage, detail.getMessage());
        assertEquals(longExplanation, detail.getExplanation());
        assertEquals(longAction, detail.getAction());
    }

    @Test
    void createErrorResponse_withSpecialCharacters_shouldPreserveCharacters()
    {
        Exception ex = new RuntimeException("Error: <value> & \"quoted\" content");
        String explanation = "Explanation with <tags> & special chars";
        String action = "Action: retry @ later time";

        ErrorResponse response = ErrorUtil.createErrorResponse(ex, explanation, action);

        assertNotNull(response);
        ErrorDetail detail = response.getErrors().getFirst();

        assertTrue(detail.getMessage().contains("<value>"));
        assertTrue(detail.getMessage().contains("&"));
        assertTrue(detail.getExplanation().contains("<tags>"));
        assertTrue(detail.getAction().contains("@"));
    }

    @Test
    void createErrorResponse_shouldReturnImmutableSingletonList()
    {
        Exception ex = new RuntimeException("Test");

        ErrorResponse response = ErrorUtil.createErrorResponse(ex);

        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());

        assertThrows(
            UnsupportedOperationException.class,
            () -> response.getErrors().add(ErrorDetail.builder().build())
        );
    }

    @Test
    void createErrorResponse_multipleCallsShouldReturnIndependentResponses()
    {
        Exception ex1 = new RuntimeException("Error 1");
        Exception ex2 = new IllegalArgumentException("Error 2");

        ErrorResponse response1 = ErrorUtil.createErrorResponse(ex1, "Explanation 1", "Action 1");
        ErrorResponse response2 = ErrorUtil.createErrorResponse(ex2, "Explanation 2", "Action 2");

        assertNotEquals(response1.getErrors().getFirst().getMessage(), response2.getErrors().getFirst().getMessage());
        assertNotEquals(
            response1.getErrors().getFirst().getMessageId(), response2.getErrors().getFirst().getMessageId());
    }
}
