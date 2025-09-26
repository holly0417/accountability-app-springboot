package com.github.holly.accountability.email;

import com.github.holly.accountability.config.GenericResponse;
import com.github.holly.accountability.config.properties.ApplicationProperties;
import com.github.holly.accountability.user.PasswordResetToken;
import com.github.holly.accountability.user.PasswordResetTokenRepository;
import com.github.holly.accountability.user.User;
import com.github.holly.accountability.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.github.holly.accountability.email.EmailController.CHANGE_PASSWORD_FROM_TOKEN;

@Component
public class EmailService {

    private final UserService userService;
    private final ApplicationProperties applicationProperties;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmailService(UserService userService,
                        ApplicationProperties applicationProperties,
                        JavaMailSender mailSender,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.applicationProperties = applicationProperties;
        this.mailSender = mailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public GenericResponse sendPasswordEmail(String email)
            throws ResponseStatusException {

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = createPasswordResetTokenForUser(user);
        mailSender.send(constructResetTokenEmail(token, user));

        return new GenericResponse("Email sent!", "no");
    }

    public GenericResponse setNewPassword(String token,
                                          String newPassword,
                                          String newPasswordRepeated)
            throws ResponseStatusException {

        PasswordResetToken thisToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow( () -> new RuntimeException("Token not found") );

        // Find the user by username or throw an exception if not found
        User user = thisToken.getUser();

        if(user == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }

        // Check if the current password matches
        if (!Objects.equals(newPassword, newPasswordRepeated)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        if (!patternMatches(newPassword, "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,20}$")) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password in wrong format");
        }

        // Encode and set the new password, then save the user
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveChangesToUser(user);

        return new GenericResponse("Password updated successfully.", "no");
    }

    private SimpleMailMessage constructEmail(String subject, String body,
                                            User user) {

        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("hoyeonyoo@gmail.com");
        return email;
    }

    private SimpleMailMessage constructResetTokenEmail(
            String token, User user) {

        String url = UriComponentsBuilder
                .fromUriString(applicationProperties.getBaseUrl())
                .path("/email" + CHANGE_PASSWORD_FROM_TOKEN)
                .pathSegment("{token}")
                .buildAndExpand(token)
                .toUriString();

        String message = """
           Click the link below to reset your password:
           %s""".formatted(url);

        return constructEmail("Reset Password", message, user);
    }

    private String createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
        return token;
    }


    public boolean validatePasswordResetToken(String token) throws ResponseStatusException {
        final Optional<PasswordResetToken> passToken = passwordResetTokenRepository.findByToken(token);
        return passToken.isPresent() && !isTokenExpired(passToken.get());
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.getExpiryDate().isBefore(LocalDateTime.now());
    }

    private static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }

}
