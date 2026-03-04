package com.madi.common.utility.auth.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.madi.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.common.utility.auth.enums.LockStatus;
import com.madi.common.utility.auth.enums.UserStatus;
import com.madi.common.utility.rest.security.SanitizeUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * User profile payload used by auth and user-management endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserDTO
{
    private Long userId;

    @NotBlank
    @Pattern(regexp = GlobalAuthConstants.LOGIN_REGEX)
    @Size(min = 1, max = 100)
    private String username;

    @Email
    @Size(min = 5, max = 100)
    private String email;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private String suffixCode;

    @JsonIgnore
    private String pwd;

    private String activationKey;

    private UserStatus status;

    private String phoneNumber;

    private List<RoleDTO> roles = null;

    private LocalDate pwdExpirationDate;

    private LocalDate registrationDate;

    private LocalDateTime lastLoginDate;

    private LockStatus lockStatus;

    private LocalDateTime inactiveTime;

    private LocalDateTime purgeTime;

    private String createdBy;

    private LocalDateTime createdDate;

    private String updateBy;

    private LocalDateTime updateDate;

    /**
     * Sanitizes activation key input to prevent unsafe HTML content.
     */
    public void setActivationKey(String activationKey)
    {
        this.activationKey = SanitizeUtil.sanitizeAllowCommonFormat(activationKey);
    }

    /**
     * Uses JSON-style formatting for concise debug logging.
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
