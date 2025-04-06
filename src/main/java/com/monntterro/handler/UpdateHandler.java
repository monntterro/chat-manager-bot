package com.monntterro.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;

    public void handle(Update update) {
        Message message = null;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasEditedMessage()) {
            message = update.getEditedMessage();
        }

        if (message != null) {
            messageHandler.handle(message);
        }
    }
}
