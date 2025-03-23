package com.monntterro.model;

import com.monntterro.model.mediafile.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumData {
    private String caption;
    private List<MediaFile> mediaFiles = new ArrayList<>();
    private List<MessageEntity> captionEntities = new ArrayList<>();
}
