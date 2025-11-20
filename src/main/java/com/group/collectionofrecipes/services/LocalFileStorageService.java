package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.exceptions.CreationDirectoryException;
import com.group.collectionofrecipes.exceptions.DeleteFileException;
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

    private final Path fileStorageImageLocation = Paths.get("uploads/recipes").toAbsolutePath().normalize();
    private final Path fileStorageAvatarLocation = Paths.get("uploads/avatars").toAbsolutePath().normalize();

    public LocalFileStorageService() {
        try {
            Files.createDirectories(this.fileStorageImageLocation);
            log.info("Директория для хранения файлов рецептов создана: {}", this.fileStorageImageLocation);
            Files.createDirectories(this.fileStorageAvatarLocation);
            log.info("Директория для хранения файлов аватаров создана: {}", this.fileStorageAvatarLocation);
        } catch (Exception ex) {
            log.error("Ошибка при создании директории для хранения файлов", ex);
            throw new CreationDirectoryException("Could not create the directory to store uploaded files.");
        }
    }

    public String storeAvatarFile(MultipartFile file) {
        return storeFile(file, fileStorageAvatarLocation);
    }

    public String storeImageFile(MultipartFile file) {
        return storeFile(file, fileStorageImageLocation);
    }

    private String storeFile(MultipartFile file, Path location) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        log.debug("Начало сохранения файла: {}", fileName);

        try {
            if (fileName.contains("..")) {
                log.warn("Обнаружено недопустимое имя файла: {}", fileName);
                throw new SaveFileException("Invalid file name: " + fileName);
            }

            Path targetLocation = location.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл успешно сохранен: {}", fileName);
            return fileName;

        } catch (IOException ex) {
            log.error("Ошибка при сохранении файла: {}", fileName, ex);
            throw new SaveFileException("Could not store file " + fileName + ": " + ex.getMessage());
        }
    }

    public void deleteAvatarFile(String fileName) {
        deleteFile(fileName, fileStorageAvatarLocation);
    }

    public void deleteImageFile(String fileName) {
        deleteFile(fileName, fileStorageImageLocation);
    }

    private void deleteFile(String fileName, Path location) {
        log.debug("Попытка удаления файла: {}", fileName);

        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                log.warn("Передано пустое имя файла для удаления");
                return;
            }

            Path filePath = location.resolve(fileName).normalize();

            if (!filePath.startsWith(location)) {
                log.warn("Попытка удаления файла вне целевой директории: {}", fileName);
                throw new DeleteFileException("Invalid file path: " + fileName);
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Файл успешно удален: {}", fileName);
            } else {
                log.warn("Файл не найден для удаления: {}", fileName);
            }

        } catch (IOException ex) {
            log.error("Ошибка при удалении файла: {}", fileName, ex);
        }
    }
}