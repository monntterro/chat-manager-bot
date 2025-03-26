package com.monntterro.processor;

import com.monntterro.TelegramBot;
import com.monntterro.service.FIleService;
import com.monntterro.service.MessageTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlDeleteProcessor {
    private final TelegramBot bot;
    private final FIleService fIleService;
    private final MessageTextService messageTextService;

    public void process(Message message) throws TelegramApiException {
        List<MessageEntity> entities = new ArrayList<>();
        String text = null;
        if (message.hasText()) {
            text = message.getText();
            if (message.hasEntities()) {
                entities = message.getEntities();
            }
        } else if (message.getCaption() != null) {
            text = message.getCaption();
            if (message.getCaptionEntities() != null) {
                entities = message.getCaptionEntities();
            }
        }

        if (message.getMediaGroupId() != null) {
            if (message.hasPhoto()) {
                fIleService.handlePhotoAlbum(message, text, entities);
            } else if (message.hasVideo()) {
                fIleService.handleVideoAlbum(message, text, entities);
            }
            return;
        }

        if (message.getForwardFromChat() == null &&
            (text == null
             || !messageTextService.hasLinksInMessage(text, entities)
             || messageTextService.hasOnlyAllowedLinks(text, entities))) {
            return;
        }

        text = messageTextService.deleteLinks(message, text, entities);
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            SendPhoto sendPhoto = SendPhoto.builder()
                    .caption(text)
                    .chatId(chatId)
                    .photo(new InputFile(photos.get(photos.size() - 1).getFileId()))
                    .captionEntities(entities)
                    .build();
            bot.execute(sendPhoto);
        } else if (message.hasVideo()) {
            SendVideo sendVideo = SendVideo.builder()
                    .caption(text)
                    .captionEntities(entities)
                    .video(new InputFile(message.getVideo().getFileId()))
                    .thumbnail(new InputFile(message.getVideo().getThumbnail().getFileId()))
                    .chatId(chatId)
                    .build();
            bot.execute(sendVideo);
        } else if (message.hasDocument()) {
            SendDocument sendDocument = SendDocument.builder()
                    .document(new InputFile(message.getDocument().getFileId()))
                    .caption(text)
                    .thumbnail(new InputFile(message.getDocument().getThumbnail().getFileId()))
                    .captionEntities(entities)
                    .chatId(chatId)
                    .build();
            bot.execute(sendDocument);
        } else if (message.hasAudio()) {
            SendAudio sendAudio = SendAudio.builder()
                    .audio(new InputFile(message.getAudio().getFileId()))
                    .captionEntities(entities)
                    .caption(text)
                    .chatId(chatId)
                    .build();
            bot.execute(sendAudio);
        } else {
            bot.sendMessage(text, chatId, entities);
        }
        bot.deleteMessage(chatId, messageId);
    }
}


