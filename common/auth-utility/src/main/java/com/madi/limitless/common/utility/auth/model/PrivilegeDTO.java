package com.madi.limitless.common.utility.auth.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivilegeDTO
{
    private Long privilegeId;

    @NotNull
    private String privilegeName;

    private List<RoleDTO> roles;
}
