package com.monntterro.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;

    public void handle(Update update) throws TelegramApiException {
        messageHandler.handle(update);
    }
}
