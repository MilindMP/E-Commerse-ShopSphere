package com.shopsphere.user.pattern;

import com.shopsphere.common.dto.UserDTO;
import com.shopsphere.user.entity.User;

// Factory Pattern - for creating DTOs from entities
public class UserDTOFactory {
    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getPhone(),
                user.getActive(),
                user.getCreatedAt());
    }
}
