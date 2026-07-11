package com.linkermak.cloud_file_storage.config.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.minio")
@Getter
@Setter
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
