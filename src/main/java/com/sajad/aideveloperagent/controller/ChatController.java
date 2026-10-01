package com.sajad.aideveloperagent.controller;

import com.sajad.aideveloperagent.tools.ServiceTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder,
                          ServiceTools serviceTools) {
        this.chatClient = builder
                .defaultSystem("""
                        You are a developer support assistant.
                        Explain things clearly using simple English.

                        For service health questions, use the available tool.
                        If no service name is provided, ask for it.
                        Clearly label simulated results as demo data.
                        If a service is unknown, say its health is unknown.
                        Do not invent logs, deployments or failure causes.
                        """
                )
                .defaultTools(serviceTools)
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
