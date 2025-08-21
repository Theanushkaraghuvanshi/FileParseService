package com.anushka.FileParserService.common.service;

import com.anushka.FileParserService.common.exception.UnsupportedFileTypeException;
import com.anushka.FileParserService.common.entity.FileEntity;
import com.anushka.FileParserService.common.entity.FileStatus;
import com.anushka.FileParserService.common.repository.FileRepository;
import com.anushka.FileParserService.parser.FileParserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileParserService fileParserService;

    private final Map<UUID, Integer> progressMap = new ConcurrentHashMap<>();
    private final Map<UUID, FileStatus> statusMap = new ConcurrentHashMap<>();

    public FileEntity saveFileMetadata(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new UnsupportedFileTypeException("Only CSV files are supported for parsing.");
        }

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(UUID.randomUUID());
        fileEntity.setFileName(fileName);
        fileEntity.setStatus(FileStatus.UPLOADING);
        fileEntity.setProgress(0);
        fileEntity.setFileSize(file.getSize());

        String filePath = fileStorageService.storeFile(file, fileEntity.getId());
        fileEntity.setFilePath(filePath);

        FileEntity savedEntity = fileRepository.save(fileEntity);

        progressMap.put(savedEntity.getId(), 0);
        statusMap.put(savedEntity.getId(), FileStatus.UPLOADING);

        processFileAsync(savedEntity.getId());

        return savedEntity;
    }

    private void processFileAsync(UUID fileId) {
        new Thread(() -> {
            try {
                statusMap.put(fileId, FileStatus.PROCESSING);

                Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);
                if (!optionalFileEntity.isPresent()) {
                    throw new RuntimeException("File not found");
                }
                FileEntity fileEntity = optionalFileEntity.get();
                String filePath = fileEntity.getFilePath();
                String fileName = fileEntity.getFileName();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

                String parsedContent = "";
                if ("csv".equals(extension)) {
                    try {
                        List<Map<String, String>> parsedData = fileParserService.parseCSV(filePath);
                        ObjectMapper objectMapper = new ObjectMapper();
                        parsedContent = objectMapper.writeValueAsString(parsedData);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
                    }
                } else {
                    parsedContent = "File type not supported for parsing.";
                }

                // Simulate processing
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(100);
                    progressMap.put(fileId, i);
                }

                statusMap.put(fileId, FileStatus.READY);

                optionalFileEntity = fileRepository.findById(fileId);
                if (optionalFileEntity.isPresent()) {
                    fileEntity = optionalFileEntity.get();
                    fileEntity.setStatus(FileStatus.READY);
                    fileEntity.setProgress(100);
                    fileEntity.setParsedContent(parsedContent);
                    fileRepository.save(fileEntity);
                }
            } catch (Exception e) {
                statusMap.put(fileId, FileStatus.FAILED);
                Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);
                if (optionalFileEntity.isPresent()) {
                    FileEntity fileEntity = optionalFileEntity.get();
                    fileEntity.setStatus(FileStatus.FAILED);
                    fileRepository.save(fileEntity);
                }
            }
        }).start();
    }

    public Integer getProgress(UUID fileId) {
        return progressMap.getOrDefault(fileId, 0);
    }

    public FileStatus getStatus(UUID fileId) {
        return statusMap.getOrDefault(fileId, FileStatus.FAILED);
    }

    public Optional<FileEntity> getFile(UUID fileId) {
        return fileRepository.findById(fileId);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public void deleteFile(UUID fileId) {
        Optional<FileEntity> fileEntity = fileRepository.findById(fileId);
        if (fileEntity.isPresent()) {
            fileStorageService.deleteFile(fileEntity.get().getFilePath());
            fileRepository.deleteById(fileId);
            progressMap.remove(fileId);
            statusMap.remove(fileId);
        }
    }
}