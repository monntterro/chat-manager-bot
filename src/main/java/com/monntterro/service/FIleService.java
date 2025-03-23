package com.monntterro.service;

import com.monntterro.TelegramBot;
import com.monntterro.model.AlbumData;
import com.monntterro.model.mediafile.MediaFile;
import com.monntterro.model.mediafile.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FIleService {
    private final ConcurrentHashMap<String, AlbumData> albumCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final TelegramBot bot;
    private final MessageTextService messageTextService;

    public void handleVideoAlbum(Message message, String caption, List<MessageEntity> entities) {
        Video video = message.getVideo();
        PhotoSize thumbnail = video.getThumbnail();
        String thumbnailId = null;
        if (thumbnail != null) {
            thumbnailId = thumbnail.getFileId();
        }

        MediaFile videoFile = new MediaFile(video.getFileId(), thumbnailId, MediaType.VIDEO, message);
        handleAlbumMedia(message, videoFile, caption, entities);
    }

    public void handlePhotoAlbum(Message message, String caption, List<MessageEntity> entities) {
        String fileId = message.getPhoto().stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElseThrow();
        MediaFile photo = new MediaFile(fileId, null, MediaType.PHOTO, message);
        handleAlbumMedia(message, photo, caption, entities);
    }

    private void handleAlbumMedia(Message message, MediaFile mediaFile, String caption, List<MessageEntity> entities) {
        String mediaGroupId = message.getMediaGroupId();
        AlbumData albumData = albumCache.computeIfAbsent(mediaGroupId, k -> new AlbumData());

        albumData.getMediaFiles().add(mediaFile);
        if (albumData.getCaption() == null) {
            albumData.setCaption(caption);
            albumData.setCaptionEntities(entities);
            scheduler.schedule(() -> sendAlbumBack(message.getChatId(), mediaGroupId), 4, TimeUnit.SECONDS);
        }
    }

    private void sendAlbumBack(Long chatId, String mediaGroupId) {
        AlbumData albumData = albumCache.get(mediaGroupId);

        boolean allowedToSend = albumData.getMediaFiles().stream()
                .map(MediaFile::getMessage)
                .filter(message -> message.getCaption() != null)
                .allMatch(message -> {
                    String messageCaption = message.getCaption();
                    List<MessageEntity> captionEntities = message.getCaptionEntities();
                    return messageTextService.hasLinksInMessage(messageCaption, captionEntities)
                           && messageTextService.hasOnlyAllowedLinks(messageCaption, captionEntities);
                });

        if (allowedToSend) {
            return;
        }

        List<InputMedia> mediaList = new ArrayList<>();
        for (int i = 0; i < albumData.getMediaFiles().size(); i++) {
            MediaFile mediaFile = albumData.getMediaFiles().get(i);
            List<MessageEntity> entities = albumData.getCaptionEntities();
            String caption = albumData.getCaption();

            if (mediaFile.getMediaType() == MediaType.PHOTO) {
                InputMediaPhoto mediaPhoto = new InputMediaPhoto(mediaFile.getFileId());
                if (i == 0) {
                    caption = messageTextService.deleteLinks(mediaFile.getMessage(), caption, entities);
                    mediaPhoto.setCaption(caption);
                    mediaPhoto.setCaptionEntities(entities);
                }
                mediaList.add(mediaPhoto);
            } else if (mediaFile.getMediaType() == MediaType.VIDEO) {
                InputMediaVideo mediaVideo = new InputMediaVideo(mediaFile.getFileId());
                if (i == 0) {
                    caption = messageTextService.deleteLinks(mediaFile.getMessage(), caption, entities);
                    mediaVideo.setThumbnail(new InputFile(mediaFile.getThumbnailId()));
                    mediaVideo.setCaption(caption);
                    mediaVideo.setCaptionEntities(entities);
                }
                mediaList.add(mediaVideo);
            }
            bot.deleteMessage(chatId, albumData.getMediaFiles().get(i).getMessage().getMessageId());
        }

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId.toString());
        sendMediaGroup.setMedias(mediaList);
        try {
            bot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }

        albumCache.remove(mediaGroupId);
    }
}
