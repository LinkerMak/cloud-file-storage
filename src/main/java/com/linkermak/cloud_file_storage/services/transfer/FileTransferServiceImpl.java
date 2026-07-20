package com.linkermak.cloud_file_storage.services.transfer;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.transfer.PreparedFileUpload;
import com.linkermak.cloud_file_storage.dto.transfer.PreparedUpload;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.loader.DuplicateUploadResourceException;
import com.linkermak.cloud_file_storage.exceptions.loader.MultipartFileEmptyException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.file.FileService;
import com.linkermak.cloud_file_storage.utils.StoragePathNormalizer;
import com.linkermak.cloud_file_storage.utils.StoragePathUtils;
import com.linkermak.cloud_file_storage.utils.StoragePathValidator;
import lombok.RequiredArgsConstructor;
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
    private final FileService fileService;

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    @Override
    public List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files) throws IOException {
        PreparedUpload preparedUpload = prepareUpload(directoryPath, files);
        return executeUpload(preparedUpload);
    }

    private List<StorageResource> executeUpload(PreparedUpload preparedUpload) throws IOException {
        Long userId = userProvider.currentUserId();
        List<StorageResource> storageResources = new ArrayList<>();

        for(PreparedFileUpload file : preparedUpload.files()) {

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
                    StoragePathUtils.extractParentPath(fullPath).orElse(""),
                    StoragePathUtils.extractLastPath(fullPath),
                    file.source().getSize(),
                    StorageResourceType.FILE
            ));
        }

        return storageResources;
    }

    private void createParentDirectories(Long userId, String relativePath) {
        List<String> fileParentPaths = StoragePathUtils
                .extractAllParentPaths(relativePath);

        for(String fileParentPath : fileParentPaths) {
            storageRepository.ensureDirectoryExists(userId, fileParentPath);
        }
    }

    private PreparedUpload prepareUpload(String directoryPath, List<MultipartFile> files) {
        String preparedDirectoryPath = prepareDirectoryPath(directoryPath);
        List<PreparedFileUpload> preparedFileUploads = prepareFileUploads(files);

        directoryService.validateDirectoryExists(preparedDirectoryPath);

        for(PreparedFileUpload preparedFileUpload : preparedFileUploads) {
            fileService.validateFileNotExists(preparedDirectoryPath
                    + preparedFileUpload.normalizedRelativePath());
        }

        return new PreparedUpload(
                preparedDirectoryPath,
                preparedFileUploads);
    }

    private String prepareDirectoryPath(String directoryPath) {
        String normalizeDirectoryPath = StoragePathNormalizer
                .normalizeDirectoryPath(directoryPath);
        StoragePathValidator.validateDirectoryPath(normalizeDirectoryPath);
        return normalizeDirectoryPath;
    }

    private List<PreparedFileUpload> prepareFileUploads(List<MultipartFile> files) {
        if(files == null || files.isEmpty()) {
            throw new MultipartFileEmptyException("List multipart files is empty");
        }

        List<PreparedFileUpload> preparedFileUploads = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();

        for(MultipartFile file : files) {
            if(file == null || file.isEmpty()) {
                throw new MultipartFileEmptyException("Multipart file is empty");
            }

            String normalizeFilePath = StoragePathNormalizer.normalizeFilePath(
                    file.getOriginalFilename()
            );
            StoragePathValidator.validateFilePath(normalizeFilePath);

            if(!fileNames.add(normalizeFilePath)) {
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


}
