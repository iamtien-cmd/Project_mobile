package vn.iostar.Project_Mobile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.*;
import vn.iostar.Project_Mobile.DTO.Message;
import vn.iostar.Project_Mobile.service.impl.ChatBotServiceImpl;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Cho phép kết nối từ front-end
public class ChatBotController {

    @Autowired
    private ChatBotServiceImpl chatBotService;

    @PostMapping
    public Message chat(@RequestBody Message message) {
        String reply = chatBotService.getReply(message.getUserMessage());
        return new Message(message.getUserMessage(), reply);
    }
}
