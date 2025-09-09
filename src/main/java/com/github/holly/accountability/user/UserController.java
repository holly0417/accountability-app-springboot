//WHY: to be able to get the current user's ID and Username
//whenever it's needed in the frontend without providing password

package com.github.holly.accountability.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
@RequestMapping("/api/user")
@ResponseBody
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    @GetMapping("")
    public UserDto getUser(@AuthenticationPrincipal AccountabilitySessionUser user) {

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());

        return userDto;
    }

    @GetMapping("/all-platform-users")
    public Page<UserDto> getAllPlatformUsers(@AuthenticationPrincipal AccountabilitySessionUser user,
                                             @PageableDefault(size = 20) Pageable pageable) {

        return userRepository.getAllExceptCurrentUser(user.getId(), pageable)
                .map(res -> userService.convertUserToDto(res));
    }

}
