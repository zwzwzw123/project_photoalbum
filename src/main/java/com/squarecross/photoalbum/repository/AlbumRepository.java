package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    //앨범명으로 앨범 테이블을 검색하는 메서드
    Optional<Album> findByalbumName(String albumName);

}
