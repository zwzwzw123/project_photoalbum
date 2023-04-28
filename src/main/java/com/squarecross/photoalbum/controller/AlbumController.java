package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<AlbumDto>> getAlbumList(@RequestParam(value = "keyword", required = false, defaultValue = "")final String keyword,
                                                       @RequestParam(value = "sort", required = false, defaultValue = "byDate") final String sort){
        List<AlbumDto> albumDtos = albumService.getAlbumList(keyword, sort);
        return new ResponseEntity<>(albumDtos, HttpStatus.OK);
    }

    /*@RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<AlbumDto>> getAlbumList2(
            @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "byDate") final String sort,
            @RequestParam(value = "orderBy", required = false, defaultValue = "desc") final String orderBy){

        List<AlbumDto> albumDtos = albumService.getAlbumList2(keyword, sort, orderBy);

        return new ResponseEntity<>(albumDtos, HttpStatus.OK);
    }*/

    @RequestMapping(value="/{albumId}", method = RequestMethod.PUT)
    public ResponseEntity<AlbumDto> updateAlbum(@PathVariable("albumId") final long albumId,
                                                @RequestBody final  AlbumDto albumDto){
        AlbumDto res = albumService.changeName(albumId, albumDto);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{albumId}",method = RequestMethod.DELETE)
    public  ResponseEntity<Void> deleteAlbum(@PathVariable("albumId") final long albumId) throws IOException {
        albumService.deleteAlbum(albumId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
