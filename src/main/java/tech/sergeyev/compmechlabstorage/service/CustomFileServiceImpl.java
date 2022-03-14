package tech.sergeyev.compmechlabstorage.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.sergeyev.compmechlabstorage.dao.CustomFileRepository;
import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomFileServiceImpl implements CustomFileService {
    static final Logger LOGGER = LoggerFactory.getLogger(CustomFileServiceImpl.class);
    final CustomFileRepository customFileRepository;
    @Value("${upload.path}")
    String uploadPath;

    public CustomFileServiceImpl(CustomFileRepository customFileRepository) {
        this.customFileRepository = customFileRepository;
    }

//    public void uploadFromForm(MultipartFile file) {
//        UUID id = UUID.randomUUID();
//        String name = file.getOriginalFilename();
//        long size = file.getSize();
//        String uniqueFileName = id.toString() + name;
//        String location = uploadPath + "\\" + uniqueFileName;
//
//        try {
//            file.transferTo(new File(location));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        CustomFile uploadFile = new CustomFile();
//        uploadFile.setId(id);
//        uploadFile.setLocation(location);
//
//        uploadFile.setSize(size);
//        uploadFile.setName(name);
//        LOGGER.info("Save file to: {}", uploadFile.getLocation());
//        customFileRepository.save(uploadFile);
//    }

    public void uploadFromForm(MultipartFile file, String uploadPath) {

        UUID id = UUID.randomUUID();
        String name = file.getOriginalFilename();
        String uniqueFileName = id.toString() + name;

        Path path = Paths.get(uploadPath + "\\" +  uniqueFileName);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long size = file.getSize();
        String location = path.toString();

        CustomFile uploadFile = new CustomFile();
        uploadFile.setId(id);
        uploadFile.setLocation(location);

        uploadFile.setSize(size);
        uploadFile.setName(name);
        LOGGER.info("Save file to: {}", uploadFile.getLocation());
        customFileRepository.save(uploadFile);
    }

    public void deleteById(UUID id) {
        LOGGER.info("File with id={} was delete", id);
        customFileRepository.deleteById(id);
    }

    public int countAll() {
        return customFileRepository.countAll();
    }


    public List<CustomFile> getAll() {
        return customFileRepository.findAll();
    }


    public CustomFile getById(UUID id) {
        return customFileRepository.getCustomFileById(id).orElseThrow(NoSuchElementException::new);
    }


    public Boolean existsByLocation(String location) {
        return customFileRepository.existsByLocation(location);
    }


    public void uploadFromDirectory(File file) {
        if (file != null) {
            UUID id = UUID.randomUUID();
            String name = file.getName();
            long size = file.length();
            String uniqueFileName = id.toString() + name;
            String location = uploadPath + "\\" + uniqueFileName;

            file.renameTo(new File(location));

            CustomFile customFile = new CustomFile();
            customFile.setId(id);
            customFile.setName(name);
            customFile.setLocation(location);
            customFile.setSize(size);
            customFileRepository.save(customFile);
        }
    }

    public void deleteAll() {
        LOGGER.info("All entities have been removed from the database");
        customFileRepository.deleteAll();
    }
}
