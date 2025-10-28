package com.trikesh.islab1.controller;

import com.trikesh.islab1.DTO.WebSocketMessage;
import com.trikesh.islab1.model.Person;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@AllArgsConstructor
@Controller
public class PersonWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyPersonCreated(Person person){
        WebSocketMessage message = new WebSocketMessage("CREATE", person, person.getId());
        messagingTemplate.convertAndSend("/topic/persons", message);
    }

    public void notifyPersonUpdated(Person person){
        WebSocketMessage message = new WebSocketMessage("UPDATE", person, person.getId());
        messagingTemplate.convertAndSend("/topic/persons", message);
    }
    public void notifyPersonDeleted(Long id){
        WebSocketMessage message = new WebSocketMessage("DELETE", null, id);
        messagingTemplate.convertAndSend("/topic/persons", message);
    }

}
