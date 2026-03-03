package com.madi.limitless.common.utility.rest.controller;


import lombok.NoArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@NoArgsConstructor
public abstract class BaseController
{
    //   Add this to avoid fortify Mass Assignment: Insecure Binder Configuration
    @InitBinder
    public void noOpBinder(WebDataBinder binder)
    {
        binder.setDisallowedFields();
    }
}