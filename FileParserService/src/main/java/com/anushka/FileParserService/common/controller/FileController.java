package com.anushka.FileParserService.common.controller;

import com.anushka.FileParserService.common.entity.FileEntity;
import com.anushka.FileParserService.common.entity.FileStatus;
import com.anushka.FileParserService.common.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting file upload: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());

            FileEntity fileEntity = fileService.saveFileMetadata(file);

            System.out.println("File entity created with ID: " + fileEntity.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("file_id", fileEntity.getId());
            response.put("message", "File uploaded successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error in uploadFile: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Could not upload the file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{fileId}/progress")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable UUID fileId) {
        Optional<FileEntity> fileEntity = fileService.getFile(fileId);

        if (!fileEntity.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("file_id", fileId);
        response.put("status", fileService.getStatus(fileId).toString().toLowerCase());
        response.put("progress", fileService.getProgress(fileId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Object> getFileContent(@PathVariable UUID fileId) {
        Optional<FileEntity> fileEntity = fileService.getFile(fileId);

        if (!fileEntity.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (fileService.getStatus(fileId) != FileStatus.READY) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File upload or processing in progress. Please try again later.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        FileEntity entity = fileEntity.get();
        Map<String, Object> response = new HashMap<>();
        response.put("file_id", entity.getId());
        response.put("file_name", entity.getFileName());
        response.put("file_size", entity.getFileSize());
        response.put("status", entity.getStatus().toString().toLowerCase());
        response.put("created_at", entity.getCreatedAt());

        if (entity.getParsedContent() != null) {
            response.put("parsed_content", entity.getParsedContent());
        } else {
            response.put("parsed_content", "No parsed content available.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        List<FileEntity> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable UUID fileId) {
        Optional<FileEntity> fileEntity = fileService.getFile(fileId);

        if (!fileEntity.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        fileService.deleteFile(fileId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        return ResponseEntity.ok(response);
    }
}
