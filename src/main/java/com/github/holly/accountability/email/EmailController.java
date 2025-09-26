package com.github.holly.accountability.email;

import com.github.holly.accountability.config.GenericResponse;
import com.github.holly.accountability.user.PasswordResetToken;
import com.github.holly.accountability.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Controller
@RequestMapping("/email")
public class EmailController {

    public static final String CHANGE_PASSWORD_FROM_TOKEN = "/change-password-from-token";

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public EmailController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @ResponseBody
    @GetMapping("/send-token")
    public GenericResponse sendPasswordEmail(@RequestParam String email) {
        try {
            return emailService.sendPasswordEmail(email);
        } catch (Exception e) {
            return new GenericResponse(e.getMessage());
        }
    }

    @GetMapping(CHANGE_PASSWORD_FROM_TOKEN + "/{token}")
    public String showChangePasswordFromTokenPage(@PathVariable("token") String token) {
        boolean isValid = emailService.validatePasswordResetToken(token);
        if (!isValid) {
            return "redirect:/login";
        }
        return "redirect:%s".formatted(CHANGE_PASSWORD_FROM_TOKEN);
    }

    //change this so it's a JSON object being given with a PostMapping
    @ResponseBody
    @GetMapping("/set-new-password")
    public GenericResponse changePasswordFromToken(@RequestParam("token") String token,
                                                   @RequestParam("password") String newPassword,
                                                   @RequestParam("passwordRepeated") String newPasswordRepeated) {
        try {
            return emailService.setNewPassword(token, newPassword, newPasswordRepeated);
        } catch (Exception e) {
            return new GenericResponse(e.getMessage(), "yes");
        }
    }

}
