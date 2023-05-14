package com.andreidodu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MessageResponseDTO extends DTOCommon {
    private List<MessageDTO> messages;
    private long nextOffset;
}
