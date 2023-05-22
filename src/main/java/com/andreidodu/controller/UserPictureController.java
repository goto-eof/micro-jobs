package com.andreidodu.controller;

import com.andreidodu.dto.DeleteStatusDTO;
import com.andreidodu.dto.UserPictureDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.UserPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/userPicture")
@RequiredArgsConstructor
public class UserPictureController {

    final private UserPictureService userPictureService;

    @GetMapping("/{id}")
    public ResponseEntity<UserPictureDTO> get(@PathVariable Long id) throws ApplicationException {
        return ResponseEntity.ok(this.userPictureService.get(id));
    }

    @PostMapping
    public ResponseEntity<UserPictureDTO> save(@RequestBody UserPictureDTO userPictureDTO) throws ApplicationException {
        return ResponseEntity.ok(this.userPictureService.save(userPictureDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserPictureDTO> update(@PathVariable Long id, @RequestBody UserPictureDTO userPictureDTO) throws ApplicationException {
        return ResponseEntity.ok(this.userPictureService.update(id, userPictureDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteStatusDTO> delete(@PathVariable Long id) {
        this.userPictureService.delete(id);
        return ResponseEntity.ok(new DeleteStatusDTO(true));
    }
}
