package com.monntterro;

import com.monntterro.config.props.TelegramBotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

@Component
public class TelegramBot extends TelegramWebhookBot {
    private final TelegramBotProperties properties;

    @Autowired
    public TelegramBot(TelegramBotProperties properties) {
        super(properties.getToken());
        this.properties = properties;
        registerWebhook();
    }

    @Override
    public String getBotPath() {
        return properties.getPath();
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    private void registerWebhook() {
        SetWebhook setWebhook = SetWebhook.builder()
                .url(properties.getUrl())
                .build();
        try {
            this.setWebhook(setWebhook);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to set webhook", e);
        }
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
}
