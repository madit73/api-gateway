package com.madi.common.utility.rest.controller.errors.general;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixedResultTest
{

    @Test
    void builder_shouldCreateMixedResultWithSuccessfulAndExceptions()
    {
        List<Long> successful = Arrays.asList(1L, 2L, 3L);
        List<Throwable> exceptions = Arrays.asList(
            new RuntimeException("Error 1"),
            new IllegalArgumentException("Error 2")
        );

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .exceptions(exceptions)
                                        .build();

        assertEquals(3, result.getSuccessful().size());
        assertEquals(2, result.getExceptions().size());
        assertTrue(result.getSuccessful().contains(1L));
        assertTrue(result.getSuccessful().contains(2L));
        assertTrue(result.getSuccessful().contains(3L));
    }

    @Test
    void builder_shouldCreateMixedResultWithOnlySuccessful()
    {
        List<Long> successful = Arrays.asList(10L, 20L, 30L);

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .build();

        assertNotNull(result);
        assertEquals(3, result.getSuccessful().size());
        assertNull(result.getExceptions());
    }

    @Test
    void builder_shouldCreateMixedResultWithOnlyExceptions()
    {
        List<Throwable> exceptions = Arrays.asList(
            new RuntimeException("Error 1"),
            new NullPointerException("Error 2")
        );

        MixedResult result = MixedResult.builder()
                                        .exceptions(exceptions)
                                        .build();

        assertNotNull(result);
        assertNull(result.getSuccessful());
        assertEquals(2, result.getExceptions().size());
    }

    @Test
    void builder_shouldCreateEmptyMixedResult()
    {
        MixedResult result = MixedResult.builder().build();

        assertNotNull(result);
        assertNull(result.getSuccessful());
        assertNull(result.getExceptions());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyMixedResult()
    {
        MixedResult result = new MixedResult();

        assertNotNull(result);
        assertNull(result.getSuccessful());
        assertNull(result.getExceptions());
    }

    @Test
    void allArgsConstructor_shouldCreateMixedResultWithBothLists()
    {
        List<Long> successful = Arrays.asList(100L, 200L);
        List<Throwable> exceptions = Collections.singletonList(new RuntimeException("Error"));

        MixedResult result = new MixedResult(successful, exceptions);

        assertEquals(2, result.getSuccessful().size());
        assertEquals(1, result.getExceptions().size());
    }

    @Test
    void allArgsConstructor_shouldAcceptNullValues()
    {
        MixedResult result = new MixedResult(null, null);

        assertNotNull(result);
        assertNull(result.getSuccessful());
        assertNull(result.getExceptions());
    }

    @Test
    void setSuccessful_shouldUpdateSuccessfulList()
    {
        List<Long> successful = Arrays.asList(5L, 6L, 7L);
        MixedResult result = MixedResult.builder().successful(successful).build();

        assertEquals(3, result.getSuccessful().size());
        assertTrue(result.getSuccessful().contains(5L));
    }

    @Test
    void setExceptions_shouldUpdateExceptionsList()
    {
        List<Throwable> exceptions = Collections.singletonList(new IllegalStateException("Invalid"));
        MixedResult result = MixedResult.builder().exceptions(exceptions).build();

        assertEquals(1, result.getExceptions().size());
        assertEquals("Invalid", result.getExceptions().getFirst().getMessage());
    }

    @Test
    void builder_shouldHandleEmptyLists()
    {
        List<Long> successful = Collections.emptyList();
        List<Throwable> exceptions = Collections.emptyList();

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .exceptions(exceptions)
                                        .build();

        assertNotNull(result.getSuccessful());
        assertNotNull(result.getExceptions());
        assertEquals(0, result.getSuccessful().size());
        assertEquals(0, result.getExceptions().size());
    }

    @Test
    void builder_shouldHandleLargeSuccessfulList()
    {
        List<Long> successful = new ArrayList<>();
        for (long i = 0; i < 1000; i++)
        {
            successful.add(i);
        }

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .build();

        assertEquals(1000, result.getSuccessful().size());
        assertTrue(result.getSuccessful().contains(0L));
        assertTrue(result.getSuccessful().contains(999L));
    }

    @Test
    void builder_shouldHandleMultipleExceptionTypes()
    {
        List<Throwable> exceptions = Arrays.asList(
            new RuntimeException("Runtime error"),
            new IllegalArgumentException("Illegal argument"),
            new NullPointerException("Null pointer"),
            new UnsupportedOperationException("Unsupported operation"),
            new IllegalStateException("Illegal state")
        );

        MixedResult result = MixedResult.builder()
                                        .exceptions(exceptions)
                                        .build();

        assertEquals(5, result.getExceptions().size());
        assertInstanceOf(RuntimeException.class, result.getExceptions().getFirst());
        assertInstanceOf(IllegalArgumentException.class, result.getExceptions().get(1));
        assertInstanceOf(NullPointerException.class, result.getExceptions().get(2));
    }

    @Test
    void builder_shouldHandleDuplicateSuccessfulIds()
    {
        List<Long> successful = Arrays.asList(1L, 1L, 2L, 2L, 3L);

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .build();

        assertEquals(5, result.getSuccessful().size());
    }

    @Test
    void builder_shouldHandleMixedPositiveAndNegativeIds()
    {
        List<Long> successful = Arrays.asList(-1L, 0L, 1L, -100L, 100L);

        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .build();

        assertEquals(5, result.getSuccessful().size());
        assertTrue(result.getSuccessful().contains(-1L));
        assertTrue(result.getSuccessful().contains(0L));
        assertTrue(result.getSuccessful().contains(100L));
    }

    @Test
    void builder_shouldHandleExceptionsWithNullMessages()
    {
        List<Throwable> exceptions = Arrays.asList(
            new RuntimeException((String) null),
            new IllegalArgumentException((String) null)
        );

        MixedResult result = MixedResult.builder()
                                        .exceptions(exceptions)
                                        .build();

        assertEquals(2, result.getExceptions().size());
        assertNull(result.getExceptions().getFirst().getMessage());
        assertNull(result.getExceptions().get(1).getMessage());
    }

    @Test
    void builder_shouldHandleExceptionsWithEmptyMessages()
    {
        List<Throwable> exceptions = Arrays.asList(
            new RuntimeException(""),
            new IllegalArgumentException("")
        );

        MixedResult result = MixedResult.builder()
                                        .exceptions(exceptions)
                                        .build();

        assertEquals(2, result.getExceptions().size());
        assertEquals("", result.getExceptions().getFirst().getMessage());
        assertEquals("", result.getExceptions().get(1).getMessage());
    }

    @Test
    void getSuccessful_shouldReturnMutableList()
    {
        List<Long> successful = new ArrayList<>(Arrays.asList(1L, 2L));
        MixedResult result = MixedResult.builder()
                                        .successful(successful)
                                        .build();

        result.getSuccessful().add(3L);

        assertEquals(3, result.getSuccessful().size());
    }

    @Test
    void getExceptions_shouldReturnMutableList()
    {
        List<Throwable> exceptions = new ArrayList<>(Collections.singletonList(new RuntimeException("Error")));
        MixedResult result = MixedResult.builder()
                                        .exceptions(exceptions)
                                        .build();

        result.getExceptions().add(new IllegalArgumentException("New error"));

        assertEquals(2, result.getExceptions().size());
    }
}
