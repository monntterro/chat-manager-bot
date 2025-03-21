package com.monntterro;

import com.monntterro.config.props.TelegramBotProperties;
import com.monntterro.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final TelegramBotProperties properties;
    private final UpdateHandler updateHandler;

    @Autowired
    public TelegramBot(TelegramBotProperties properties, @Lazy UpdateHandler updateHandler) {
        super(properties.getToken());
        this.properties = properties;
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    public void sendMessage(String text, Long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .entities(entities)
                .build();
        this.perform(sendMessage);
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .build();
        this.perform(deleteMessage);
    }

    public void perform(BotApiMethod<? extends Serializable> method) {
        try {
            super.execute(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to execute method", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            updateHandler.handle(update);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
