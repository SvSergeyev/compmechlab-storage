package tech.sergeyev.compmechlabstorage.service;

import org.springframework.web.multipart.MultipartFile;
import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface CustomFileService {
    void uploadFromForm(MultipartFile file);
    void deleteById(UUID id);
    int countAll();
    List<CustomFile> getAll();
    CustomFile getById(UUID id);
    Boolean existsByLocation(String location);
    void uploadFromDirectory(File file);
    void deleteAll();

}
