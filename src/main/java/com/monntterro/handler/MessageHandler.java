package com.monntterro.handler;

import com.monntterro.processor.UrlDeleteProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final UrlDeleteProcessor urlDeleteProcessor;

    public void handle(Message message) {
        urlDeleteProcessor.process(message);
    }
}
