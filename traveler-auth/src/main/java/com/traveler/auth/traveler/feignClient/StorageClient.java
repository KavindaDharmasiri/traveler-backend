package com.traveler.auth.traveler.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "traveler-storage", configuration = com.traveler.auth.traveler.config.FeignConfig.class)
public interface StorageClient {

    @PostMapping("/files/upload")
    ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file);

    @GetMapping("/files/download/{uuid}")
    ResponseEntity<byte[]> downloadFile(@PathVariable String uuid);
}
