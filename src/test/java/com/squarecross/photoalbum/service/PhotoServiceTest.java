package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PhotoServiceTest {
    @Autowired
    PhotoService photoService;

    @Autowired
    PhotoRepository photoRepository;

    @Test
    void getPhoto(){
        Photo photo = new Photo();
        photo.setPhotoId(1L);
        //photo.set
        Photo savePhoto = photoRepository.save(photo);

        PhotoDto photoDto = photoService.getPhoto(savePhoto.getPhotoId());
        assertEquals(1L, photoDto.getPhotoId());

    }
}
