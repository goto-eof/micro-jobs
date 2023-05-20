package com.andreidodu.controller;

import com.andreidodu.dto.*;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.JwtService;
import com.andreidodu.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/room")
@RequiredArgsConstructor
public class RoomController {

    final private RoomService roomService;

    final private JwtService jwtService;

    @PostMapping("/message")
    public ResponseEntity<MessageDTO> createMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator, @RequestBody MessageDTO messageDTO) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.createMessage(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator), messageDTO));
    }

    @PostMapping("/message/roomId/{roomId}")
    public ResponseEntity<MessageResponseDTO> getMessages(@PathVariable Long roomId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator, @RequestBody MessageRequestDTO messageRequest) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.getMessages(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator), roomId, messageRequest));
    }

    @GetMapping("/jobId/{jobId}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.getRoom(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator), jobId));
    }

    @GetMapping("/getWorkerId/roomId/{roomId}")
    public ResponseEntity<Optional<Long>> getWorkerId(@PathVariable Long roomId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.retrieveWorkerId(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator), roomId));
    }

    @GetMapping
    public ResponseEntity<List<RoomExtendedDTO>> getRooms(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.getRooms(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator)));
    }

}
