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
import com.madi.limitless.common.utility.rest.controller.errors.general.ErrorUtil;
import com.madi.limitless.common.utility.rest.controller.errors.general.MixedResult;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Validated
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler(value = { MultipleExceptionsException.class })
    protected ResponseEntity<Object> handleMultipleExceptionsException(
        MultipleExceptionsException ex, WebRequest request
    )
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation =
            "The file provided has more than one observation, and one or all failed with different exceptions.";

        Map<String, String> exceptions = new HashMap<>();
        ex.getExceptions().forEach(exception ->
            exceptions.put(exception.getClass().getSimpleName(), Arrays.toString(exception.getStackTrace())));
        String action = exceptions.toString();
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { NotImplementedException.class })
    protected ResponseEntity<Object> handleNotImplementedExceptionViolation(
        NotImplementedException ex,
        WebRequest request
    )
    {
        HttpStatus status = HttpStatus.NOT_IMPLEMENTED;
        String
            explanation
            =
            "The requested operation has not been implemented. This error typically occurs because the current system or application "
                + "does not support the requested feature or method at this time. It may be included in future versions of the software.";
        String
            action
            =
            "Please contact our support team for more information about the availability of this feature. Provide them with the context"
                + " of your request, and they may offer alternative solutions or workarounds in the meantime.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { IllegalTimePeriodException.class })
    protected ResponseEntity<Object> handleIllegalTimePeriodError(IllegalTimePeriodException ex, WebRequest request)
    {
        // pretty sure this is what this one would get
        HttpStatus status = HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS;
        String
            explanation
            =
            "The time period specified in the request is invalid or not allowed. This could be due to the time period being too broad, "
                + "conflicting with existing reservations, or outside of operational hours.";

        String
            action
            =
            "Please review the time period requirements and constraints, adjust your request to fit within these parameters, and try "
                + "again. If you need further assistance, contact support with your request details.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { InternalServerErrorException.class })
    protected ResponseEntity<Object> handleInternalServerError(InternalServerErrorException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String explanation = "An unexpected error occurred on the server while processing the request.";
        String
            action
            = "Please try your request again later. If the problem persists, contact support with the details of your request.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { AuthenticationException.class })
    protected ResponseEntity<Object> handleAuthenticationErrors(AuthenticationException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String explanation = "Authentication failed due to invalid credentials or missing authentication information.";
        String
            action
            =
            "Please ensure your credentials are correct and present, and try your request again. If you believe this is an error, "
                + "contact support.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String
            explanation
            = "You do not have permission to access the requested resource or perform the requested operation.";
        String
            action
            = "If you believe you should have access, please check your permissions or contact support for assistance.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(
        ConstraintViolationException ex, WebRequest request
    )
    {
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        Set<String> messages = HashSet.newHashSet(constraintViolations.size());
        messages.addAll(constraintViolations.stream()
                                            .map(constraintViolation -> String.format(
                                                "%s",
                                                constraintViolation.getMessage()
                                            ))
                                            .toList());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "One or more validation constraints have been violated.";
        String action =
            "Please review the provided data for correctness and ensure all required fields meet the validation requirements.\n"
                + messages;
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "The request contains an argument that is not valid.";
        String action = "Please review the request parameters and ensure all arguments are correct.";

        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { EntityNotFoundException.class })
    protected ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String
            explanation
            =
            "The requested resource could not be found. This may be due to the resource being deleted, or the provided identifier is "
                + "incorrect.";
        String
            action
            =
            "Please verify that the resource identifier is correct and try your request again. If you believe this is an error, contact"
                + " support with the request details.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { EntityDataNotFoundException.class })
    protected ResponseEntity<Object> handleEntityDataNotFoundException(
        EntityDataNotFoundException ex, WebRequest request
    )
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "The requested data was not found in the system.";
        String
            action
            =
            "Please verify the identifiers or query parameters used and try again. If the issue persists, contact support for further "
                + "assistance.";

        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { DataValidationException.class })
    protected ResponseEntity<Object> handleDataValidationException(DataValidationException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "The provided data failed to pass validation checks.";
        String
            action
            = "Please review the provided data for accuracy and completeness according to the specified requirements.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { ValidationException.class })
    protected ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "The provided data failed to pass validation checks.";
        String
            action
            = "Please review the provided data for accuracy and completeness according to the specified requirements.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String
            explanation
            = "The requested resource is not available. It may have been removed or you may have used an incorrect identifier.";
        String
            action
            =
            "Verify the identifier or parameters you have used to request the resource. If you believe this resource should exist, "
                + "please contact support with the details of your request.";

        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex, WebRequest request
    )
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String explanation = "One of the request parameters is not of the expected type.";
        String
            action
            =
            "Please review the request parameters to ensure they are in the correct format. Refer to the API documentation for the "
                + "correct types and formats of all parameters.";

        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(DeleteEntityConflictException.class)
    protected ResponseEntity<Object> handleDeleteEntityConflict(DeleteEntityConflictException ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.CONFLICT;
        String
            explanation
            = "The entity cannot be deleted as it is currently referenced by other entities or resources.";
        String
            action
            =
            "Please ensure that all references to this entity are removed before attempting deletion again. If you're unsure how to "
                + "proceed, consult the documentation or contact support for guidance.";
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(PartialSuccessException.class)
    protected ResponseEntity<MixedResult> handlePartialSuccessException(PartialSuccessException ex)
    {
        return new ResponseEntity<>(ex.getMixedResult(), new HttpHeaders(), HttpStatus.MULTI_STATUS);
    }

    @ExceptionHandler(value = { IOException.class })
    protected ResponseEntity<Object> handleIOException(
        @NotNull IOException ex,
        @NotNull WebRequest request
    )
    {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String
            action
            =
            "Please ensure the file contents are valid. If you're unsure how to proceed, consult the documentation or contact support "
                + "for guidance.";
        String explanation = "Unable handle this file. error message: "
            + ex.getMessage();
        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), status, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = { Throwable.class })
    protected ResponseEntity<Object> handleThrowableError(final Throwable ex)
    {
        String bodyOfResponse = ex.getLocalizedMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(bodyOfResponse, new HttpHeaders(), status);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    )
    {
        List<ObjectError> listOfErrors = ex.getBindingResult().getAllErrors();
        StringBuilder errorMessage = new StringBuilder();

        for (ObjectError error : listOfErrors)
        {
            errorMessage.append(error.getDefaultMessage());
            errorMessage.append("\n");
        }
        String explanation = "One or more arguments in the request are invalid.\n" + errorMessage;
        String
            action
            = "Please review the request parameters and ensure they conform to the expected formats and constraints.";

        ErrorResponse errorResponse = ErrorUtil.createErrorResponse(ex, explanation, action);

        return new ResponseEntity<>(errorResponse, headers, status);
    }
}
