package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {
    private String userId;
    private String email;
    private String password;
    private String username;
    private String firstname;
    private String lastname;
    private String phone;
    private Boolean active;
    private Long createdAt;
}
