package com.madi.limitless.common.utility.rest.controller.errors.exception;


import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * Aggregates multiple exceptions into a single runtime exception for batch processing.
 */
@Setter
@Getter
public class MultipleExceptionsException extends RuntimeException
{
    private final List<Throwable> exceptions;
    private static final String MESSAGE = "This file has multiple exceptions associated to it.";

    /**
     * Builds a combined error message listing all underlying exceptions.
     */
    public MultipleExceptionsException(List<Throwable> exceptions, String message)
    {
        // Precompute the final message before calling super()
        super(buildMessage(exceptions, message));
        this.exceptions = exceptions;
    }

    private static String buildMessage(List<Throwable> exceptions, String message)
    {
        StringBuilder messageBuilder = new StringBuilder(
            StringUtils.hasText(message) ? message : MESSAGE
        );
        messageBuilder.append("\n");
        exceptions.forEach(e -> messageBuilder.append(e.getClass().getName())
                                              .append(": ")
                                              .append(e.getMessage())
                                              .append("\n"));
        return messageBuilder.toString();
    }
}
