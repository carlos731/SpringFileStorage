package com.ecommerce.filestorage.controller;

import com.ecommerce.filestorage.model.File;
import com.ecommerce.filestorage.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/storage/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam(name = "file") MultipartFile multipartFile,
            @RequestParam(required = false, name = "filename") String filename
    ) {
        try {
            File file = fileService.upload(multipartFile, filename);
            String fileId = file.getId();
            String fileLink = "http://localhost:9000/" + fileId;

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileLink);
            response.put("name", file.getName());
            response.put("extension", file.getExtension());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("createdAt", file.getCreatedAt());
            response.put("updatedAt", file.getUpdatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro ao fazer upload do arquivo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id,
            @RequestParam(name = "file") MultipartFile multipartFile
    ) {
        try {
            String[] parts = id.split("\\.");
            String value = parts[0];

            File updatedFile = fileService.update(value, multipartFile);
            String fileId = updatedFile.getId();
            String fileLink = "http://localhost:9000/" + fileId;

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileLink);
            response.put("id", updatedFile.getId());
            response.put("name", updatedFile.getName());
            response.put("extension", updatedFile.getExtension());
            response.put("size", updatedFile.getSize());
            response.put("contentType", updatedFile.getContentType());
            response.put("createdAt", updatedFile.getCreatedAt());
            response.put("updatedAt", updatedFile.getUpdatedAt());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro ao atualizar o arquivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFileById(@PathVariable String id) {
        String[] parts = id.split("\\.");
        String value = parts[0];

        File file = fileService.findById(value);
        byte[] fileContent = file.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        System.out.println("Retornando arquivo: " + file.getId());
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @GetMapping("/video/{id}")
    public ResponseEntity<ResourceRegion> getVideoById(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        String[] parts = id.split("\\.");
        String value = parts[0];

        File videoFile = fileService.findById(value);
        byte[] videoContent = videoFile.getData();

        // Verificar o tipo de conteúdo para garantir que seja um arquivo de vídeo
        MediaType mediaType = MediaType.parseMediaType(videoFile.getContentType());
        if (!mediaType.getType().equals("video")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        // Criar um recurso ByteArrayResource para armazenar o conteúdo do vídeo
        ByteArrayResource videoResource = new ByteArrayResource(videoContent);

        // Definir o tamanho do vídeo
        long videoLength = videoContent.length;

        // Verificar se o cliente solicitou uma parte específica (Range request)
        Optional<HttpRange> range = headers.getRange().stream().findFirst();
        ResourceRegion region;

        if (range.isPresent()) {
            long start = range.get().getRangeStart(videoLength);
            long end = range.get().getRangeEnd(videoLength);
            long chunkSize = Math.min(1_000_000L, end - start + 1);  // Limitar o tamanho do chunk para 1MB

            region = new ResourceRegion(videoResource, start, chunkSize);
        } else {
            // Se não há range request, enviar o vídeo inteiro
            region = new ResourceRegion(videoResource, 0, Math.min(1_000_000L, videoLength));
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(responseHeaders)
                .body(region);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        String[] parts = id.split("\\.");
        String value = parts[0];

        File file = fileService.findById(value);

        byte[] fileContent = file.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentDispositionFormData("attachment", file.getId() + "." + file.getExtension());
        headers.setContentLength(fileContent.length);

        System.out.println("Baixando arquivo: " + file.getId());

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFileById(@PathVariable String id) {
        fileService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        // response.put("fileId", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/")
    public ResponseEntity<List<File>> findALl() {
        List<File> files = fileService.findAll();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }
}
