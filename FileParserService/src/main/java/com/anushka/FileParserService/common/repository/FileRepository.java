package com.anushka.FileParserService.common.repository;

import com.anushka.FileParserService.common.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
}
