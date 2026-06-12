package com.shopsphere.user.service;

import com.shopsphere.common.config.JwtTokenProvider;
import com.shopsphere.common.dto.UserDTO;
import com.shopsphere.common.exception.BusinessException;
import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.pattern.*;
import com.shopsphere.user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, UserDTO> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncodingStrategy passwordEncodingStrategy;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final long CACHE_EXPIRATION_MINUTES = 30;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email: {}", userDTO.getEmail());

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new BusinessException("User with email already exists");
        }

        // Using Builder Pattern
        User user = new UserBuilder()
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .password(passwordEncodingStrategy.encode(userDTO.getEmail())) // For demo
                .firstname(userDTO.getFirstname())
                .lastname(userDTO.getLastname())
                .phone(userDTO.getPhone())
                .build();

        User savedUser = userRepository.save(user);
        UserDTO result = UserDTOFactory.toDTO(savedUser);

        // Cache the user
        cacheUser(result);

        return result;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public UserDTO getUser(String userId) {
        log.info("Fetching user: {}", userId);

        // Try to get from cache first
        UserDTO cachedUser = redisTemplate.opsForValue().get(USER_CACHE_PREFIX + userId);
        if (cachedUser != null) {
            log.info("User found in cache");
            return cachedUser;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserDTO result = UserDTOFactory.toDTO(user);
        cacheUser(result);
        return result;
    }

    public String authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncodingStrategy.matches(password, user.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());
        return token;
    }

    @CircuitBreaker(name = "userService")
    public void updateUser(String userId, UserDTO userDTO) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userDTO.getFirstname() != null)
            user.setFirstname(userDTO.getFirstname());
        if (userDTO.getLastname() != null)
            user.setLastname(userDTO.getLastname());
        if (userDTO.getPhone() != null)
            user.setPhone(userDTO.getPhone());

        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        // Invalidate cache
        redisTemplate.delete(USER_CACHE_PREFIX + userId);
    }

    private void cacheUser(UserDTO userDTO) {
        redisTemplate.opsForValue().set(
                USER_CACHE_PREFIX + userDTO.getUserId(),
                userDTO,
                CACHE_EXPIRATION_MINUTES,
                TimeUnit.MINUTES);
    }

    public UserDTO getUserFallback(String userId, Exception ex) {
        log.error("Circuit breaker fallback triggered for user: {}", userId, ex);
        throw new BusinessException("User service temporarily unavailable");
    }
}
