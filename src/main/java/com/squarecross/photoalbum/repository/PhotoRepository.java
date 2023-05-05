package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo,Long> {
    int countByAlbum_AlbumId(Long albumId);

    List<Photo> findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(Long AlbumId); //최신 이미지 4장을 가져옴

    List<Photo> findByAlbum_AlbumId(Long albumId);

    //Long findByAlbumId_photoId(Long photoId);

    //파일명 존재 유무 체크
    Optional<Photo> findByFileNameAndAlbum_AlbumId(String photoName, Long albumId);

    List<Photo> findByFileNameContainingOrderByFileNameAsc(String keyword);
    List<Photo> findByFileNameContainingOrderByFileNameDesc(String keyword);
    List<Photo> findByFileNameContainingOrderByUploadedAtDesc(String keyword);
    List<Photo> findByFileNameContainingOrderByUploadedAtAsc(String keyword);

}
