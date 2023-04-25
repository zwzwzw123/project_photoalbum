package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // 1. controller에서 albumdto 입력받음
    // 2. albumdto 객체를 album객체로 변환
    // 3. 생성된 album객체를 db에 저장(albumId생성)
    // 4. photos/original,  photos/thumvail 디렉토리 내 신규 앨범 디렉토리 생성
    // 5. 생성된 앨범정보 dto로 변환해 controller출력
    public  AlbumDto createAlbum(AlbumDto albumDto) throws IOException {
        Album album = AlbumMapper.convertToModel(albumDto);
        this.albumRepository.save(album);
        this.createAlbumDirectories(album);
        return AlbumMapper.convertToDto(album);
    }

    private void createAlbumDirectories(Album album) throws IOException {
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+album.getAlbumId()));
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+album.getAlbumId()));
    }

    public void deleteAlbum(Album album) throws IOException {
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+album.getAlbumId()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+album.getAlbumId()));
    }
}
