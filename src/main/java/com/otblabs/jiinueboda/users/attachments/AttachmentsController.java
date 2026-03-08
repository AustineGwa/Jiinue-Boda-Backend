package com.otblabs.jiinueboda.users.attachments;


import com.otblabs.jiinueboda.filemanagement.FileManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/client-attachments")
public class AttachmentsController {

    final FileManagementService fileManagementService;

    public AttachmentsController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    @PostMapping("/uploads/{fileType}/{userId}")
    ResponseEntity<String> uploadUserAttachment(@RequestParam("file") MultipartFile file, @PathVariable String fileType,@PathVariable int userId){
        try{
            return ResponseEntity.ok(fileManagementService.uploadClientAttachment(file,fileType,userId));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }
}
