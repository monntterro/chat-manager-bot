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
            if (message.hasPhoto()) {
                fileService.handlePhotoAlbum(message, text, entities);
            } else if (message.hasVideo()) {
                fileService.handleVideoAlbum(message, text, entities);
            }
            return;
        }

        if (message.getForwardFromChat() == null &&
            (text == null
             || !messageTextService.hasLinksInMessage(text, entities)
             || messageTextService.hasOnlyAllowedLinks(text, entities))) {
            return;
        }

        ProcessedMessage processedMessage = messageTextService.deleteLinks(message, text, entities);
        text = processedMessage.text();
        entities = processedMessage.entities();

        long chatId = message.getChatId();
        int messageId = message.getMessageId();

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
        } else {
            bot.sendMessage(text, chatId, entities);
        }

        bot.deleteMessage(chatId, messageId);
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


