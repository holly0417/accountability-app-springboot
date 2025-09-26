package com.github.holly.accountability.User;
import com.github.holly.accountability.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void UserService_convertUserToDTO() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        UserDto userDto = userService.convertUserToDto(user);

        assertEquals(user.getUsername(), userDto.getUsername());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getId(), userDto.getId());
    }

    @Test
    public void UserService_findUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        userRepository.save(user);

        User savedUser = userService.findUserById(user.getId());

        assertEquals(savedUser.getId(), user.getId());
    }

    @Test
    public void UserService_findUserByEmail() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        when(userRepository.save(user)).thenReturn(user);
        userRepository.save(user);
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(userService.findUserByEmail(user.getEmail())).containsSame(user);
    }

    @Test
    public void UserService_findUsersByNameExceptOne() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setUsername("username2");
        secondUser.setPassword("Password2@app");
        secondUser.setEmail("email2@email");
        secondUser.setName("name2");

        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(secondUser)).thenReturn(secondUser);
        userRepository.save(user);
        userRepository.save(secondUser);

        String search = "username";
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userRepository.findUsersByUsernameContainsIgnoreCase(search)).thenReturn(List.of(user, secondUser));

        assertThat(userService.findUsersByNameExceptOne(search, user.getId())).doesNotContain(user);
        assertThat(userService.findUsersByNameExceptOne(search, user.getId())).contains(secondUser);
    }

    @Test
    public void UserRepository_save_ReturnNewUser() {

        // 1. data preparation
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        // 2. mocking calls if any
        // We need this because our Mock repository would
        // return null otherwise because we're not
        // touching our actual database!
        when(userRepository.save(user)).thenReturn(user);

        // 3. calling actual method
        User savedUser = userRepository.save(user);

        // 4. assertions
        assertNotNull(savedUser);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(1, (long) user.getId());
    }

    @Test
    public void userService_saveChangesToUser(){
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Password@app");
        user.setEmail("email@email");
        user.setName("name");

        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userRepository.save(user);

        savedUser.setEmail("changedemail@email");

        User changedUser = userService.saveChangesToUser(savedUser);
        assertEquals("changedemail@email", changedUser.getEmail());
    }

    @Test
    public void UserRepository_deleteUser() {

        doNothing().when(userRepository).deleteById(1L);
        userRepository.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

}

