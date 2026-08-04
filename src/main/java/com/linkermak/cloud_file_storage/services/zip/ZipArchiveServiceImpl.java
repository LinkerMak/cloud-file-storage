package com.linkermak.cloud_file_storage.services.zip;

import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.exceptions.repository.StorageException;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZipArchiveServiceImpl implements ZipArchiveService {

    private static final int BYTE_BUFFER_SIZE = 8192;

    private final ResourceStorageRepository storageRepository;

    public byte[] createZip(Long userId, String directoryPath, List<StorageObjectInfo> resources) {
        log.info("Create zip started: userId={}, directoryPath={}, resourcesCount={}",
                userId, directoryPath, resources != null ? resources.size() : null);

        try (ByteArrayOutputStream byteArrayOutputStream =
                     new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream =
                     new ZipOutputStream(byteArrayOutputStream)) {

            byte[] buffer = new byte[BYTE_BUFFER_SIZE];

            for (StorageObjectInfo objectInfo : resources) {
                String objectPath = objectInfo.path();

                if (objectPath.endsWith("/")) {
                    continue;
                }

                writeFileToZip(
                        userId,
                        objectPath,
                        directoryPath,
                        zipOutputStream,
                        buffer
                );
            }

            zipOutputStream.finish();
            byte[] result = byteArrayOutputStream.toByteArray();

            log.info("Create zip completed: userId={}, directoryPath={}, zippedSize={}",
                    userId, directoryPath, result.length);

            return result;
        } catch (Exception e) {
            throw new StorageException("Failed to create zip archive for directory:" + directoryPath, e);
        }
    }

    private void writeFileToZip(Long userId,
                                String filePath,
                                String directoryPath,
                                ZipOutputStream zipOutputStream,
                                byte[] buffer) throws IOException {
        log.debug("Write file to zip started: userId={}, filePath={}, directoryPath={}",
                userId, filePath, directoryPath);

        StorageDownloadObject downloadedFile = storageRepository
                .downloadFile(userId, filePath);

        String zipEntryName = filePath.substring(directoryPath.length());
        try (InputStream inputStream = downloadedFile.inputStream()) {
            zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, read);
            }

            zipOutputStream.closeEntry();
        }

        log.debug("Write file to zip completed: userId={}, filePath={}, zipEntryName={}",
                userId, filePath, zipEntryName);
    }
}
