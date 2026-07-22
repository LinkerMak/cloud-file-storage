package com.linkermak.cloud_file_storage.dto.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class StorageResource {
    private String path;
    private String name;
    private Long size;
    private StorageResourceType type;
}
