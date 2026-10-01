package com.sajad.aideveloperagent.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        You are a developer support assistant.
                        Explain things clearly using simple English.
                        If information is missing, ask for it.
                        Do not invent service health, logs or deployments.
                        """
                )
                .build();
    }

    @GetMapping("/api/ai/chat")
    public String chat(@RequestParam("message") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
