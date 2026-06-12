package com.shopsphere.user.pattern;

// Strategy Pattern - for password encoding strategies
public interface PasswordEncodingStrategy {
    String encode(String password);

    boolean matches(String rawPassword, String encodedPassword);
}
