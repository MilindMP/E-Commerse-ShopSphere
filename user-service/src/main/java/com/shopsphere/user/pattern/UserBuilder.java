package com.shopsphere.user.pattern;

// import com.shopsphere.common.dto.UserDTO;
import com.shopsphere.user.entity.User;

// Builder Pattern - for creating User objects with flexible construction
public class UserBuilder {
    private final User user;

    public UserBuilder() {
        this.user = new User();
    }

    public UserBuilder email(String email) {
        user.setEmail(email);
        return this;
    }

    public UserBuilder username(String username) {
        user.setUsername(username);
        return this;
    }

    public UserBuilder password(String password) {
        user.setPassword(password);
        return this;
    }

    public UserBuilder firstname(String firstname) {
        user.setFirstname(firstname);
        return this;
    }

    public UserBuilder lastname(String lastname) {
        user.setLastname(lastname);
        return this;
    }

    public UserBuilder phone(String phone) {
        user.setPhone(phone);
        return this;
    }

    public User build() {
        return user;
    }
}
