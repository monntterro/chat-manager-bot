package com.monntterro.model.mediafile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {
    private String fileId;
    private String thumbnailId;
    private MediaType mediaType;
    private Message message;
}
