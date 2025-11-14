package com.traveler.storage.controller;

import com.traveler.storage.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uuid = fileService.uploadFile(file);
            return ResponseEntity.ok(uuid);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{uuid}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String uuid) {
        try {
            byte[] data = fileService.downloadFile(uuid);
            return ResponseEntity.ok().body(data);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
