package com.monntterro;

import com.monntterro.config.props.TelegramBotProperties;
import com.monntterro.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    public void onUpdateReceived(Update update) {
        try {
            updateHandler.handle(update);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    public void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendReplyMessage(String text, Long chatId, int replyToMessageId, List<MessageEntity> entities) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .replyToMessageId(replyToMessageId)
                .entities(entities)
                .build();
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(String text, Long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .entities(entities)
                .build();
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendPhoto(String caption, Long chatId, List<MessageEntity> entities, List<PhotoSize> photos) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .caption(caption)
                .photo(new InputFile(photos.get(photos.size() - 1).getFileId()))
                .captionEntities(entities)
                .build();
        try {
            this.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendVideo(String caption, Long chatId, List<MessageEntity> entities, Video video) {
        SendVideo sendVideo = SendVideo.builder()
                .chatId(chatId)
                .caption(caption)
                .video(new InputFile(video.getFileId()))
                .captionEntities(entities)
                .build();
        if (video.getThumbnail() != null) {
            sendVideo.setThumbnail(new InputFile(video.getThumbnail().getFileId()));
        }

        try {
            this.execute(sendVideo);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendDocument(String caption, Long chatId, List<MessageEntity> entities, Document document) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(document.getFileId()))
                .caption(caption)
                .captionEntities(entities)
                .build();
        if (document.getThumbnail() != null) {
            sendDocument.setThumbnail(new InputFile(document.getThumbnail().getFileId()));
        }

        try {
            this.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendAudio(String caption, Long chatId, List<MessageEntity> entities, Audio audio) {
        SendAudio sendAudio = SendAudio.builder()
                .chatId(chatId)
                .audio(new InputFile(audio.getFileId()))
                .caption(caption)
                .captionEntities(entities)
                .build();
        try {
            this.execute(sendAudio);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .build();
        try {
            this.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMediaGroup(Long chatId, List<InputMedia> medias) {
        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId)
                .medias(medias)
                .build();
        try {
            this.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendAnimation(String caption, long chatId, List<MessageEntity> captionEntities, Animation animation) {
        SendAnimation sendAnimation = SendAnimation.builder()
                .chatId(chatId)
                .animation(new InputFile(animation.getFileId()))
                .caption(caption)
                .captionEntities(captionEntities)
                .build();
        if (animation.getThumbnail() != null) {
            sendAnimation.setThumbnail(new InputFile(animation.getThumbnail().getFileId()));
        }

        try {
            this.execute(sendAnimation);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendSticker(long chatId, Sticker sticker) {
        SendSticker sendSticker = SendSticker.builder()
                .chatId(chatId)
                .sticker(new InputFile(sticker.getFileId()))
                .emoji(sticker.getEmoji())
                .build();

        try {
            this.execute(sendSticker);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendVideoNote(long chatId, VideoNote videoNote) {
        SendVideoNote sendVideoNote = SendVideoNote.builder()
                .chatId(chatId)
                .videoNote(new InputFile(videoNote.getFileId()))
                .build();
        if (videoNote.getThumbnail() != null) {
            sendVideoNote.setThumbnail(new InputFile(videoNote.getThumbnail().getFileId()));
        }

        try {
            this.execute(sendVideoNote);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendVoice(String caption, long chatId, List<MessageEntity> captionEntities, Voice voice) {
        SendVoice sendVoice = SendVoice.builder()
                .caption(caption)
                .chatId(chatId)
                .captionEntities(captionEntities)
                .voice(new InputFile(voice.getFileId()))
                .build();

        try {
            this.execute(sendVoice);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
