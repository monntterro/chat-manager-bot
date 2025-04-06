package com.monntterro.processor;

import com.monntterro.TelegramBot;
import com.monntterro.service.FileService;
import com.monntterro.service.MessageTextService;
import com.monntterro.service.model.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlDeleteProcessor {
    private final TelegramBot bot;
    private final FileService fileService;
    private final MessageTextService messageTextService;

    public void process(Message message) {
        String text = extractText(message);
        List<MessageEntity> entities = extractEntities(message);

        if (message.getMediaGroupId() != null) {
            processMediaGroup(message, text, entities);
            return;
        }
        if (shouldSkipProcessing(message, text, entities)) {
            return;
        }

        ProcessedMessage processedMessage = messageTextService.deleteLinks(message, text, entities);
        text = processedMessage.text();
        entities = processedMessage.entities();
        sendProcessedMessage(message, text, entities);

        bot.deleteMessage(message.getChatId(), message.getMessageId());
    }

    private void processMediaGroup(Message message, String text, List<MessageEntity> entities) {
        if (message.hasPhoto()) {
            fileService.handlePhotoAlbum(message, text, entities);
        } else if (message.hasVideo()) {
            fileService.handleVideoAlbum(message, text, entities);
        }
    }

    private boolean shouldSkipProcessing(Message message, String text, List<MessageEntity> entities) {
        return message.getForwardFromChat() == null &&
               (text == null ||
                !messageTextService.hasLinksInMessage(text, entities) ||
                messageTextService.hasOnlyAllowedLinks(text, entities));
    }

    private void sendProcessedMessage(Message message, String text, List<MessageEntity> entities) {
        long chatId = message.getChatId();

        if (message.hasPhoto()) {
            bot.sendPhoto(text, chatId, entities, message.getPhoto());
        } else if (message.hasVideo()) {
            bot.sendVideo(text, chatId, entities, message.getVideo());
        } else if (message.hasDocument()) {
            bot.sendDocument(text, chatId, entities, message.getDocument());
        } else if (message.hasAudio()) {
            bot.sendAudio(text, chatId, entities, message.getAudio());
        } else if (message.hasAnimation()) {
            bot.sendAnimation(text, chatId, entities, message.getAnimation());
        } else if (message.hasSticker()) {
            bot.sendSticker(chatId, message.getSticker());
        } else if (message.hasVideoNote()) {
            bot.sendVideoNote(chatId, message.getVideoNote());
        } else if (message.hasVoice()) {
            bot.sendVoice(text, chatId, entities, message.getVoice());
        } else {
            bot.sendMessage(text, chatId, entities);
        }
    }

    private String extractText(Message message) {
        if (message.hasText()) {
            return message.getText();
        } else if (message.getCaption() != null) {
            return message.getCaption();
        }
        return null;
    }

    private List<MessageEntity> extractEntities(Message message) {
        if (message.hasEntities()) {
            return message.getEntities();
        } else if (message.getCaptionEntities() != null) {
            return message.getCaptionEntities();
        }
        return new ArrayList<>();
    }
}