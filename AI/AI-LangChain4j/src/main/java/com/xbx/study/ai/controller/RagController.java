package com.xbx.study.ai.controller;

import com.xbx.study.ai.service.ChromaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rag")
public class RagController {


    private final ChromaService chromaService;


    public RagController(ChromaService chromaService) {
        this.chromaService = chromaService;
    }



    @RequestMapping(value = "fileEmbedAdd", method = {RequestMethod.POST})
    public ResponseEntity<String> fileEmbedAdd(@RequestParam("file") MultipartFile file){
        chromaService.addFile(file);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("search")
    public ResponseEntity<String> search(@RequestParam("text") String text){
        String query = chromaService.query(text);
        return ResponseEntity.status(HttpStatus.OK).body(query);
    }



}
