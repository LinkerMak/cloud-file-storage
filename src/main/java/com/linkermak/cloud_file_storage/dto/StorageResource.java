package com.linkermak.cloud_file_storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageResource {
    private String path;
    private String name;
    private Long size;
    private StorageResourceType type;
}
