package com.linkermak.cloud_file_storage.services.transfer;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.dto.transfer.service.PreparedFileUpload;
import com.linkermak.cloud_file_storage.dto.transfer.service.PreparedUpload;
import com.linkermak.cloud_file_storage.dto.transfer.web.DownloadedResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.StorageException;
import com.linkermak.cloud_file_storage.exceptions.loader.DuplicateUploadResourceException;
import com.linkermak.cloud_file_storage.exceptions.loader.MultipartFileEmptyException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import com.linkermak.cloud_file_storage.services.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileTransferServiceImpl implements FileTransferService {

    private static final int BYTE_BUFFER_SIZE = 8192;

    private final DirectoryService directoryService;
    private final ResourceService fileService;

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    @Override
    public DownloadedResource downloadResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);
        boolean isDirectory = trimmedPath.endsWith("/");
        return isDirectory
                ? downloadDirectory(path)
                : downloadFile(path);
    }

    private DownloadedResource downloadFile(String filePath) {
        String normalizedFilePath = pathPreparer.prepareFilePath(filePath);

        StorageDownloadObject downloadedFile =
                storageRepository.downloadFile(userProvider.currentUserId(), normalizedFilePath);

        return new DownloadedResource(
                StoragePathExtractor.extractLastPath(downloadedFile.fileName()),
                new InputStreamResource(downloadedFile.inputStream()),
                downloadedFile.size());
    }

    private DownloadedResource downloadDirectory(String directoryPath) {
        String normalizedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        List<StorageObjectInfo> resources = storageRepository
                .findResourcesRecursiveByPrefix(userId, normalizedDirectoryPath);

        byte[] zipBytes = createZip(userId, normalizedDirectoryPath, resources);

        return new DownloadedResource(
                directoryPath,
                new ByteArrayResource(zipBytes),
                zipBytes.length
        );
    }

    private byte[] createZip(Long userId, String directoryPath, List<StorageObjectInfo> resources) {
        try (ByteArrayOutputStream byteArrayOutputStream =
                     new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream =
                     new ZipOutputStream(byteArrayOutputStream)) {

            byte[] buffer = new byte[BYTE_BUFFER_SIZE];

            for (StorageObjectInfo objectInfo : resources) {
                String path = objectInfo.path();

                if (path.endsWith("/")) {
                    continue;
                }

                StorageDownloadObject downloadedFile = storageRepository
                        .downloadFile(userId, path);

                String zipEntryName = path.substring(directoryPath.length());
                try (InputStream inputStream = downloadedFile.inputStream()) {
                    zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, read);
                    }

                    zipOutputStream.closeEntry();
                }
            }

            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new StorageException("Failed to create zip archive for directory:" + directoryPath, e);
        }
    }

    @Override
    public List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files) throws IOException {
        PreparedUpload preparedUpload = prepareUpload(directoryPath, files);
        return executeUpload(preparedUpload);
    }

    private PreparedUpload prepareUpload(String directoryPath, List<MultipartFile> files) {
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);
        List<PreparedFileUpload> preparedFileUploads = prepareFileUploads(files);

        directoryService.validateDirectoryExists(preparedDirectoryPath);

        for (PreparedFileUpload preparedFileUpload : preparedFileUploads) {
            fileService.validateFileNotExists(preparedDirectoryPath
                    + preparedFileUpload.normalizedRelativePath());
        }

        return new PreparedUpload(
                preparedDirectoryPath,
                preparedFileUploads);
    }

    private List<PreparedFileUpload> prepareFileUploads(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new MultipartFileEmptyException("List multipart files is empty");
        }

        List<PreparedFileUpload> preparedFileUploads = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new MultipartFileEmptyException("Multipart file is empty");
            }

            String normalizeFilePath = pathPreparer.prepareFilePath(file.getOriginalFilename());

            if (!fileNames.add(normalizeFilePath)) {
                throw new DuplicateUploadResourceException(
                        "File path:" + normalizeFilePath + " are duplicated");
            }

            preparedFileUploads.add(new PreparedFileUpload(
                    file,
                    normalizeFilePath
            ));
        }
        return preparedFileUploads;
    }

    private List<StorageResource> executeUpload(PreparedUpload preparedUpload) throws IOException {
        Long userId = userProvider.currentUserId();
        List<StorageResource> storageResources = new ArrayList<>();

        for (PreparedFileUpload file : preparedUpload.files()) {

            String fullPath = preparedUpload.normalizedDirectoryPath()
                    + file.normalizedRelativePath();

            createParentDirectories(userId, fullPath);

            storageRepository.uploadFile(new UploadFileRequest(
                    userId,
                    fullPath,
                    file.source().getInputStream(),
                    file.source().getSize(),
                    file.source().getContentType()
            ));

            storageResources.add(new StorageResource(
                    StoragePathExtractor.extractParentPath(fullPath).orElse(""),
                    StoragePathExtractor.extractLastPath(fullPath),
                    file.source().getSize(),
                    StorageResourceType.FILE
            ));
        }

        return storageResources;
    }

    private void createParentDirectories(Long userId, String relativePath) {
        List<String> fileParentPaths = StoragePathExtractor
                .extractAllParentPaths(relativePath);

        for (String fileParentPath : fileParentPaths) {
            storageRepository.ensureDirectoryExists(userId, fileParentPath);
        }
    }

}
