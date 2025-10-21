package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.exceptions.CreationDirectoryException;
import com.group.collectionofrecipes.exceptions.SaveFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public LocalFileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Директория для хранения файлов создана: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Ошибка при создании директории для хранения файлов: {}", this.fileStorageLocation, ex);
            throw new CreationDirectoryException("Could not create the directory to store uploaded files.");
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        log.debug("Начало сохранения файла: {}", fileName);

        try {
            if (fileName.contains("..")) {
                log.warn("Обнаружено недопустимое имя файла: {}", fileName);
                throw new SaveFileException("Invalid file name: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл успешно сохранен: {}", fileName);
            return fileName;

        } catch (IOException ex) {
            log.error("Ошибка при сохранении файла: {}", fileName, ex);
            throw new SaveFileException("Could not store file " + fileName + ": " + ex.getMessage());
        }
    }

    public void deleteFile(String file) {
        //реализовать
    }
}