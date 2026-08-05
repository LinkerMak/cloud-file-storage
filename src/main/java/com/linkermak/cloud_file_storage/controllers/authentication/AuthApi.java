package com.linkermak.cloud_file_storage.controllers.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.response.UsernameResponse;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignInRequest;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.dto.web.exception.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

@Tag(
        name = "Authentication API"
)
public interface AuthApi {

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создает нового пользователя, сразу авторизует его и выставляет session cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsernameResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки валидации тела запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username занят",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Неизвестная ошибка",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<UsernameResponse> register(
            @RequestBody(
                    required = true,
                    description = "Данные для регистрации",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequest.class)
                    )
            )
            @Validated SignUpRequest signUpRequest,

            @Parameter(
                    hidden = true,
                    description = "HTTP-запрос, нужен для извлечения старой session cookie"
            )
            HttpServletRequest servletRequest
    );

    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет логин и пароль, создает сессию и выставляет session cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsernameResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки валидации тела запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверные данные",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Неизвестная ошибка",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<UsernameResponse> login(
            @RequestBody(
                    required = true,
                    description = "Данные для авторизации",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignInRequest.class)
                    )
            )
            @Validated SignInRequest request
    );

    @Operation(
            summary = "Выход из аккаунта",
            description = "Удаляет сессию пользователя и очищает session cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно вышел из аккаунта"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Запрос исполняется неавторизованным пользователем",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Неизвестная ошибка",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<Void> logout(
            @Parameter(
                    hidden = true,
                    description = "HTTP-запрос, нужен для извлечения session cookie"
            )
            HttpServletRequest request
    );
}
