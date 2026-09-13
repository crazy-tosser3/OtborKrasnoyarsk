package com.example.balloon.controller;

import com.example.balloon.model.dto.user.*;
import com.example.balloon.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "Регистрация, авторизация и профиль")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Вход по логину и паролю")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создание нового пользователя")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", userService.register(request)));
    }

    @PostMapping("/update")
    @Operation(summary = "Обновить пользователя", description = "Изменение логина и пароля пользователя")
    public ResponseEntity<Map<String, String>> update(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(Map.of("message", userService.update(request)));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить пользователя", description = "Удаление пользователя по логину и паролю")
    public ResponseEntity<Map<String, String>> delete(@RequestBody UserDeleteRequest request) {
        return ResponseEntity.ok(Map.of("message", userService.delete(request)));
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Получить профиль пользователя", description = "Получение профиля по имени пользователя")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }
}
