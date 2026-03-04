package com.madi.common.utility.rest.controller;


import lombok.NoArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Base controller that applies a safe binder configuration to mitigate mass assignment.
 */
@NoArgsConstructor
public abstract class BaseController
{
    //   Add this to avoid fortify Mass Assignment: Insecure Binder Configuration

    /**
     * Disables data binding to unknown fields for all inheriting controllers.
     */
    @InitBinder
    public void noOpBinder(WebDataBinder binder)
    {
        binder.setDisallowedFields();
    }
}
