package tech.sergeyev.compmechlabstorage.service;

import org.springframework.core.io.FileSystemResource;
import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomFileService {
    List<CustomFile> getAll();
    CustomFile getById(UUID id);
    Boolean existsByLocation(String location);
    void uploadFromDirectory(File file);
}
