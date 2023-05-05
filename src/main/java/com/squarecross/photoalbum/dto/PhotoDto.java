package com.squarecross.photoalbum.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PhotoDto {

    private Long photoId;
    private String fileName;
    private int fileSize;
    private String thumbUrl;
    private String originalUrl;
    private Date uploadedAt;
    private Long albumId;

    private List<String> thumbUrls;

}
