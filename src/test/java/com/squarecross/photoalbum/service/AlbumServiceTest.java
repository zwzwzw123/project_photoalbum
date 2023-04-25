package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AlbumServiceTest {

    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    AlbumService albumService;

    @Autowired
    PhotoRepository photoRepository;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album saveAlbum = albumRepository.save(album);

        AlbumDto resAlbum =albumService.getAlbum(saveAlbum.getAlbumId());
        assertEquals("테스트",resAlbum.getAlbumName());
    }

    @Test
    void getAlbumName() {
        Album album = new Album();
        album.setAlbumName("test");
        Album saveAlbum = albumRepository.save(album);
        Album resAlbum = albumService.getAlbumName(saveAlbum.getAlbumName());
        assertEquals("test",resAlbum.getAlbumName());
    }

    @Test
    void testPhotoCount(){
        Album album = new Album();
        album.setAlbumName("테스트");
        Album saveAlbum = albumRepository.save(album);

        Photo photo1 = new Photo();
        photo1.setFileName("사진1");
        photo1.setAlbum(saveAlbum);
        photoRepository.save(photo1);

        Photo photo2 = new Photo();
        photo2.setFileName("사진2");
        photo2.setAlbum(saveAlbum);
        photoRepository.save(photo2);

        assertEquals(2,photoRepository.countByAlbum_AlbumId(saveAlbum.getAlbumId()));
    }

    //createAlbum 메서드 테스팅
    @Test
    void testAlbumCreate() throws IOException {
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("test");
        AlbumDto albumDto1 = albumService.createAlbum(albumDto);
        assertTrue(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+albumDto1.getAlbumId())));
        assertTrue(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+albumDto1.getAlbumId())));
    }

    //deleteAlbum 메서드 테스팅
    @Test
    void testAlbumDelete() throws IOException {
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("test111");
        AlbumDto albumDto1 = albumService.createAlbum(albumDto);

        Album album = AlbumMapper.convertToModel(albumDto1);
        albumService.deleteAlbum(album);
        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+albumDto1.getAlbumId())));
        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+albumDto1.getAlbumId())));
    }


}