package com.linkermak.cloud_file_storage.controllers.directory;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.exception.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(
        name = "Directory API"
)
public interface DirectoryApi {

    @Operation(
            summary = "Создать новую папку",
            description = "Создает новую папку"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Папка успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StorageResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидный или отсутствующий путь к новой папке",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
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
                    responseCode = "404",
                    description = "Родительская папка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Папка уже существует",
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
    @PostMapping
    ResponseEntity<StorageResource> createDirectory(
            @Parameter(
                    description = "Полный путь к папке в url-encoded формате. Должен заканчиваться на /",
                    example = "folder1/folder2/",
                    required = true
            )
            @RequestParam("path") String path
    );


    @Operation(
            summary = "Получение информации о содержимом папки",
            description = "Возвращает коллекцию ресурсов лежащих в папке(не рекурсивно)"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Коллекция ресурсов, лежащих в папке",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = StorageResource.class)
                                    )
                            )

                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидный или отсутствующий путь",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
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
                            responseCode = "404",
                            description = "Папка не существует",
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
            }
    )
    @GetMapping
    ResponseEntity<List<StorageResource>> getAllResourcesInDirectory(
            @Parameter(
                    description = "Полный путь к папке в url-encoded формате. Должен заканчиваться на /",
                    example = "folder1/folder2/",
                    required = true
            )
            @RequestParam(value = "path", defaultValue = "") String path
    );

}
