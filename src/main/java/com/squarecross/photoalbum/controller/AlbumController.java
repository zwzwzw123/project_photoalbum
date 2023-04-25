package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    AlbumService albumService;

    @RequestMapping(value = "/{albumId}", method = RequestMethod.GET)
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable("albumId") final Long albumId){
        AlbumDto album = albumService.getAlbum(albumId);
        return new ResponseEntity<AlbumDto>(album, HttpStatus.OK);
    }

    //Query String으로 albumId받는 메서드
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseEntity<AlbumDto> getAlbumByQuery(@RequestParam(value = "albumId") final Long albumId){
        AlbumDto albumDto = albumService.getAlbum(albumId);
        return new ResponseEntity<AlbumDto>(albumDto,HttpStatus.OK);
    }

    //Json Body로 albumId를 받는 메서드
    @RequestMapping(value = "/json_body", method = RequestMethod.POST)
    public ResponseEntity<AlbumDto> getAlbumByJson(@RequestBody final AlbumDto  albumDto){
        AlbumDto albumdto = albumService.getAlbum(albumDto.getAlbumId());
        return new ResponseEntity<AlbumDto>(albumdto,HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<AlbumDto> createAlbum (@RequestBody final AlbumDto albumDto) throws IOException {
        AlbumDto saveAlbumDto = albumService.createAlbum(albumDto);
        return new ResponseEntity<>(saveAlbumDto, HttpStatus.OK);
    }

}
