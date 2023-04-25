package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    public AlbumDto getAlbum(Long albumId){
        Optional<Album> res = albumRepository.findById(albumId);
        if(res.isPresent()){
            AlbumDto albumDto = AlbumMapper.convertToDto(res.get());
            albumDto.setCount(photoRepository.countByAlbum_AlbumId(albumId));
            return albumDto;
        }else {
            throw new EntityNotFoundException(String.format("앨범아이디%d로 조회되지않습니다.", albumId));
        }
    }

    //앨범명으로 앨범 테이블을 검색
    public Album getAlbumName(String albumName){
        Optional<Album> res = albumRepository.findByalbumName(albumName);
        if (res.isPresent()) {
            return res.get();
        }else {
            throw new EntityNotFoundException(String.format("앨범명%d로 조회되지않습니다.", albumName));
        }
    }

}
