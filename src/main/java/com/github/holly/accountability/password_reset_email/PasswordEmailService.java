package com.github.holly.accountability.password_reset_email;

import com.github.holly.accountability.config.GenericResponse;
import com.github.holly.accountability.config.properties.ApplicationProperties;
import com.github.holly.accountability.user.PasswordResetToken;
import com.github.holly.accountability.user.PasswordResetTokenRepository;
import com.github.holly.accountability.user.User;
import com.github.holly.accountability.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.github.holly.accountability.password_reset_email.PasswordEmailController.CHANGE_PASSWORD_FROM_TOKEN;

@Component
public class PasswordEmailService {

    private final UserService userService;
    private final ApplicationProperties applicationProperties;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordEmailService(UserService userService,
                                ApplicationProperties applicationProperties,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.applicationProperties = applicationProperties;
        this.mailSender = mailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public class PasswordsNotMatchingException extends RuntimeException {
        public PasswordsNotMatchingException(String message) {
            super(message);
        }
    }

    public GenericResponse sendPasswordEmail(String email)
            throws ResponseStatusException, MailSendException {

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = createPasswordResetTokenForUser(user);
        mailSender.send(constructResetTokenEmail(token, user));

        return new GenericResponse("Email sent!");
    }

    public ResetPasswordDto setNewPassword(String token, ResetPasswordDto passwordDto) throws RuntimeException {

        Optional<PasswordResetToken> thisToken = passwordResetTokenRepository.findByToken(token);

        if (thisToken.isPresent()) {

            User user = thisToken.get().getUser();

            if (Objects.equals(passwordDto.getPassword(), passwordDto.getPasswordRepeated())) {
                user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
                userService.saveChangesToUser(user);
                return passwordDto;
            }

            throw new PasswordsNotMatchingException("Passwords do not match");

        }

        throw new RuntimeException("Unexpected error occurred");
    }

    private SimpleMailMessage constructEmail(String subject, String body,
                                            User user) {

        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
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

}
