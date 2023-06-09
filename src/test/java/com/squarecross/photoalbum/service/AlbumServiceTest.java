package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Test
    void testAlbumRepository() throws InterruptedException {
        Album album = new Album();
        Album album1 = new Album();
        album.setAlbumName("aaa");
        album1.setAlbumName("aab");

        albumRepository.save(album);
        TimeUnit.SECONDS.sleep(1);
        albumRepository.save(album1);

        //최신정렬, 두번째로 생성한 앨범이 먼저 출력되야함
        List<Album> resDate = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc("aa");
        assertEquals("aab", resDate.get(0).getAlbumName());
        assertEquals("aaa", resDate.get(1).getAlbumName());
        assertEquals(2,resDate.size());

        //앨범명 정렬, aaa->aab로 출력되야함
        List<Album> resName = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc("aa");
        assertEquals("aaa", resName.get(0).getAlbumName());
        assertEquals("aab", resName.get(1).getAlbumName());
        assertEquals(2,resName.size());

    }

    @Test
    void testChangeAlbumName() throws IOException {
        //앨범생성
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("변경 전");
        AlbumDto res = albumService.createAlbum(albumDto);

        //생성된 앨범 아이디 추출
        Long albumId = res.getAlbumId();
        AlbumDto updateDto = new AlbumDto();
        updateDto.setAlbumName("변경 후");
        albumService.changeName(albumId, updateDto);

        AlbumDto updatedDto = albumService.getAlbum(albumId);

        //앨범명이 변경되었는지 확인
        assertEquals("변경 후",updatedDto.getAlbumName());
    }

    @Test
    void testDeleteAlbum() throws IOException {
        Long albumId = 4L;

        albumService.deleteAlbum(albumId);
        List<Photo> delPhotos = photoRepository.findByAlbum_AlbumId(albumId);

        for(Photo photo : delPhotos){
            assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+photo.getOriginalUrl())));
            assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+photo.getThumbUrl())));
        }

        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+albumId)));
        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+albumId)));

        assertEquals(Optional.empty(), albumRepository.findById(albumId));

    }
}