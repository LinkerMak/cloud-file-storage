package com.linkermak.cloud_file_storage.services;

import com.linkermak.cloud_file_storage.repositories.ObjectStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilesService {

    private final ObjectStorageRepository storageRepository;

    @Autowired
    public FilesService(ObjectStorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public void createDirectory() {
        if(storageRepository.)
    }
}
