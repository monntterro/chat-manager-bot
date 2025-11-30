package com.monntterro.service;

import com.monntterro.command.GetChatId;
import com.monntterro.config.props.TelegramBotProperties;
import com.monntterro.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Service
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramBotProperties properties;
    private final UpdateHandler updateHandler;
    private final TelegramClient telegramClient;

    @Autowired
    public TelegramBot(TelegramBotProperties properties, @Lazy UpdateHandler updateHandler) {
        this.properties = properties;
        this.updateHandler = updateHandler;
        telegramClient = new OkHttpTelegramClient(properties.getToken());
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            updateHandler.handle(update);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @AfterBotRegistration
    public void afterBotRegistration() {
        SetMyCommands setMyCommands = SetMyCommands.builder()
                .commands(List.of(
                        new BotCommand(GetChatId.COMMAND, "get chat id")
                ))
                .build();
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        try {
            telegramClient.execute(sendMessage);
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
            telegramClient.execute(sendMessage);
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
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendPhoto(String caption, Long chatId, List<MessageEntity> entities, List<PhotoSize> photos) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .caption(caption)
                .photo(new InputFile(photos.getLast().getFileId()))
                .captionEntities(entities)
                .build();
        try {
            telegramClient.execute(sendPhoto);
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
            telegramClient.execute(sendVideo);
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
            telegramClient.execute(sendDocument);
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
            telegramClient.execute(sendAudio);
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
            telegramClient.execute(deleteMessage);
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
            telegramClient.execute(sendMediaGroup);
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
            telegramClient.execute(sendAnimation);
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
            telegramClient.execute(sendSticker);
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
            telegramClient.execute(sendVideoNote);
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
            telegramClient.execute(sendVoice);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public Chat getChat(String chatId) throws TelegramApiException {
        return telegramClient.execute(GetChat.builder().chatId(chatId).build());
    }
}
