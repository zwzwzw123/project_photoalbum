package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/albums/{albumId}/photos")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    //사진 상세정보 API
    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    public ResponseEntity<PhotoDto> getPhoto(@PathVariable("photoId") final Long photoId){
        PhotoDto photoDto = photoService.getPhoto(photoId);
        return new ResponseEntity<PhotoDto>(photoDto, HttpStatus.OK);
    }

    //사진 업로드 API
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<List<PhotoDto>> uploadPhotos(@PathVariable("albumId") final  Long albumId,
                                                       @RequestParam("photos") MultipartFile[] files ){
        List<PhotoDto> photos = new ArrayList<>();
        for(MultipartFile file :files){
            PhotoDto photoDto =photoService.savePhoto(file, albumId);
            photos.add(photoDto);
        }
        return new ResponseEntity<>(photos, HttpStatus.OK);
    }


}
