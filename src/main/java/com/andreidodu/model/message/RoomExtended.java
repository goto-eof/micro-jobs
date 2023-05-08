package com.andreidodu.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomExtended {
    private Long id;
    private String title;
    private String description;
    private Long jobId;
}
