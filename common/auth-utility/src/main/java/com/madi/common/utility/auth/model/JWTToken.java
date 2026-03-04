package com.madi.common.utility.auth.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JSON-serializable wrapper for an ID token returned by authentication endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JWTToken
{
    @JsonProperty("id_token")
    private String idToken;
}
