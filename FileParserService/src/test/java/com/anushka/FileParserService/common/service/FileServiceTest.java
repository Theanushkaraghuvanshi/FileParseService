package com.anushka.FileParserService.common.service;

import com.anushka.FileParserService.common.entity.FileEntity;
import com.anushka.FileParserService.common.repository.FileRepository;
import com.anushka.FileParserService.parser.FileParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileParserService fileParserService;

    @InjectMocks
    private FileService fileService;

    @Test
    public void testSaveFileMetadata() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getSize()).thenReturn(100L);
        when(fileStorageService.storeFile(any(), any())).thenReturn("/path/to/file");

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(UUID.randomUUID());
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);

        FileEntity result = fileService.saveFileMetadata(file);

        assertNotNull(result);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    public void testGetFile() {
        UUID fileId = UUID.randomUUID();
        FileEntity fileEntity = new FileEntity();
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        Optional<FileEntity> result = fileService.getFile(fileId);

        assertTrue(result.isPresent());
    }
}
