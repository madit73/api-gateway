package com.madi.limitless.common.utility.auth.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.madi.limitless.common.utility.auth.enums.RoleType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * Role payload with optional privilege and user associations.
 */
@Getter
@Setter
@Validated
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO
{
    private Long roleId;
    private String roleName;
    private RoleType roleType;
    private List<PrivilegeDTO> privileges = null;
    private List<UserDTO> users = null;
}
