package com.trikesh.islab1.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object data;
    private Long personId;
    public WebSocketMessage(){}
}
