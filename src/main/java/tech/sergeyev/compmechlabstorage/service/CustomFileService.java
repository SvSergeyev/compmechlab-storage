package tech.sergeyev.compmechlabstorage.service;

import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface CustomFileService {
    List<CustomFile> getAll();
    CustomFile getById(UUID id);
    Boolean existsByLocation(String location);
    void uploadFromDirectory(File file);
}
