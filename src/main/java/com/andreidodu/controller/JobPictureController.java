package com.andreidodu.controller;

import com.andreidodu.dto.DeleteStatusDTO;
import com.andreidodu.dto.JobPictureDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.JobPictureService;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Delete;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping(value = "/api/v1/jobPicture")
@RequiredArgsConstructor
public class JobPictureController {

    final private JobPictureService jobPictureService;

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get("./files/" + filename)));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename+ "\"").body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPictureDTO> get(@PathVariable Long id) throws ApplicationException {
        return ResponseEntity.ok(this.jobPictureService.get(id));
    }

    @PostMapping
    public ResponseEntity<JobPictureDTO> save(@RequestBody JobPictureDTO jobPictureDTO) throws ApplicationException {
        return ResponseEntity.ok(this.jobPictureService.save(jobPictureDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPictureDTO> update(@PathVariable Long id, @RequestBody JobPictureDTO jobPictureDTO) throws ApplicationException {
        return ResponseEntity.ok(this.jobPictureService.update(id, jobPictureDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteStatusDTO> delete(@PathVariable Long id) throws ApplicationException {
        this.jobPictureService.delete(id);
        return ResponseEntity.ok(new DeleteStatusDTO(true));
    }
}
