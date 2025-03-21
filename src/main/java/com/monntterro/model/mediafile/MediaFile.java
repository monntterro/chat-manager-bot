package com.monntterro.model.mediafile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {
    private String fileId;
    private String thumbnailId;
    private MediaType mediaType;
}
