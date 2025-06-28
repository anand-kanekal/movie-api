package com.movieflix.movieapi.service.impl;

import com.movieflix.movieapi.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {
        // Get the file name
        String fileName = file.getOriginalFilename();

        // Create upload file directory
        File uploadFileDir = new File(path);

        if (!uploadFileDir.exists()) {
            uploadFileDir.mkdir();
        }

        // Upload the file
        String fileLocation = path + File.separator + fileName;
        Files.copy(file.getInputStream(), Paths.get(fileLocation));

        return fileName;
    }

    @Override
    public InputStream getFile(String path, String fileName) throws FileNotFoundException {
        String fileLocation = path + File.separator + fileName;
        return new FileInputStream(fileLocation);
    }
}
