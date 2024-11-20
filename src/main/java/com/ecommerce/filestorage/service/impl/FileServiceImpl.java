package com.ecommerce.filestorage.service.impl;

import com.ecommerce.filestorage.exceptions.CustomException;
import com.ecommerce.filestorage.model.File;
import com.ecommerce.filestorage.repository.FileRepository;
import com.ecommerce.filestorage.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public File upload(MultipartFile multipartFile, String filename) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND,
                    "Nenhum arquivo selecionado."
            );
        }
        File file = new File();
        file.setName(multipartFile.getOriginalFilename());
        file.setExtension(getFileExtension(file.getName()));
        file.setSize(multipartFile.getSize());
        file.setData(multipartFile.getBytes());
        file.setContentType(multipartFile.getContentType());
        file.setCreatedAt(new Date());
        return fileRepository.save(file);
    }

    @Override
    public File update(String id, MultipartFile file) throws IOException {
        File existingFile = findById(id);
        if (file == null || file.isEmpty()) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND,
                    "Nenhum arquivo selecionado."
            );
        }
        existingFile.setName(file.getOriginalFilename());
        existingFile.setExtension(getFileExtension(existingFile.getName()));
        existingFile.setSize(file.getSize());
        existingFile.setData(file.getBytes());
        existingFile.setContentType(file.getContentType());
        existingFile.setUpdatedAt(new Date());

        fileRepository.updateById(id, existingFile);

        return existingFile;
    }

    @Override
    public List<File> findAll() {
        return fileRepository.findAll();
    }

    @Override
    public File findById(String id) {
        // Validações movidas para o serviço
        if (id == null || id.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "File ID cannot be null or empty"
            );
        }

        // Chama o repositório após a validação
        File file = fileRepository.findById(id);

        if (file == null) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND,
                    "File not found for id: " + id
            );
        }

        return file;
    }

    @Override
    public void delete(String id) {
        File file = findById(id);
        fileRepository.deleteById(id);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
