package tech.sergeyev.compmechlabstorage.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.sergeyev.compmechlabstorage.model.CustomFile;
import tech.sergeyev.compmechlabstorage.service.CustomFileServiceImpl;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/")
@FieldDefaults(
        level = AccessLevel.PRIVATE
)
public class MainController {
    final static Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    final CustomFileServiceImpl customFileService;

    @Value("${upload.path}")
    String uploadPath;

    public MainController(CustomFileServiceImpl customFileService) {
        this.customFileService = customFileService;
    }

    @PostConstruct
    private void init() {
        // Проверяем, не было ли что-то добавлено руками в папку upload.path
        File dir = new File(uploadPath);
        List<File> fileSystemStorage = Arrays.asList(Objects.requireNonNull(dir.listFiles()));
        int databaseStorageSize = customFileService.countAll();
        if (databaseStorageSize < fileSystemStorage.size()) {
            for (File file : fileSystemStorage) {
                if (!customFileService.existsByLocation(file.getPath())) {
                    customFileService.uploadFromDirectory(file);
                }
            }
        }
    }


    @GetMapping()
    public String index(Model model) {
        LOGGER.info("Received a GET request on the main page");
        List<CustomFile> customFiles = customFileService.getAll();
        model.addAttribute("files", customFiles);
        return "index";
    }

    @PostMapping()
    public String upload(@RequestParam("files") MultipartFile[] files,
                         Model model) {
        if (files == null) {
            // тут бы какое нибудь окошко с напоминанием выбрать файл
        }
        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdir();
        }
        for (MultipartFile file : files) {
            if (!file.getOriginalFilename().isEmpty()) {
                customFileService.uploadFromForm(file);
                List<CustomFile> allCustomFiles = customFileService.getAll();
                model.addAttribute("files", allCustomFiles);
            }
        }
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String download(@PathVariable UUID id) {
        // TODO: загрузка файлов
        String location = customFileService.getById(id).getLocation();
        File downloaded = new File(location);

        return "redirect:/";
    }


    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        CustomFile customFile = customFileService.getById(id);
        File fileForDelete = new File(customFile.getLocation());
        customFileService.deleteById(id);
        fileForDelete.delete();
        return "redirect:/";
    }


}
