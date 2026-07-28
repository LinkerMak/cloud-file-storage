package com.linkermak.cloud_file_storage.controllers.resource;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(
        name = "Resource API",
        description = "Операции над ресурсами. Для всех методов параметр path — полный путь к ресурсу в URL-encoded формате." +
                " Для всех запросов ниже, параметр path - полный путь к ресурсу в url-encoded формате. Путь к папке должен заканчиваться на /."
)
public interface ResourceApi {

    @Operation(
            summary = "Получение информации о ресурсе",
            description = "Возвращает информацию о ресурсе."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Информация о ресурсе",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageResource.class)
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
                            description = "Ресурс не найден",
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
    ResponseEntity<StorageResource> getResourceInfoByPath(
            @Parameter(
                    description = "Полный путь к ресурсу в url-encoded формате.",
                    example = "folder1/folder2/file.txt для файла или folder1/folder2/ для папки",
                    required = true
            )
            @RequestParam("path") String path
    );

    @Operation(
            summary = "Удаление ресурса",
            description = "Удаляет ресурс"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Ресурс успешно удален",
                            content = @Content(schema = @Schema(hidden = true))

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
                            description = "Ресурс не найден",
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
    @DeleteMapping
    ResponseEntity<Void> deleteResource(
            @Parameter(
                    description = "Полный путь к ресурсу в url-encoded формате.",
                    example = "folder1/folder2/file.txt для файла или folder1/folder2/ для папки",
                    required = true
            )
            @RequestParam("path") String path
    );

    @Operation(
            summary = "Поиск ресурсов",
            description = "Поиск ресурсов по заданному запросу"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Коллекция ресурсов, удовлетворяющих запросу",
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
                            description = "Ресурс не найден",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ресурс, лежащий по пути to уже существует",
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
    @GetMapping("/search")
    ResponseEntity<List<StorageResource>> searchResources(
            @Parameter(
                    description = "Слово запрос, по которому будет происходить поиск ресурсов",
                    example = "word",
                    required = true
            )
            @RequestParam("query") String query
    );


    @Operation(
            summary = "Перемещение/переименовывание ресурса",
            description = "Перемещает/переименовывает ресурс.  При переименовании меняется только имя файла, при перемещении — только путь."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Информация о ресурсе",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageResource.class)
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
                            description = "Ресурс не найден",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ресурс, лежащий по пути to уже существует",
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
    @PostMapping("/move")
    ResponseEntity<StorageResource> moveResource(
            @Parameter(
                    description = "Старый полный путь к ресурсу в URL-encoded формате.",
                    example = "folder1/folder2/file.txt для файла или folder1/folder2/ для папки",
                    required = true
            )
            @RequestParam("from") String from,
            @Parameter(
                    description = "Новый полный путь к ресурсу в URL-encoded формате.",
                    example = "folder1/folder2/file.txt для файла или folder1/folder2/ для папки",
                    required = true
            )
            @RequestParam("to") String to);
}
