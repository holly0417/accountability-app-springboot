package com.github.holly.accountability.password_reset_email;

import com.github.holly.accountability.config.ErrorResponse;
import com.github.holly.accountability.config.GenericResponse;
import com.github.holly.accountability.config.properties.ApplicationProperties;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/email")
public class PasswordEmailController {

    private final ApplicationProperties applicationProperties;
    public static final String CHANGE_PASSWORD_FROM_TOKEN = "/change-password-from-token";

    private final PasswordEmailService passwordEmailService;

    @Autowired
    public PasswordEmailController(PasswordEmailService passwordEmailService,
                                   ApplicationProperties applicationProperties) {
        this.passwordEmailService = passwordEmailService;
        this.applicationProperties = applicationProperties;
    }

    @ResponseBody
    @GetMapping("/send-token")
    public GenericResponse sendPasswordEmail(@RequestParam String email) {
        try {
            return passwordEmailService.sendPasswordEmail(email);
        } catch (Exception e) {
            return new GenericResponse(e.getMessage());
        }
    }

    @GetMapping(CHANGE_PASSWORD_FROM_TOKEN + "/{token}")
    public ResponseEntity<?>  showChangePasswordFromTokenPage(@PathVariable("token") String token) {
        boolean isValid = passwordEmailService.validatePasswordResetToken(token);

        if (!isValid) {

            String loginUrl = UriComponentsBuilder
                    .fromUriString(applicationProperties.getBaseUrl())
                    .path("/login")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Location", loginUrl)
                    .build();
        }

        String tokenPasswordChangeUrl = UriComponentsBuilder
                .fromUriString(applicationProperties.getBaseUrl())
                .path(CHANGE_PASSWORD_FROM_TOKEN)
                .queryParam("token", token)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", tokenPasswordChangeUrl)
                .build();
    }

    @ResponseBody
    @PostMapping("/set-new-password")
    public ResponseEntity<?> changePasswordFromToken(@RequestBody @Valid ResetPasswordDto passwordDto,
                                                     BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Validation failed", errorMap));
        }

        try {
            passwordEmailService.setNewPassword(passwordDto.getToken(), passwordDto);
            return ResponseEntity
                    .ok(new ErrorResponse(
                            "Password changed successfully!"));

        } catch (PasswordEmailService.PasswordsNotMatchingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Passwords do not match"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }


}
