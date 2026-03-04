package com.madi.common.utility.auth.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.madi.common.utility.auth.constant.GlobalAuthConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Login request payload containing user credentials and site context.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginModel
{

    @NotNull
    @Size(min = 1, max = 100)
    @JsonInclude
    private String username;

    @NotBlank
    @Size(min = GlobalAuthConstants.PASSWORD_MIN_LENGTH, max = GlobalAuthConstants.PASSWORD_MAX_LENGTH)
    @JsonInclude
    private String pswd;

    @Size(max = GlobalAuthConstants.SITE_CODE_LENGTH)
    @NotBlank
    private String siteCode;

    /**
     * Intentionally omits the password from logs.
     */
    @Override
    public String toString()
    {
        return "LoginModel{" + "username='" + username + "'; siteCode='" + siteCode + "'}";
    }
}
