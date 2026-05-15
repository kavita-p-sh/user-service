package com.ecommerce.user.dto;


import com.ecommerce.common.util.RegexConstant;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @Pattern(regexp = RegexConstant.USERNAME, message ="{user.username.valid}")
    private String username;

    @Pattern(regexp = RegexConstant.EMAIL, message = "{user.email.valid}")
    private String email;

    @Pattern(regexp = RegexConstant.PASSWORD, message = "{user.password.pattern}")
    private String password;

    @Pattern(regexp = RegexConstant.PHONE, message = "{phone.format}")
    private String phoneNumber;
}
