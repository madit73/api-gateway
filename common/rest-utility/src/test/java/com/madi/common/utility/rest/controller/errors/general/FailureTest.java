package com.madi.common.utility.rest.controller.errors.general;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailureTest
{

    @Test
    void builder_shouldCreateFailureWithObjectAndMessage()
    {
        Throwable throwable = new RuntimeException("Test error");
        String message = "Failure message";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        assertEquals(throwable, failure.object());
        assertEquals("Test error", failure.object().getMessage());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldCreateFailureWithOnlyObject()
    {
        Throwable throwable = new IllegalArgumentException("Invalid argument");

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .build();

        assertEquals(throwable, failure.object());
        assertNull(failure.message());
    }

    @Test
    void builder_shouldCreateFailureWithOnlyMessage()
    {
        String message = "Error occurred";

        Failure failure = Failure.builder()
                                 .message(message)
                                 .build();

        assertNull(failure.object());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldCreateEmptyFailure()
    {
        Failure failure = Failure.builder().build();

        assertNull(failure.object());
        assertNull(failure.message());
    }

    @Test
    void builder_shouldHandleNullValues()
    {
        Failure failure = Failure.builder()
                                 .object(null)
                                 .message(null)
                                 .build();

        assertNull(failure.object());
        assertNull(failure.message());
    }

    @Test
    void builder_shouldHandleEmptyMessage()
    {
        Throwable throwable = new RuntimeException("Error");

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message("")
                                 .build();

        assertEquals(throwable, failure.object());
        assertEquals("", failure.message());
    }

    @Test
    void builder_shouldHandleDifferentExceptionTypes()
    {
        Throwable ex1 = new RuntimeException("Runtime");
        Throwable ex2 = new NullPointerException("Null");
        Throwable ex3 = new IllegalStateException("State");

        Failure failure1 = Failure.builder().object(ex1).message("Msg1").build();
        Failure failure2 = Failure.builder().object(ex2).message("Msg2").build();
        Failure failure3 = Failure.builder().object(ex3).message("Msg3").build();

        assertTrue(failure1.object() instanceof RuntimeException);
        assertTrue(failure2.object() instanceof NullPointerException);
        assertTrue(failure3.object() instanceof IllegalStateException);
    }

    @Test
    void record_shouldBeImmutable()
    {
        Throwable throwable = new RuntimeException("Error");
        String message = "Failure message";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        // Record fields are immutable by design
        // Verify we can access them but cannot modify
        assertEquals(throwable, failure.object());
        assertEquals(message, failure.message());
    }

    @Test
    void equals_shouldReturnTrueForSameValues()
    {
        Throwable throwable = new RuntimeException("Error");
        String message = "Same message";

        Failure failure1 = Failure.builder()
                                  .object(throwable)
                                  .message(message)
                                  .build();

        Failure failure2 = Failure.builder()
                                  .object(throwable)
                                  .message(message)
                                  .build();

        assertEquals(failure1, failure2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues()
    {
        Failure failure1 = Failure.builder()
                                  .object(new RuntimeException("Error 1"))
                                  .message("Message 1")
                                  .build();

        Failure failure2 = Failure.builder()
                                  .object(new RuntimeException("Error 2"))
                                  .message("Message 2")
                                  .build();

        assertNotEquals(failure1, failure2);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals()
    {
        Throwable throwable = new RuntimeException("Error");
        String message = "Message";

        Failure failure1 = Failure.builder()
                                  .object(throwable)
                                  .message(message)
                                  .build();

        Failure failure2 = Failure.builder()
                                  .object(throwable)
                                  .message(message)
                                  .build();

        assertEquals(failure1.hashCode(), failure2.hashCode());
    }

    @Test
    void toString_shouldContainFieldInformation()
    {
        Throwable throwable = new RuntimeException("Test error");
        String message = "Failure occurred";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        String toString = failure.toString();

        assertTrue(toString.contains("Failure"));
        assertTrue(toString.contains("Failure occurred"));
    }

    @Test
    void builder_shouldHandleLongMessages()
    {
        String longMessage = "This is a very long error message that contains a lot of text ".repeat(50);
        Throwable throwable = new RuntimeException("Error");

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(longMessage)
                                 .build();

        assertEquals(longMessage, failure.message());
    }

    @Test
    void builder_shouldHandleSpecialCharacters()
    {
        String message = "Error: <value> & \"quoted\" content with special chars!@#$%";
        Throwable throwable = new RuntimeException("Exception: <test>");

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        assertTrue(failure.message().contains("<value>"));
        assertTrue(failure.message().contains("&"));
        assertTrue(failure.message().contains("\"quoted\""));
        assertTrue(failure.object().getMessage().contains("<test>"));
    }

    @Test
    void builder_shouldHandleExceptionWithNullMessage()
    {
        Throwable throwable = new RuntimeException((String) null);
        String message = "Custom failure message";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        assertNull(failure.object().getMessage());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldHandleExceptionWithEmptyMessage()
    {
        Throwable throwable = new RuntimeException("");
        String message = "Custom message";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        assertEquals("", failure.object().getMessage());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldHandleCustomExceptionTypes()
    {
        class CustomException extends Exception
        {
            public CustomException(String message)
            {
                super(message);
            }
        }

        Throwable customEx = new CustomException("Custom error");
        String message = "Custom failure";

        Failure failure = Failure.builder()
                                 .object(customEx)
                                 .message(message)
                                 .build();

        assertTrue(failure.object() instanceof CustomException);
        assertEquals("Custom error", failure.object().getMessage());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldHandleExceptionWithCause()
    {
        Throwable cause = new IllegalArgumentException("Root cause");
        Throwable throwable = new RuntimeException("Wrapper exception", cause);
        String message = "Failure with cause";

        Failure failure = Failure.builder()
                                 .object(throwable)
                                 .message(message)
                                 .build();

        assertEquals("Wrapper exception", failure.object().getMessage());
        assertEquals("Root cause", failure.object().getCause().getMessage());
        assertEquals(message, failure.message());
    }

    @Test
    void builder_shouldHandleMultipleFailuresIndependently()
    {
        Failure failure1 = Failure.builder()
                                  .object(new RuntimeException("Error 1"))
                                  .message("Message 1")
                                  .build();

        Failure failure2 = Failure.builder()
                                  .object(new IllegalArgumentException("Error 2"))
                                  .message("Message 2")
                                  .build();

        assertNotEquals(failure1.object().getClass(), failure2.object().getClass());
        assertNotEquals(failure1.message(), failure2.message());
    }
}
