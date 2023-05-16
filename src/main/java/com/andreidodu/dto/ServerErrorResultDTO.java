package com.andreidodu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerErrorResultDTO {

    public ServerErrorResultDTO() {
    }

    public ServerErrorResultDTO(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    private int errorCode;
    private String message;
}
