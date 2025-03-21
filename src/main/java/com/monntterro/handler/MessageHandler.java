package com.monntterro.handler;

import com.monntterro.processor.UrlDeleteProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final UrlDeleteProcessor urlDeleteProcessor;

    public void handle(Update update) throws TelegramApiException {
        Message message = null;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasEditedMessage()) {
            message = update.getEditedMessage();
        }

        if (message != null) {
            urlDeleteProcessor.process(message);
        }
    }
}
