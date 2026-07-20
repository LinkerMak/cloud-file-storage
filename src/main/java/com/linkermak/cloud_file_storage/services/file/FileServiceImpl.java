package com.linkermak.cloud_file_storage.services.file;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService{

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    @Override
    public void validateFileNotExists(String filePath) {
        if(storageRepository.existsFile(userProvider.currentUserId(), filePath)) {
            throw new ResourceAlreadyExistsException("File already exists by path:" + filePath);
        }
    }


}
