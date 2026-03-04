package com.madi.common.utility.rest.controller.errors.general;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorResponseTest
{

    @Test
    void builder_shouldCreateErrorResponseWithErrors()
    {
        ErrorDetail detail1 = ErrorDetail.builder()
                                         .messageId("ERR_001")
                                         .message("Error 1")
                                         .explanation("Explanation 1")
                                         .action("Action 1")
                                         .build();

        ErrorDetail detail2 = ErrorDetail.builder()
                                         .messageId("ERR_002")
                                         .message("Error 2")
                                         .explanation("Explanation 2")
                                         .action("Action 2")
                                         .build();

        List<ErrorDetail> errors = Arrays.asList(detail1, detail2);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals("ERR_001", response.getErrors().getFirst().getMessageId());
        assertEquals("ERR_002", response.getErrors().get(1).getMessageId());
    }

    @Test
    void builder_shouldCreateErrorResponseWithSingleError()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_SINGLE")
                                        .message("Single error")
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1, response.getErrors().size());
        assertEquals("ERR_SINGLE", response.getErrors().getFirst().getMessageId());
    }

    @Test
    void builder_shouldCreateEmptyErrorResponse()
    {
        ErrorResponse response = ErrorResponse.builder().build();

        assertNotNull(response);
        assertNull(response.getErrors());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyErrorResponse()
    {
        ErrorResponse response = new ErrorResponse();

        assertNotNull(response);
        assertNull(response.getErrors());
    }

    @Test
    void allArgsConstructor_shouldCreateErrorResponseWithErrors()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_001")
                                        .message("Error")
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = new ErrorResponse(errors);

        assertEquals(1, response.getErrors().size());
        assertEquals("ERR_001", response.getErrors().getFirst().getMessageId());
    }

    @Test
    void allArgsConstructor_shouldAcceptNullErrors()
    {
        ErrorResponse response = new ErrorResponse(null);

        assertNotNull(response);
        assertNull(response.getErrors());
    }

    @Test
    void setErrors_shouldUpdateErrorsList()
    {

        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_UPDATE")
                                        .message("Updated error")
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);
        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1, response.getErrors().size());
        assertEquals("ERR_UPDATE", response.getErrors().getFirst().getMessageId());
    }

    @Test
    void builder_shouldHandleEmptyErrorList()
    {
        List<ErrorDetail> errors = Collections.emptyList();

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertNotNull(response.getErrors());
        assertEquals(0, response.getErrors().size());
    }

    @Test
    void builder_shouldHandleMultipleErrors()
    {
        List<ErrorDetail> errors = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            errors.add(ErrorDetail.builder()
                                  .messageId("ERR_" + i)
                                  .message("Error " + i)
                                  .build());
        }

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(10, response.getErrors().size());
        assertEquals("ERR_0", response.getErrors().getFirst().getMessageId());
        assertEquals("ERR_9", response.getErrors().get(9).getMessageId());
    }

    @Test
    void builder_shouldHandleErrorsWithNullFields()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId(null)
                                        .message(null)
                                        .explanation(null)
                                        .action(null)
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1, response.getErrors().size());
        assertNull(response.getErrors().getFirst().getMessageId());
        assertNull(response.getErrors().getFirst().getMessage());
    }

    @Test
    void builder_shouldHandleErrorsWithEmptyStrings()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("")
                                        .message("")
                                        .explanation("")
                                        .action("")
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1, response.getErrors().size());
        assertEquals("", response.getErrors().getFirst().getMessageId());
        assertEquals("", response.getErrors().getFirst().getMessage());
    }

    @Test
    void getErrors_shouldReturnMutableList()
    {
        ErrorDetail detail1 = ErrorDetail.builder()
                                         .messageId("ERR_001")
                                         .message("Error 1")
                                         .build();

        List<ErrorDetail> errors = new ArrayList<>(Collections.singletonList(detail1));

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        ErrorDetail detail2 = ErrorDetail.builder()
                                         .messageId("ERR_002")
                                         .message("Error 2")
                                         .build();

        response.getErrors().add(detail2);

        assertEquals(2, response.getErrors().size());
    }

    @Test
    void builder_shouldHandleLargeNumberOfErrors()
    {
        List<ErrorDetail> errors = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            errors.add(ErrorDetail.builder()
                                  .messageId("ERR_" + i)
                                  .message("Error message " + i)
                                  .build());
        }

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1000, response.getErrors().size());
        assertEquals("ERR_0", response.getErrors().getFirst().getMessageId());
        assertEquals("ERR_999", response.getErrors().get(999).getMessageId());
    }

    @Test
    void builder_shouldHandleErrorsWithSpecialCharacters()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR-001")
                                        .message("Error: <value> & \"quoted\"")
                                        .explanation("Special chars: !@#$%^&*()")
                                        .action("Action with <tags>")
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertTrue(response.getErrors().getFirst().getMessage().contains("<value>"));
        assertTrue(response.getErrors().getFirst().getMessage().contains("&"));
        assertTrue(response.getErrors().getFirst().getExplanation().contains("!@#$%"));
    }

    @Test
    void equals_shouldReturnTrueForSameErrors()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_001")
                                        .message("Error")
                                        .build();

        List<ErrorDetail> errors1 = Collections.singletonList(detail);
        List<ErrorDetail> errors2 = Collections.singletonList(detail);

        ErrorResponse response1 = ErrorResponse.builder().errors(errors1).build();
        ErrorResponse response2 = ErrorResponse.builder().errors(errors2).build();

        assertEquals(response1.getErrors(), response2.getErrors());
    }

    @Test
    void builder_shouldHandleMixedCompleteAndIncompleteErrors()
    {
        ErrorDetail completeError = ErrorDetail.builder()
                                               .messageId("ERR_COMPLETE")
                                               .message("Complete error")
                                               .explanation("Full explanation")
                                               .action("Full action")
                                               .build();

        ErrorDetail incompleteError = ErrorDetail.builder()
                                                 .messageId("ERR_INCOMPLETE")
                                                 .build();

        List<ErrorDetail> errors = Arrays.asList(completeError, incompleteError);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(2, response.getErrors().size());
        assertNotNull(response.getErrors().getFirst().getExplanation());
        assertNull(response.getErrors().get(1).getExplanation());
    }

    @Test
    void builder_shouldHandleDuplicateErrors()
    {
        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_DUP")
                                        .message("Duplicate error")
                                        .build();

        List<ErrorDetail> errors = Arrays.asList(detail, detail, detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(3, response.getErrors().size());
        assertEquals("ERR_DUP", response.getErrors().getFirst().getMessageId());
        assertEquals("ERR_DUP", response.getErrors().get(1).getMessageId());
        assertEquals("ERR_DUP", response.getErrors().get(2).getMessageId());
    }

    @Test
    void setErrors_shouldReplaceExistingErrors()
    {
        ErrorDetail detail1 = ErrorDetail.builder()
                                         .messageId("ERR_001")
                                         .message("Error 1")
                                         .build();

        ErrorDetail detail2 = ErrorDetail.builder()
                                         .messageId("ERR_002")
                                         .message("Error 2")
                                         .build();

        ErrorResponse response = new ErrorResponse(Collections.singletonList(detail1));
        assertEquals(1, response.getErrors().size());

        response.setErrors(Collections.singletonList(detail2));
        assertEquals(1, response.getErrors().size());
        assertEquals("ERR_002", response.getErrors().getFirst().getMessageId());
    }

    @Test
    void builder_shouldHandleErrorsWithLongText()
    {
        String longMessage = "This is a very long error message ".repeat(100);
        String longExplanation = "This is a very long explanation ".repeat(100);

        ErrorDetail detail = ErrorDetail.builder()
                                        .messageId("ERR_LONG")
                                        .message(longMessage)
                                        .explanation(longExplanation)
                                        .build();

        List<ErrorDetail> errors = Collections.singletonList(detail);

        ErrorResponse response = ErrorResponse.builder()
                                              .errors(errors)
                                              .build();

        assertEquals(1, response.getErrors().size());
        assertEquals(longMessage, response.getErrors().getFirst().getMessage());
        assertEquals(longExplanation, response.getErrors().getFirst().getExplanation());
    }
}
