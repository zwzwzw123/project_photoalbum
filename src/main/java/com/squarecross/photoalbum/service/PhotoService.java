package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private AlbumRepository albumRepository;

    private final String original_path = Constants.PATH_PREFIX+"/photos/original";
    private final String thumb_path = Constants.PATH_PREFIX+"/photos/thumb";


    public PhotoDto getPhoto(Long photoId){
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isPresent()){
            PhotoDto photoDto = PhotoMapper.convertToDto(res.get());
            //앨범Id set으로 주입
           // photoDto.setAlbumId(photoRepository.findByAlbumId_photoId(photoId));
            return photoDto;
        }else {
            throw new EntityNotFoundException(String.format("사진아이디 '%d'로 조회되지 않습니다.",photoId));
        }
    }

    //1. 사진 이미지파일 기본정보 추출 : 파일명, 파일용량
    //2. 파일명이 존재하는지 파악
    // - 예) photo.jpg가 있으면 photo1.jpg, photo2.jpg등등 숫자가 증가
    // - DB에 입력된 앨범 내에 파일명이 이미 존재하는지 조회
    //  - 있으면 +1
    //  - 없을때 까지 계속 진행
    //3. 이미지파일 저장
    // - Thumbnail 이미지파일 Original에서 max 300*300으로 사이즈 축소
    // - Original 이미지 파일 /photos/original/{albumId}/filename 디렉토리 저장
    // - Thumbmail 이미지 파일 /photos/thumb/{albumId}/filename 디렉토리 저정
    //4. 파일 저장 성공시 Database에 사진 레코드 Insert
    public  PhotoDto savePhoto(MultipartFile file, Long albumId){
        //앨범 아이디가 존재하는지 확인
        Optional<Album> res = albumRepository.findById(albumId);
        if(res.isEmpty()){
            throw new EntityNotFoundException("앨범이 존재하지 않습니다.");
        }

        String fileName = file.getOriginalFilename();//파일 이름을 가져옴
        int fileSize = (int)file.getSize();
        //파일명 존재 체크
        fileName = getNextFileName(fileName,albumId);
        //썸네일용 사이즈 축소
        saveFile(file,albumId,fileName);

        //db에 사진 레코드 생성 & 생성된 앨범dto반환
        Photo photo = new Photo();
        photo.setOriginalUrl("/photos/original/"+albumId+"/"+fileName);
        photo.setThumbUrl("/photos/thumb/"+albumId+"/"+fileName);
        photo.setFileName(fileName);
        photo.setFileSize(fileSize);
        photo.setAlbum(res.get());
        Photo createdPhoto = photoRepository.save(photo);
        return PhotoMapper.convertToDto(createdPhoto);

    }

    private String getNextFileName(String fileName, Long albumId){
        String fileNameNoExt = StringUtils.stripFilenameExtension(fileName); //확장자 추출
        String ext = StringUtils.getFilenameExtension(fileName);//추출한 확장자 제거

        Optional<Photo> res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName,albumId);

        int count = 2;
        while (res.isPresent()){
            fileName = String.format("%s (%d).%s", fileNameNoExt,count,ext);
            res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName,albumId);
            count++;
        }
        return fileName;
    }
    //이미지 저장
    private void saveFile(MultipartFile file, Long AlbumId, String fileName)   {
        try {
            //원본 이미지 저장
            String filePath = AlbumId+"/"+fileName;
            Files.copy(file.getInputStream(), Paths.get(original_path+"/"+filePath));

            //Scalr라이브러리를 이용해 resize
            BufferedImage thumbImg = Scalr.resize(ImageIO.read(file.getInputStream()),Constants.THUMB_SIZE, Constants.THUMB_SIZE);
            file.getInputStream().close();
            //resize된 썸네일 저장
            File thumFile = new File(thumb_path+"/"+filePath);
            String ext = StringUtils.getFilenameExtension(fileName);
            if(ext ==null){
                throw  new IllegalArgumentException("No Extention");
            }
            ImageIO.write(thumbImg,ext,thumFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error:"+e.getMessage());
        }
    }

}
