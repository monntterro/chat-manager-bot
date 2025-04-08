package com.monntterro.handler;

import com.monntterro.processor.CommandProcessor;
import com.monntterro.service.UpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;
    private final UpdateValidator updateValidator;
    private final CommandProcessor commandProcessor;

    public void handle(Update update) {
        Message message = null;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasEditedMessage()) {
            message = update.getEditedMessage();
        }

        if (message != null) {
            if (message.isCommand()) {
                commandProcessor.process(message);
                return;
            }
            if (!updateValidator.validate(update)) {
                return;
            }
            messageHandler.handle(message);
        }
    }
}
