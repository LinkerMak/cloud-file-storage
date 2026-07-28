package com.linkermak.cloud_file_storage.controllers.resource.loader;

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
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(
        name = "Resource Loader API"
)
public interface ResourceLoaderApi {

    @Operation(
            summary = "Загрузка ресурсов",
            description = "Загружает ресурс(ы) по указанному пути"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Информация о загруженных ресурсах",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = StorageResource.class)
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидное тело запроса",
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
                            responseCode = "409",
                            description = "Файл уже существует",
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<StorageResource>> uploadResource(
            @Parameter(
                    description = "path - путь к папке, в которую мы загружаем ресурс(ы)",
                    example = "folder1/folder2/",
                    required = true
            )
            @RequestParam("path") String path,
            @Parameter(
                    description = "Список ресурсов для загрузки в хранилище",
                    required = true
            )
            @RequestParam("object") List<MultipartFile> object
    ) throws IOException;

    @Operation(
            summary = "Скачивание ресурса",
            description = "Скачивает ресурс. Если ресурс является файлом скачивает его напрямую. Если ресурс является папкой скачивает как zip архив"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Информация о ресурсе",
                            content = @Content(
                                    mediaType = "application/octet-stream",
                                    schema = @Schema(implementation = Resource.class)
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
    @GetMapping("/download")
    ResponseEntity<Resource> downloadResource(
            @Parameter(
                    description = "Полный путь к ресурсу в url-encoded формате.",
                    example = "folder1/folder2/file.txt для файла или folder1/folder2/ для папки",
                    required = true
            )
            @RequestParam("path") String path
    );

}
