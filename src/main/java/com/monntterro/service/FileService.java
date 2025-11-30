package com.monntterro.service;

import com.monntterro.model.AlbumData;
import com.monntterro.model.ProcessedMessage;
import com.monntterro.model.mediafile.MediaFile;
import com.monntterro.model.mediafile.MediaType;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private static final int ALBUM_PROCESSING_DELAY_SECONDS = 3;

    private final ConcurrentHashMap<String, AlbumData> albumCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TelegramBot bot;
    private final MessageTextService messageTextService;
    private final MeterRegistry meterRegistry;

    public void handleVideos(Message message, String caption, List<MessageEntity> entities) {
        Video video = message.getVideo();
        String thumbnailId = Optional.ofNullable(video.getThumbnail())
                .map(PhotoSize::getFileId)
                .orElse(null);

        MediaFile mediaFile = new MediaFile(video.getFileId(), thumbnailId, MediaType.VIDEO, message);
        handleAlbumMedia(message, mediaFile, caption, entities);
    }

    public void handlePhotos(Message message, String caption, List<MessageEntity> entities) {
        Optional<String> fileIdOpt = message.getPhoto().stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId);

        if (fileIdOpt.isEmpty()) {
            log.error("Could not find photo ID");
            return;
        }

        MediaFile mediaFile = new MediaFile(fileIdOpt.get(), null, MediaType.PHOTO, message);
        handleAlbumMedia(message, mediaFile, caption, entities);
    }

    private void handleAlbumMedia(Message message, MediaFile mediaFile, String caption, List<MessageEntity> entities) {
        bot.deleteMessage(message.getChatId(), message.getMessageId());

        String mediaGroupId = message.getMediaGroupId();
        AlbumData albumData = albumCache.computeIfAbsent(mediaGroupId, k -> new AlbumData());
        albumData.getMediaFiles().add(mediaFile);

        if (albumData.getCaption() == null) {
            albumData.setCaption(caption);
            albumData.setCaptionEntities(entities);
            scheduler.schedule(
                    () -> sendAlbumBack(message.getChatId(), mediaGroupId),
                    ALBUM_PROCESSING_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    private void sendAlbumBack(Long chatId, String mediaGroupId) {
        AlbumData albumData = albumCache.get(mediaGroupId);
        if (albumData == null || shouldSkipSending(albumData)) {
            meterRegistry.counter("message.skipped").increment();
            return;
        }

        bot.sendMediaGroup(chatId, createMediaList(albumData));
        albumCache.remove(mediaGroupId);
    }

    private boolean shouldSkipSending(AlbumData albumData) {
        return albumData.getMediaFiles().stream()
                .map(MediaFile::getMessage)
                .filter(message -> message.getCaption() != null)
                .allMatch(message -> {
                    String messageCaption = message.getCaption();
                    List<MessageEntity> captionEntities = message.getCaptionEntities();
                    return messageTextService.hasLinksInMessage(messageCaption, captionEntities)
                           && messageTextService.hasOnlyAllowedLinks(messageCaption, captionEntities);
                });
    }

    private List<InputMedia> createMediaList(AlbumData albumData) {
        List<InputMedia> mediaList = new ArrayList<>();
        String caption = albumData.getCaption();
        List<MessageEntity> entities = albumData.getCaptionEntities();

        for (int i = 0; i < albumData.getMediaFiles().size(); i++) {
            MediaFile mediaFile = albumData.getMediaFiles().get(i);
            boolean isFirstMedia = (i == 0);

            InputMedia media = createInputMedia(mediaFile, caption, entities, isFirstMedia);
            mediaList.add(media);
        }

        return mediaList;
    }

    private InputMedia createInputMedia(MediaFile mediaFile, String caption, List<MessageEntity> entities,
                                        boolean addCaption) {
        InputMedia media;
        if (mediaFile.getMediaType() == MediaType.PHOTO) {
            media = new InputMediaPhoto(mediaFile.getFileId());
        } else if (mediaFile.getMediaType() == MediaType.VIDEO) {
            InputMediaVideo videoMedia = new InputMediaVideo(mediaFile.getFileId());
            if (mediaFile.getThumbnailId() != null) {
                videoMedia.setThumbnail(new InputFile(mediaFile.getThumbnailId()));
            }
            media = videoMedia;
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaFile.getMediaType());
        }

        if (addCaption) {
            ProcessedMessage processedMessage = messageTextService.deleteLinks(mediaFile.getMessage(), caption, entities);
            media.setCaption(processedMessage.text());
            media.setCaptionEntities(processedMessage.entities());
        }

        return media;
    }
}
