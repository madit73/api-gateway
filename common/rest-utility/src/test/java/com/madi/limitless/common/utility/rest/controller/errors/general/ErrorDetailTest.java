package com.madi.limitless.common.utility.rest.controller.errors.general;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorDetailTest
{

    @Test
    void builder_shouldCreateErrorDetailWithAllFields()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId("TEST_001")
                                             .message("Test error message")
                                             .explanation("This is a test explanation")
                                             .action("Please take this action")
                                             .build();

        assertEquals("TEST_001", errorDetail.getMessageId());
        assertEquals("Test error message", errorDetail.getMessage());
        assertEquals("This is a test explanation", errorDetail.getExplanation());
        assertEquals("Please take this action", errorDetail.getAction());
    }

    @Test
    void builder_shouldCreateErrorDetailWithPartialFields()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId("TEST_002")
                                             .message("Partial error")
                                             .build();

        assertEquals("TEST_002", errorDetail.getMessageId());
        assertEquals("Partial error", errorDetail.getMessage());
        assertNull(errorDetail.getExplanation());
        assertNull(errorDetail.getAction());
    }

    @Test
    void builder_shouldCreateEmptyErrorDetail()
    {
        ErrorDetail errorDetail = ErrorDetail.builder().build();

        assertNull(errorDetail.getMessageId());
        assertNull(errorDetail.getMessage());
        assertNull(errorDetail.getExplanation());
        assertNull(errorDetail.getAction());
    }

    @Test
    void setters_shouldUpdateFields()
    {
        ErrorDetail errorDetail = ErrorDetail.builder().build();

        errorDetail.setMessageId("UPDATE_001");
        errorDetail.setMessage("Updated message");
        errorDetail.setExplanation("Updated explanation");
        errorDetail.setAction("Updated action");

        assertEquals("UPDATE_001", errorDetail.getMessageId());
        assertEquals("Updated message", errorDetail.getMessage());
        assertEquals("Updated explanation", errorDetail.getExplanation());
        assertEquals("Updated action", errorDetail.getAction());
    }

    @Test
    void equals_shouldReturnTrueForSameValues()
    {
        ErrorDetail error1 = ErrorDetail.builder()
                                        .messageId("ID_001")
                                        .message("Same message")
                                        .explanation("Same explanation")
                                        .action("Same action")
                                        .build();

        ErrorDetail error2 = ErrorDetail.builder()
                                        .messageId("ID_001")
                                        .message("Same message")
                                        .explanation("Same explanation")
                                        .action("Same action")
                                        .build();

        assertEquals(error1, error2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues()
    {
        ErrorDetail error1 = ErrorDetail.builder()
                                        .messageId("ID_001")
                                        .message("Message 1")
                                        .build();

        ErrorDetail error2 = ErrorDetail.builder()
                                        .messageId("ID_002")
                                        .message("Message 2")
                                        .build();

        assertNotEquals(error1, error2);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals()
    {
        ErrorDetail error1 = ErrorDetail.builder()
                                        .messageId("ID_001")
                                        .message("Message")
                                        .build();

        ErrorDetail error2 = ErrorDetail.builder()
                                        .messageId("ID_001")
                                        .message("Message")
                                        .build();

        assertEquals(error1.hashCode(), error2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId("ID_001")
                                             .message("Test message")
                                             .explanation("Test explanation")
                                             .action("Test action")
                                             .build();

        String toString = errorDetail.toString();

        assertTrue(toString.contains("ID_001"));
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("Test explanation"));
        assertTrue(toString.contains("Test action"));
    }

    @Test
    void builder_shouldHandleNullValues()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId(null)
                                             .message(null)
                                             .explanation(null)
                                             .action(null)
                                             .build();

        assertNull(errorDetail.getMessageId());
        assertNull(errorDetail.getMessage());
        assertNull(errorDetail.getExplanation());
        assertNull(errorDetail.getAction());
    }

    @Test
    void builder_shouldHandleEmptyStrings()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId("")
                                             .message("")
                                             .explanation("")
                                             .action("")
                                             .build();

        assertEquals("", errorDetail.getMessageId());
        assertEquals("", errorDetail.getMessage());
        assertEquals("", errorDetail.getExplanation());
        assertEquals("", errorDetail.getAction());
    }

    @Test
    void builder_shouldHandleSpecialCharacters()
    {
        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId("ID-001")
                                             .message("Error: Something went wrong!")
                                             .explanation("Details: <value> & \"quoted\"")
                                             .action("Try again @ later time")
                                             .build();

        assertEquals("ID-001", errorDetail.getMessageId());
        assertEquals("Error: Something went wrong!", errorDetail.getMessage());
        assertEquals("Details: <value> & \"quoted\"", errorDetail.getExplanation());
        assertEquals("Try again @ later time", errorDetail.getAction());
    }
}
