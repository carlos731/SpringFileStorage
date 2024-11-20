package com.ecommerce.filestorage.service;

import com.ecommerce.filestorage.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    public File upload(MultipartFile file, String filename) throws IOException;
    public File update(String id, MultipartFile file) throws IOException;
    public List<File> findAll();
    public File findById(String id);
    public void delete(String id);
}
