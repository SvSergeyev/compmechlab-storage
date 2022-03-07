package tech.sergeyev.compmechlabstorage.service;

import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomFileService {
    List<CustomFile> getAll();
    CustomFile getById(UUID id);
    Boolean existsByName(String name);
    Boolean existsByLocation(String location);
    void uploadFromDirectory(File file);
}
