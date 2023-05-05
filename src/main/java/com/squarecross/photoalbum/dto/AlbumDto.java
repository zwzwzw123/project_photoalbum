package com.squarecross.photoalbum.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AlbumDto {
    Long albumId;
    String albumName;
    Date createdAt;
    int count;
    private List<String> thumbUrls;

}
