package com.movieflix.movieapi.controller;

import com.movieflix.movieapi.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${project.poster}")
    private String fileUploadPath;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestPart MultipartFile file) throws IOException {
        String fileName = fileService.uploadFile(fileUploadPath, file);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }

    @GetMapping("/{fileName}")
    public void getFile(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        InputStream fileInputStream = fileService.getFile(fileUploadPath, fileName);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(fileInputStream, response.getOutputStream());
    }
}
