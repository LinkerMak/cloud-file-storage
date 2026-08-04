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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
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
        log.info("Download resource started: path={}", path);

        String trimmedPath = pathPreparer.trimPath(path);
        boolean isDirectory = trimmedPath.endsWith("/");

        DownloadedResource result = isDirectory
                ? downloadDirectory(path)
                : downloadFile(path);

        log.info("Download resource completed: path={}, isDirectory={}", path, isDirectory);
        return result;
    }

    private DownloadedResource downloadFile(String filePath) {
        log.debug("Download file started: path={}", filePath);

        String normalizedFilePath = pathPreparer.prepareFilePath(filePath);

        StorageDownloadObject downloadedFile =
                storageRepository.downloadFile(userProvider.currentUserId(), normalizedFilePath);

        DownloadedResource result = new DownloadedResource(
                StoragePathExtractor.extractLastPath(downloadedFile.fileName()),
                new InputStreamResource(downloadedFile.inputStream()),
                downloadedFile.size()
        );

        log.debug("Download file completed: path={}, size={}", normalizedFilePath, downloadedFile.size());
        return result;
    }

    private DownloadedResource downloadDirectory(String directoryPath) {
        log.info("Download directory started: path={}", directoryPath);

        String normalizedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        List<StorageObjectInfo> resources = storageRepository
                .findDescendantsByPrefix(userId, normalizedDirectoryPath);

        byte[] zipBytes = zipArchiveService.
                createZip(userId, normalizedDirectoryPath, resources);

        log.info("Download directory completed: path={}, itemsCount={}, zipSize={}",
                normalizedDirectoryPath, resources.size(), zipBytes.length);

        return new DownloadedResource(
                normalizedDirectoryPath,
                new ByteArrayResource(zipBytes),
                zipBytes.length
        );
    }

    @Override
    public List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files) throws IOException {
        log.info("Upload resource started: directoryPath={}, filesCount={}",
                directoryPath, files != null ? files.size() : null);

        PreparedUpload preparedUpload = prepareUpload(directoryPath, files);
        List<StorageResource> result = executeUpload(preparedUpload);

        log.info("Upload resource completed: directoryPath={}, uploadedCount={}",
                preparedUpload.preparedDirectoryPath(), result.size());

        return result;
    }

    private PreparedUpload prepareUpload(String directoryPath, List<MultipartFile> files) {
        log.debug("Prepare upload started: directoryPath={}, filesCount={}",
                directoryPath, files != null ? files.size() : null);

        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);
        List<PreparedFileUpload> preparedFileUploads = prepareFileUploads(files);

        directoryService.validatePreparedDirectoryExists(preparedDirectoryPath);

        for (PreparedFileUpload preparedFileUpload : preparedFileUploads) {
            resourceService.validatePreparedFileNotExists(preparedDirectoryPath
                    + preparedFileUpload.preparedRelativeFilePath());
        }

        log.debug("Prepare upload completed: directoryPath={}, preparedFilesCount={}",
                preparedDirectoryPath, preparedFileUploads.size());

        return new PreparedUpload(
                preparedDirectoryPath,
                preparedFileUploads);
    }

    private List<PreparedFileUpload> prepareFileUploads(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new MultipartFileEmptyException("List multipart files is empty");
        }

        log.debug("Prepare file uploads started: filesCount={}", files.size());

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

        log.debug("Prepare file uploads completed: preparedFilesCount={}", preparedFileUploads.size());

        return preparedFileUploads;
    }

    private List<StorageResource> executeUpload(PreparedUpload preparedUpload) throws IOException {
        Long userId = userProvider.currentUserId();
        List<StorageResource> storageResources = new ArrayList<>();

        log.info("Execute upload started: directoryPath={}, filesCount={}",
                preparedUpload.preparedDirectoryPath(), preparedUpload.files().size());

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

        log.info("Execute upload completed: directoryPath={}, uploadedCount={}",
                preparedUpload.preparedDirectoryPath(), storageResources.size());

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
