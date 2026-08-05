package com.linkermak.cloud_file_storage.controllers.user;

import com.linkermak.cloud_file_storage.dto.web.authentication.response.UsernameResponse;
import com.linkermak.cloud_file_storage.dto.web.exception.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;

@
        Tag(
        name = "User API"
)
public interface UserApi {

    @Operation(
            summary = "Получение информации о текущем пользователе",
            description = "Возвращает имя текущего авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Текущий пользователь успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsernameResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
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
    @GetMapping("/user/me")
    ResponseEntity<UsernameResponse> showUser(
            @Parameter(
                    hidden = true,
                    description = "Текущий авторизованный пользователь, получаемый из security context"
            )
            @AuthenticationPrincipal UserDetails userDetails
    );
}