package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //1. 날짜 기준 정렬, 앨범명 기준 정렬로 나누어서 별도로 repository 호출해서 앨범 도메인 리스트를 반환받음
    //  - AlbumRepository에 사용자 정의 메서드 추가 필요
    //2. 앨범 도메인 리스트를 dto 리스트로 매핑
    //3. 각 앨범마다 가장 최신 업로드된 이미지 4장 썸네일 URL DTO에 필드값 입력
    //4. 앨범 DTO 리스트 반환
    public List<AlbumDto> getAlbumList(String keyword, String sort) {
        List<Album> albums;
        if (Objects.equals(sort, "byName")) {
            albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc(keyword);
        } else if (Objects.equals(sort, "byDate")) {
            albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc(keyword);
        } else {
            throw new IllegalArgumentException("알 수 없는 정렬 기준입니다.");
        }
        List<AlbumDto> albumDtos = AlbumMapper.convertToDtoList(albums);

        for (AlbumDto albumDto : albumDtos) {
            List<Photo> top4 = photoRepository.findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(albumDto.getAlbumId());
            albumDto.setThumbUrls(top4.stream().map(Photo::getThumbUrl).map(c -> Constants.PATH_PREFIX + c).collect(Collectors.toList()));
        }
        return albumDtos;
    }

    public List<AlbumDto> getAlbumList2(String keyword, String sort, String orderBy) {
        List<Album> albums;
        if (Objects.equals(sort, "byName")) {
            if(Objects.equals(orderBy, "desc")){
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameDesc(keyword);
            }else{
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc(keyword);
            }
        } else if (Objects.equals(sort, "byDate")) {
            if(Objects.equals(orderBy,"asc")){
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtAsc(keyword);
            }else{
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc(keyword);
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 정렬 기준입니다.");
        }
        List<AlbumDto> albumDtos = AlbumMapper.convertToDtoList(albums);

        for (AlbumDto albumDto : albumDtos) {
            List<Photo> top4 = photoRepository.findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(albumDto.getAlbumId());
            albumDto.setThumbUrls(top4.stream().map(Photo::getThumbUrl).map(c -> Constants.PATH_PREFIX + c).collect(Collectors.toList()));
        }
        return albumDtos;
    }



}
