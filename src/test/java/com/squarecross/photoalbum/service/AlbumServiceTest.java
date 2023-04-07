package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AlbumServiceTest {

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    AlbumService albumService;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album saveAlbum = albumRepository.save(album);

        Album resAlbum = albumService.getAlbum(saveAlbum.getAlbumId());
        assertEquals("테스트",resAlbum.getAlbumName());
    }

    @Test
    void findbyAlbum() {
        Album album = new Album();
        album.setAlbumName("앨범이름 테스트");
        Album saveAlbum = albumRepository.save(album);

        Album resAlbum = albumService.findbyAlbumname(saveAlbum.getAlbumName());
        assertEquals("앨범이름 테스트",resAlbum.getAlbumName());


    }
}