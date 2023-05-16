package com.andreidodu.controller;

import com.andreidodu.constants.ApplicationConst;
import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.dto.ServerResultDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.JobInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/jobInstance/private")
@RequiredArgsConstructor
public class JobInstanceController {

    final private JobInstanceService jobInstanceService;

    @GetMapping("/{id}")
    public ResponseEntity<JobInstanceDTO> get(@PathVariable Long id) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.get(id));
    }

    @PostMapping
    public ResponseEntity<JobInstanceDTO> save(@RequestBody JobInstanceDTO jobInstanceDTO) throws ApplicationException {
        jobInstanceDTO.setStatus(0);
        return ResponseEntity.ok(this.jobInstanceService.save(jobInstanceDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobInstanceDTO> update(@PathVariable Long id, @RequestBody JobInstanceDTO jobInstanceDTO) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.update(id, jobInstanceDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServerResultDTO> delete(@PathVariable Long id) {
        this.jobInstanceService.delete(id);
        return ResponseEntity.ok(new ServerResultDTO(ApplicationConst.STATUS_DELETE_OK, "deleted"));
    }
}
