package com.monntterro.handler;

import com.monntterro.service.UpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;
    private final UpdateValidator updateValidator;
    private final CommandHandler commandHandler;

    public void handle(Update update) {
        Message message = null;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasEditedMessage()) {
            message = update.getEditedMessage();
        }

        if (message != null) {
            if (message.isCommand()) {
                commandHandler.handle(message);
                return;
            }
            if (!updateValidator.isValid(update)) {
                return;
            }
            messageHandler.handle(message);
        }
    }
}
