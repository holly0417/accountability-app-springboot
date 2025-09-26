package com.github.holly.accountability.User;

import com.github.holly.accountability.config.GenericResponse;
import com.github.holly.accountability.config.properties.ApplicationProperties;
import com.github.holly.accountability.email.EmailService;
import com.github.holly.accountability.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;


    @Test
    public void EmailService_sendPasswordEmail() {
        RuntimeException userNotFound = assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordEmail("doesntexist@email");
        });

        assertEquals("User not found", userNotFound.getMessage());

        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app1");
        user.setEmail("email@email");
        user.setName("name");

        when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(applicationProperties.getBaseUrl()).thenReturn("http://localhost:8080");

        GenericResponse successResponse = emailService.sendPasswordEmail("email@email");

        assertEquals("Email sent!", successResponse.getMessage());

    }

    //test if a method will throw an exception if wrong input given
    @Test
    public void EmailService_setNewPassword_ThrowsException(){
        User user = new User();
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        when(passwordResetTokenRepository.save(passwordResetToken)).thenReturn(passwordResetToken);

        passwordResetTokenRepository.save(passwordResetToken);
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        GenericResponse successResponse = emailService.setNewPassword(token, "Changedpass@app1", "Changedpass@app1");

        assertEquals("Password updated successfully.", successResponse.getMessage());

        String unusedToken = UUID.randomUUID().toString();

        RuntimeException noTokenFound = assertThrows(RuntimeException.class, () -> {
            emailService.setNewPassword(unusedToken, "Changedpass@app1", "Changedpass@app1");
        });

        assertEquals("Token not found", noTokenFound.getMessage());

        ResponseStatusException passwordNotMatching = assertThrows(ResponseStatusException.class, () -> {
            emailService.setNewPassword(token, "notMatching", "Changedpass@app1");
        });

        assertEquals("Passwords do not match.", passwordNotMatching.getReason());

        ResponseStatusException passwordNotFormatted = assertThrows(ResponseStatusException.class, () -> {
            emailService.setNewPassword(token, "noformat", "noformat");
        });

        assertEquals("Password in wrong format", passwordNotFormatted.getReason());

        //checks if anything was saved in the userRepository using Mockito.any
        verify(userRepository, times(0)).save(any(User.class));

        //instead of times(0), we can also use never()
        verify(userRepository, never()).save(any(User.class));
    }

    //we don't normally test private methods in production. example for learning.
    @Test
    void UserService_validateEmail_testPrivateMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method validateEmail = EmailService.class.getDeclaredMethod("patternMatches", String.class, String.class);

        //java reflection
        validateEmail.setAccessible(true);

        Boolean email = (Boolean) validateEmail.invoke(emailService, "email@email", "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        assertTrue(email);
    }


}
