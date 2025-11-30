package com.monntterro.handler;

import com.monntterro.service.UrlDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final UrlDeleteService urlDeleteService;

    public void handle(Message message) {
        urlDeleteService.process(message);
    }
}
