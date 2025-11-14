package com.traveler.storage.service;

import com.traveler.storage.repository.FileRepository;
import com.traveler.storage.entity.FileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final String uploadDir = "uploads/";
    
    @Autowired
    private FileRepository fileRepository;

    public String uploadFile(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());
        
        FileEntity fileEntity = new FileEntity(uuid, file.getOriginalFilename(), filePath.toString());
        fileRepository.save(fileEntity);
        
        return uuid;
    }

    public byte[] downloadFile(String uuid) throws IOException {
        Optional<FileEntity> fileEntity = fileRepository.findById(uuid);
        if (fileEntity.isPresent()) {
            Path filePath = Paths.get(fileEntity.get().getFilePath());
            return Files.readAllBytes(filePath);
        }
        throw new IOException("File not found");
    }
}
