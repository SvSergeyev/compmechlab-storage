package tech.sergeyev.compmechlabstorage.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.sergeyev.compmechlabstorage.model.CustomFile;
import tech.sergeyev.compmechlabstorage.service.CustomFileServiceImpl;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/")
@FieldDefaults(
        level = AccessLevel.PRIVATE
)
public class MainController {
    final static Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    final CustomFileServiceImpl customFileService;

//    @Value("${upload.path}")
//    String uploadPath;
    String uploadPath;

    public MainController(CustomFileServiceImpl customFileService) {
        this.customFileService = customFileService;
    }

//    @PostConstruct
    private void init() {
        File dir = new File(uploadPath);
        // Проверяем, не пуста ли папка хранилища. Если пуста - удаляем все из БД.
        if (Objects.requireNonNull(dir.list()).length == 0) {
            LOGGER.info("Storage folder on the file system is empty. Database will be cleared");
            customFileService.deleteAll();
        }
        // Проверяем, не было ли что-то добавлено руками в папку upload.path после предыдущего запуска
        List<File> fileSystemStorage = Arrays.asList(Objects.requireNonNull(dir.listFiles()));
        int databaseStorageSize = customFileService.countAll();
        if (databaseStorageSize < fileSystemStorage.size()) {
            for (File file : fileSystemStorage) {
                if (!customFileService.existsByLocation(file.getPath())) {
                    // Если было - загружаем это в БД, попутно переименовывая
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
        customFiles.forEach(System.out::println);
        return "index";
    }

//    @PostMapping()
//    public String upload(@RequestParam("files") MultipartFile[] files,
//                         Model model,
//                         RedirectAttributes attributes) {
//        LOGGER.info("Received a POST request to upload file(s)");
//        File uploadDirectory = new File(uploadPath);
//        if (!uploadDirectory.exists()) {
//            uploadDirectory.mkdir();
//            LOGGER.info("Directory created: {}", uploadDirectory);
//        }
//        for (MultipartFile file : files) {
//            if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
//                customFileService.uploadFromForm(file);
//                List<CustomFile> allCustomFiles = customFileService.getAll();
//                model.addAttribute("files", allCustomFiles);
//                attributes.addFlashAttribute("success", true);
//                attributes.addFlashAttribute("message", "File uploaded successfully");
//            }
//        }
//        return "redirect:/";
//    }

    @PostMapping()
    public String/*ResponseEntity<?>*/ upload(@RequestParam("file") MultipartFile file,
                                    HttpServletRequest request,
                                    Model model) throws IOException {
        LOGGER.info("Received a POST request to upload file");
        uploadPath = request.getParameter("dest");


//        String fileDownloadUri = ServletUriComponentsBuilder
//                .fromCurrentContextPath()
//                .path("/storage")
//                .path(uploadPath)
//                .toUriString();

        customFileService.uploadFromForm(file, uploadPath);

        List<CustomFile> allCustomFiles = customFileService.getAll();
        model.addAttribute("files", allCustomFiles);

//        return ResponseEntity.status(HttpStatus.OK).location(URI.create("http://localhost:8080/")).build();

//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Location", "/");
//        return new ResponseEntity<String>(headers, HttpStatus.CREATED);

        return "redirect:/";
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable UUID id) throws FileNotFoundException {
        LOGGER.info("Received GET request to download file with id={}", id);
        CustomFile cf = customFileService.getById(id);
        if (cf == null) {
            LOGGER.info("File not found. File with id={} cannot be downloaded", id);
            return ResponseEntity.notFound().build();
        }
        String name = cf.getName();
        String location = cf.getLocation();
        File file = new File(location);
        // Проверяем, не был ли файл вручную удален из хранилища, в то время как запись в БД осталась
        if (file.length() == 0) {
            LOGGER.info("File was not found in the storage directory. Entity with id={} will be removed from the database", id);
            customFileService.deleteById(id);

            // Перенаправляем на главную, чтобы список обновился
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/");
            return new ResponseEntity<String>(headers, HttpStatus.FOUND);
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        LOGGER.info("File with id={} will be downloaded", id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + name + "\"")
                .contentLength(file.length())
                .body(resource);
    }


    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        LOGGER.info("Received DELETE request to remove file with id={}", id);
        CustomFile customFile = customFileService.getById(id);
        File fileForDelete = new File(customFile.getLocation());
        customFileService.deleteById(id);
        fileForDelete.delete();
        return "redirect:/";
    }
}
