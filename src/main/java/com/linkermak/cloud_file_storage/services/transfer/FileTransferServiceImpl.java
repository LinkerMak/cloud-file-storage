package com.linkermak.cloud_file_storage.services.transfer;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.dto.transfer.service.PreparedFileUpload;
import com.linkermak.cloud_file_storage.dto.transfer.service.PreparedUpload;
import com.linkermak.cloud_file_storage.dto.transfer.web.DownloadedResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.exceptions.loader.DuplicateUploadResourceException;
import com.linkermak.cloud_file_storage.exceptions.loader.MultipartFileEmptyException;
import com.linkermak.cloud_file_storage.mappers.StorageResourceMapper;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import com.linkermak.cloud_file_storage.services.resource.ResourceService;
import com.linkermak.cloud_file_storage.services.zip.ZipArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileTransferServiceImpl implements FileTransferService {

    private final DirectoryService directoryService;
    private final ResourceService resourceService;
    private final ZipArchiveService zipArchiveService;

    private final ResourceStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    private final StorageResourceMapper resourceMapper;

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
                .findDescendantsByPrefix(userId, normalizedDirectoryPath);

        byte[] zipBytes = zipArchiveService.
                createZip(userId, normalizedDirectoryPath, resources);

        return new DownloadedResource(
                normalizedDirectoryPath,
                new ByteArrayResource(zipBytes),
                zipBytes.length
        );
    }



    @Override
    public List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files) throws IOException {
        PreparedUpload preparedUpload = prepareUpload(directoryPath, files);
        return executeUpload(preparedUpload);
    }

    private PreparedUpload prepareUpload(String directoryPath, List<MultipartFile> files) {
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);
        List<PreparedFileUpload> preparedFileUploads = prepareFileUploads(files);

        directoryService.validatePreparedDirectoryExists(preparedDirectoryPath);

        for (PreparedFileUpload preparedFileUpload : preparedFileUploads) {
            resourceService.validatePreparedFileNotExists(preparedDirectoryPath
                    + preparedFileUpload.preparedRelativeFilePath());
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

            String preparedFilePath = pathPreparer.prepareFilePath(file.getOriginalFilename());

            if (!fileNames.add(preparedFilePath)) {
                throw new DuplicateUploadResourceException(
                        "File path:" + preparedFilePath + " are duplicated");
            }

            preparedFileUploads.add(new PreparedFileUpload(
                    file,
                    preparedFilePath
            ));
        }
        return preparedFileUploads;
    }

    private List<StorageResource> executeUpload(PreparedUpload preparedUpload) throws IOException {
        Long userId = userProvider.currentUserId();
        List<StorageResource> storageResources = new ArrayList<>();

        for (PreparedFileUpload file : preparedUpload.files()) {

            String fullPath = preparedUpload.preparedDirectoryPath()
                    + file.preparedRelativeFilePath();

            createParentDirectories(userId, fullPath);

            storageRepository.uploadFile(new UploadFileRequest(
                    userId,
                    fullPath,
                    file.source().getInputStream(),
                    file.source().getSize(),
                    file.source().getContentType()
            ));

            storageResources.add(
                    resourceMapper.toFileResource(fullPath, file.source().getSize())
            );
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
