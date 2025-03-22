package com.monntterro.processor;

import com.monntterro.TelegramBot;
import com.monntterro.service.FIleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlDeleteProcessor {
    private final Pattern urlPattern = Pattern.compile("(?i)((?:https?|ftp)://|www\\.)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{1,3}(?:\\.\\d{1,3}){3}|\\[[a-fA-F0-9:]+])(:\\d{1,5})?(/\\S*)?");

    private final TelegramBot bot;
    private final FIleService fIleService;

    @Value("${telegram.secret-word-to-pass}")
    private String secretWordToPass;

    public void process(Message message) throws TelegramApiException {
        List<MessageEntity> entities = Collections.emptyList();
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

        if (text == null && message.getMediaGroupId() == null) {
            return;
        }


        if (text != null) {
            String lastName = message.getFrom().getLastName();
            String username = message.getFrom().getFirstName() + (lastName == null ? "" : " " + lastName);
            text = "%s:\n%s".formatted(username, text);
            if (!hasLinksInMessage(text, entities)) {
                return;
            }

            entities = entities.stream()
                    .filter(entity -> !"text_link".equals(entity.getType()))
                    .peek(entity -> entity.setOffset(entity.getOffset() + 2 + username.length()))
                    .collect(Collectors.toCollection(ArrayList::new));
            entities.add(userMention(message, username));

            Matcher matcher = urlPattern.matcher(text);
            while (matcher.find()) {
                String url = matcher.group();
                int start = matcher.start();
                if (start > secretWordToPass.length()) {
                    String substring = text.substring(start - secretWordToPass.length(), start);
                    if (secretWordToPass.equals(substring)) {
                        text = text.replaceFirst(Pattern.quote(secretWordToPass + url), " ".repeat(secretWordToPass.length()) + url);
                        continue;
                    }
                }
                text = text.replace(url, "*".repeat(url.length()));
            }
        }

        long chatId = message.getChatId();
        int messageId = message.getMessageId();

        if (message.getMediaGroupId() != null) {
            if (message.hasPhoto()) {
                fIleService.handlePhotoAlbum(message, text, entities);
            } else if (message.hasVideo()) {
                fIleService.handleVideoAlbum(message, text, entities);
            }
        } else if (message.hasPhoto()) {
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

    private MessageEntity userMention(Message message, String username) {
        return MessageEntity.builder()
                .user(message.getFrom())
                .type("text_mention")
                .user(message.getFrom())
                .offset(0)
                .length(username.length())
                .build();
    }

    private boolean hasLinksInMessage(String text, List<MessageEntity> entities) {
        if (urlPattern.matcher(text).find()) {
            return true;
        }
        return entities.stream()
                .map(MessageEntity::getType)
                .anyMatch(type -> "text_link".equals(type) || "url".equals(type));
    }
}


