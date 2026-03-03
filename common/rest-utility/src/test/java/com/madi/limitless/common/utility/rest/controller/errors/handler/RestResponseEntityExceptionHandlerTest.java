package com.madi.limitless.common.utility.rest.controller.errors.handler;


import com.madi.limitless.common.utility.rest.controller.errors.exception.DataValidationException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.DeleteEntityConflictException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.EntityDataNotFoundException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.IllegalTimePeriodException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.InternalServerErrorException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.MultipleExceptionsException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.PartialSuccessException;
import com.madi.limitless.common.utility.rest.controller.errors.exception.ResourceNotFoundException;
import com.madi.limitless.common.utility.rest.controller.errors.general.ErrorResponse;
import com.madi.limitless.common.utility.rest.controller.errors.general.MixedResult;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoBean(types = { WebRequest.class })
@DisplayName("RestResponseEntityExceptionHandler Tests")
class RestResponseEntityExceptionHandlerTest
{
    @InjectMocks
    private RestResponseEntityExceptionHandler exceptionHandler;

    @MockitoBean
    private WebRequest webRequest;

    private static final String TEST_ERROR_MESSAGE = "Test error message";

    @Nested
    @DisplayName("MultipleExceptionsException Tests")
    class MultipleExceptionsExceptionTests
    {
        @Test
        @DisplayName("Should handle MultipleExceptionsException with multiple exceptions")
        void shouldHandleMultipleExceptionsException()
        {
            List<Throwable> exceptions = List.of(
                new IllegalArgumentException("Error 1"),
                new RuntimeException("Error 2")
            );
            MultipleExceptionsException ex = new MultipleExceptionsException(exceptions, "My exception message");

            ResponseEntity<Object> response = exceptionHandler.handleMultipleExceptionsException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle MultipleExceptionsException with single exception")
        void shouldHandleMultipleExceptionsExceptionWithSingleException()
        {
            List<Throwable> exceptions = List.of(
                new IllegalArgumentException(TEST_ERROR_MESSAGE), new DataValidationException("TEST ERROR 2"));
            MultipleExceptionsException ex = new MultipleExceptionsException(exceptions, "My exception message");

            ResponseEntity<Object> response = exceptionHandler.handleMultipleExceptionsException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("NotImplementedException Tests")
    class NotImplementedExceptionTests
    {
        @Test
        @DisplayName("Should handle NotImplementedException with correct status")
        void shouldHandleNotImplementedException()
        {
            NotImplementedException ex = new NotImplementedException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleNotImplementedExceptionViolation(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("IllegalTimePeriodException Tests")
    class IllegalTimePeriodExceptionTests
    {
        @Test
        @DisplayName("Should handle IllegalTimePeriodException")
        void shouldHandleIllegalTimePeriodException()
        {
            IllegalTimePeriodException ex = new IllegalTimePeriodException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleIllegalTimePeriodError(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("InternalServerErrorException Tests")
    class InternalServerErrorExceptionTests
    {
        @Test
        @DisplayName("Should handle InternalServerErrorException")
        void shouldHandleInternalServerErrorException()
        {
            InternalServerErrorException ex = new InternalServerErrorException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle InternalServerErrorException with cause")
        void shouldHandleInternalServerErrorExceptionWithCause()
        {
            InternalServerErrorException ex = new InternalServerErrorException(
                TEST_ERROR_MESSAGE,
                new RuntimeException("Root cause")
            );

            ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("AuthenticationException Tests")
    class AuthenticationExceptionTests
    {
        @Test
        @DisplayName("Should handle AuthenticationException")
        void shouldHandleAuthenticationException()
        {
            AuthenticationException ex = new AuthenticationException(TEST_ERROR_MESSAGE)
            {
            };

            ResponseEntity<Object> response = exceptionHandler.handleAuthenticationErrors(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle AuthenticationException with null message")
        void shouldHandleAuthenticationExceptionWithNullMessage()
        {
            AuthenticationException ex = new AuthenticationException(null)
            {
            };

            ResponseEntity<Object> response = exceptionHandler.handleAuthenticationErrors(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("AccessDeniedException Tests")
    class AccessDeniedExceptionTests
    {
        @Test
        @DisplayName("Should handle AccessDeniedException")
        void shouldHandleAccessDeniedException()
        {
            AccessDeniedException ex = new AccessDeniedException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle AccessDeniedException with file path")
        void shouldHandleAccessDeniedExceptionWithFilePath()
        {
            AccessDeniedException ex = new AccessDeniedException("/restricted/path", null, "Access denied");

            ResponseEntity<Object> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException Tests")
    class ConstraintViolationExceptionTests
    {
        @Test
        @DisplayName("Should handle ConstraintViolationException with multiple violations")
        void shouldHandleConstraintViolationExceptionWithMultipleViolations()
        {
            ConstraintViolation<?> violation1 = createMockConstraintViolation("Field 'name' must not be null");
            ConstraintViolation<?> violation2 = createMockConstraintViolation("Field 'email' must be valid");
            Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);

            ConstraintViolationException ex = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle ConstraintViolationException with single violation")
        void shouldHandleConstraintViolationExceptionWithSingleViolation()
        {
            ConstraintViolation<?> violation = createMockConstraintViolation("Field 'id' must be positive");
            Set<ConstraintViolation<?>> violations = Set.of(violation);

            ConstraintViolationException ex = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle ConstraintViolationException with empty violations")
        void shouldHandleConstraintViolationExceptionWithEmptyViolations()
        {
            Set<ConstraintViolation<?>> violations = Set.of();
            ConstraintViolationException ex = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        private ConstraintViolation<?> createMockConstraintViolation(String message)
        {
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn(message);
            Path path = mock(Path.class);
            when(violation.getPropertyPath()).thenReturn(path);
            return violation;
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException Tests")
    class IllegalArgumentExceptionTests
    {
        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void shouldHandleIllegalArgumentException()
        {
            IllegalArgumentException ex = new IllegalArgumentException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with null message")
        void shouldHandleIllegalArgumentExceptionWithNullMessage()
        {
            IllegalArgumentException ex = new IllegalArgumentException();

            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("EntityNotFoundException Tests")
    class EntityNotFoundExceptionTests
    {
        @Test
        @DisplayName("Should handle EntityNotFoundException")
        void shouldHandleEntityNotFoundException()
        {
            EntityNotFoundException ex = new EntityNotFoundException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleEntityNotFoundException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle EntityNotFoundException with null message")
        void shouldHandleEntityNotFoundExceptionWithNullMessage()
        {
            EntityNotFoundException ex = new EntityNotFoundException();

            ResponseEntity<Object> response = exceptionHandler.handleEntityNotFoundException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("EntityDataNotFoundException Tests")
    class EntityDataNotFoundExceptionTests
    {
        @Test
        @DisplayName("Should handle EntityDataNotFoundException")
        void shouldHandleEntityDataNotFoundException()
        {
            EntityDataNotFoundException ex = new EntityDataNotFoundException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleEntityDataNotFoundException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("DataValidationException Tests")
    class DataValidationExceptionTests
    {
        @Test
        @DisplayName("Should handle DataValidationException")
        void shouldHandleDataValidationException()
        {
            DataValidationException ex = new DataValidationException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleDataValidationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("ValidationException Tests")
    class ValidationExceptionTests
    {
        @Test
        @DisplayName("Should handle ValidationException")
        void shouldHandleValidationException()
        {
            ValidationException ex = new ValidationException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleValidationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle ValidationException with null message")
        void shouldHandleValidationExceptionWithNullMessage()
        {
            ValidationException ex = new ValidationException();

            ResponseEntity<Object> response = exceptionHandler.handleValidationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException Tests")
    class ResourceNotFoundExceptionTests
    {
        @Test
        @DisplayName("Should handle ResourceNotFoundException")
        void shouldHandleResourceNotFoundException()
        {
            ResourceNotFoundException ex = new ResourceNotFoundException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("MethodArgumentTypeMismatchException Tests")
    class MethodArgumentTypeMismatchExceptionTests
    {
        @Test
        @DisplayName("Should handle MethodArgumentTypeMismatchException")
        void shouldHandleMethodArgumentTypeMismatchException()
        {
            MethodParameter parameter = mock(MethodParameter.class);

            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid",
                Long.class,
                "id",
                parameter,
                new NumberFormatException("For input string: \"invalid\"")
            );

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentTypeMismatchException(
                ex,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle MethodArgumentTypeMismatchException with null cause")
        void shouldHandleMethodArgumentTypeMismatchExceptionWithNullCause()
        {
            MethodParameter parameter = mock(MethodParameter.class);

            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "INVALID_STATUS",
                String.class,
                "status",
                parameter,
                null
            );

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentTypeMismatchException(
                ex,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("DeleteEntityConflictException Tests")
    class DeleteEntityConflictExceptionTests
    {
        @Test
        @DisplayName("Should handle DeleteEntityConflictException")
        void shouldHandleDeleteEntityConflictException()
        {
            DeleteEntityConflictException ex = new DeleteEntityConflictException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleDeleteEntityConflict(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }
    }

    @Nested
    @DisplayName("PartialSuccessException Tests")
    class PartialSuccessExceptionTests
    {
        @Test
        @DisplayName("Should handle PartialSuccessException")
        void shouldHandlePartialSuccessException()
        {
            MixedResult mixedResult = MixedResult.builder()
                                                 .successful(List.of(1L, 2L, 3L))
                                                 .exceptions(List.of(new RuntimeException("Failed item")))
                                                 .build();

            PartialSuccessException ex = new PartialSuccessException(mixedResult);

            ResponseEntity<MixedResult> response = exceptionHandler.handlePartialSuccessException(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.MULTI_STATUS, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(mixedResult, response.getBody());
        }

        @Test
        @DisplayName("Should handle PartialSuccessException with all failures")
        void shouldHandlePartialSuccessExceptionWithAllFailures()
        {
            MixedResult mixedResult = MixedResult.builder()
                                                 .successful(List.of())
                                                 .exceptions(
                                                     List.of(
                                                         new RuntimeException("Error 1"),
                                                         new RuntimeException("Error 2")
                                                     )
                                                 )
                                                 .build();

            PartialSuccessException ex = new PartialSuccessException(mixedResult);

            ResponseEntity<MixedResult> response = exceptionHandler.handlePartialSuccessException(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.MULTI_STATUS, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle PartialSuccessException with all successes")
        void shouldHandlePartialSuccessExceptionWithAllSuccesses()
        {
            MixedResult mixedResult = MixedResult.builder()
                                                 .successful(List.of(1L, 2L, 3L, 4L, 5L))
                                                 .exceptions(List.of())
                                                 .build();

            PartialSuccessException ex = new PartialSuccessException(mixedResult);

            ResponseEntity<MixedResult> response = exceptionHandler.handlePartialSuccessException(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.MULTI_STATUS, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("IOException Tests")
    class IOExceptionTests
    {
        @Test
        @DisplayName("Should handle IOException")
        void shouldHandleIOException()
        {
            IOException ex = new IOException(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleIOException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle IOException with cause")
        void shouldHandleIOExceptionWithCause()
        {
            IOException ex = new IOException(TEST_ERROR_MESSAGE, new RuntimeException("Root cause"));

            ResponseEntity<Object> response = exceptionHandler.handleIOException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle IOException with null message")
        void shouldHandleIOExceptionWithNullMessage()
        {
            IOException ex = new IOException();

            ResponseEntity<Object> response = exceptionHandler.handleIOException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Throwable Tests")
    class ThrowableTests
    {
        @Test
        @DisplayName("Should handle generic Throwable")
        void shouldHandleThrowable()
        {
            Throwable ex = new Throwable(TEST_ERROR_MESSAGE);

            ResponseEntity<Object> response = exceptionHandler.handleThrowableError(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(TEST_ERROR_MESSAGE, response.getBody());
        }

        @Test
        @DisplayName("Should handle Throwable with null message")
        void shouldHandleThrowableWithNullMessage()
        {
            Throwable ex = new Throwable();

            ResponseEntity<Object> response = exceptionHandler.handleThrowableError(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle Error subclass")
        void shouldHandleError()
        {
            Error ex = new Error("Critical system error");

            ResponseEntity<Object> response = exceptionHandler.handleThrowableError(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Tests")
    class MethodArgumentNotValidExceptionTests
    {
        private MethodParameter createMockMethodParameter()
        {
            try
            {
                // Create a real MethodParameter pointing to a test method
                java.lang.reflect.Method method = TestController.class.getMethod("testMethod", String.class);
                return new MethodParameter(method, 0);
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with multiple errors")
        void shouldHandleMethodArgumentNotValidExceptionWithMultipleErrors()
        {
            BindingResult bindingResult = mock(BindingResult.class);
            ObjectError error1 = new FieldError("user", "name", "Name must not be null");
            ObjectError error2 = new FieldError("user", "email", "Email must be valid");
            when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));

            MethodParameter parameter = createMockMethodParameter();
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with single error")
        void shouldHandleMethodArgumentNotValidExceptionWithSingleError()
        {
            BindingResult bindingResult = mock(BindingResult.class);
            ObjectError error = new FieldError("product", "price", "Price must be positive");
            when(bindingResult.getAllErrors()).thenReturn(List.of(error));

            MethodParameter parameter = createMockMethodParameter();
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with empty errors")
        void shouldHandleMethodArgumentNotValidExceptionWithEmptyErrors()
        {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getAllErrors()).thenReturn(List.of());

            MethodParameter parameter = createMockMethodParameter();
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with custom headers")
        void shouldHandleMethodArgumentNotValidExceptionWithCustomHeaders()
        {
            BindingResult bindingResult = mock(BindingResult.class);
            ObjectError error = new FieldError("order", "quantity", "Quantity must be greater than 0");
            when(bindingResult.getAllErrors()).thenReturn(List.of(error));

            MethodParameter parameter = createMockMethodParameter();
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Custom-Header", "CustomValue");

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex,
                headers,
                HttpStatus.BAD_REQUEST,
                webRequest
            );

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("CustomValue", response.getHeaders().getFirst("X-Custom-Header"));
        }
    }

    // Helper class for creating valid MethodParameter instances
    static class TestController
    {
        public void testMethod(String param)
        {
            // This method is only used for reflection in tests
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests
    {
        @Test
        @DisplayName("Should handle cascading exceptions consistently")
        void shouldHandleCascadingExceptionsConsistently()
        {
            IllegalArgumentException cause = new IllegalArgumentException("Invalid input");
            InternalServerErrorException ex = new InternalServerErrorException("Processing failed", cause);

            ResponseEntity<Object> response = exceptionHandler.handleInternalServerError(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response.getBody());
        }

        @Test
        @DisplayName("Should maintain consistent response structure across different exceptions")
        void shouldMaintainConsistentResponseStructure()
        {
            ResponseEntity<Object> response1 = exceptionHandler.handleIllegalArgumentException(
                new IllegalArgumentException(TEST_ERROR_MESSAGE),
                webRequest
            );

            ResponseEntity<Object> response2 = exceptionHandler.handleValidationException(
                new ValidationException(TEST_ERROR_MESSAGE),
                webRequest
            );

            assertNotNull(response1);
            assertNotNull(response2);
            assertEquals(response1.getStatusCode(), response2.getStatusCode());
            assertInstanceOf(ErrorResponse.class, response1.getBody());
            assertInstanceOf(ErrorResponse.class, response2.getBody());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests
    {
        @Test
        @DisplayName("Should handle exception with very long message")
        void shouldHandleExceptionWithVeryLongMessage()
        {
            String longMessage = "A".repeat(10000);
            IllegalArgumentException ex = new IllegalArgumentException(longMessage);

            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle exception with special characters in message")
        void shouldHandleExceptionWithSpecialCharactersInMessage()
        {
            String specialMessage = "Error: <script>alert('xss')</script> & \"quotes\" & 'apostrophes'";
            ValidationException ex = new ValidationException(specialMessage);

            ResponseEntity<Object> response = exceptionHandler.handleValidationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle exception with unicode characters")
        void shouldHandleExceptionWithUnicodeCharacters()
        {
            String unicodeMessage = "Error: 日本語 🚀 Ñoño";
            DataValidationException ex = new DataValidationException(unicodeMessage);

            ResponseEntity<Object> response = exceptionHandler.handleDataValidationException(ex, webRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }
}
