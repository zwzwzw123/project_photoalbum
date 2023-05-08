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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
    public  PhotoDto savePhoto(MultipartFile file, Long albumId) throws IOException {
        //앨범 아이디가 존재하는지 확인
        Optional<Album> res = albumRepository.findById(albumId);
        if(res.isEmpty()){
            throw new EntityNotFoundException("앨범이 존재하지 않습니다.");
        }

        String fileName = file.getOriginalFilename();//파일 이름을 가져옴
        int fileSize = (int)file.getSize();

        //확장자 확인
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
        String fileExtensions = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
        if(!Arrays.asList(allowedExtensions).contains(fileExtensions)){
            throw  new IllegalArgumentException("허용되지 않은 파일 확장자입니다.");
        }

        //실제 이미지 파일 확인
        BufferedImage image = ImageIO.read(file.getInputStream());
        if(image ==null){
            throw new IllegalArgumentException("이미지 파일이 아닙니다.");
        }

        fileName = getNextFileName(fileName,albumId);//파일명 존재 체크

        saveFile(file,albumId,fileName);//썸네일용 사이즈 축소

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

    //파일명 존재 유무 체크 및 이름 변경
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

    //1. 입력된 이미지 아이디로 db조회
    // - 없으면 Exception
    //2. 프로젝트 경로를 옆에 붙여 디렉토리 내에서 사진을 불러옴
    //3. 파일을 Controller로 출력
    public  File getImageFile(Long photoId){
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isEmpty()){
            throw new EntityNotFoundException(String.format("사진ID %d를 찾을 수 없습니다.",photoId));
        }
        return new File(Constants.PATH_PREFIX+res.get().getOriginalUrl());
    }

    //사진 목록 불러오기
    public List<PhotoDto> getPhotoList(String keyword, String sort){
        List<Photo> photos;
        if(Objects.equals(sort,"byName")){
            photos = photoRepository.findByFileNameContainingOrderByFileNameAsc(keyword);
        }else if(Objects.equals(sort,"byDate")){
            photos = photoRepository.findByFileNameContainingOrderByUploadedAtDesc(keyword);
        }else if(Objects.equals(sort,"byNameDesc")){
            photos = photoRepository.findByFileNameContainingOrderByFileNameDesc(keyword);
        }else if(Objects.equals(sort,"byDateAsc")){
            photos = photoRepository.findByFileNameContainingOrderByUploadedAtAsc(keyword);
        }else {
            throw new IllegalArgumentException("알 수 없는 정렬 기준 입니다.");
        }

        List<PhotoDto> photoDtos = PhotoMapper.converToDtoList(photos);

        for (PhotoDto photoDto : photoDtos){
            List<Photo> top4 = photoRepository.findTop4ByAlbum_AlbumIdOrderByUploadedAtDesc(photoDto.getPhotoId());
            photoDto.setThumbUrls(top4.stream().map(Photo::getThumbUrl).map(c -> Constants.PATH_PREFIX + c).collect(Collectors.toList()));
        }
        return photoDtos;
    }


    //사진 삭제
    public void deletePhoto(Long photoId) throws IOException {
        Optional<Photo> photo = photoRepository.findById(photoId);
        if(photo.isEmpty()){
            throw new NoSuchElementException(String.format("사진 Id '%d'가 존재하지 않습니다",photoId));
        }
        Photo deleltePhoto = photo.get();
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+deleltePhoto.getOriginalUrl()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+deleltePhoto.getThumbUrl()));

        photoRepository.deleteById(deleltePhoto.getPhotoId());

    }

    //1. photo의 filname을 가져오고 같은 이름을 toALbumId에서 체킹하여 filename수정
    //2. photo의 original, thumb주소를 toAlbumId에 맞춰서 저장
    //3. photo의 원본을 File로 저장하고 toAlbumId로 옮기기
    //4. fromAlbumId에 있던 원본 파일 삭제
    public List<PhotoDto> movePhoto(Long fromAlbumId, Long toAlbumId, List<Long> photoIds) throws IOException {
        List<Photo> photos = new ArrayList<>();
        for (Long photoId : photoIds){
            Optional<Photo> res = photoRepository.findById(photoId);
            if(res.isPresent()){
                Photo photo = res.get();
                File originalFile = new File(Constants.PATH_PREFIX+photo.getOriginalUrl());
                File thumbFile = new File(Constants.PATH_PREFIX+photo.getThumbUrl());

                //파일 이름을 받은 후 겹치는 이름을 피해 새 이름을 생성 후 photo url변경
                String fileName = photo.getFileName();
                fileName = getNextFileName(fileName,toAlbumId);
                photo.setOriginalUrl("/photos/original/"+toAlbumId+"/"+fileName);
                photo.setThumbUrl("/photos/thumb/"+toAlbumId+"/"+fileName);

                //변경된 url에 대한 파일을 받음
                File originalChangeFile = new File(Constants.PATH_PREFIX+photo.getOriginalUrl());
                File thumbChangeFile = new File(Constants.PATH_PREFIX+photo.getThumbUrl());

                //기존에 있던 사진 내용을 변경 주소로 옮김
                Files.copy(originalFile.toPath(),originalChangeFile.toPath());
                Files.copy(thumbFile.toPath(),thumbChangeFile.toPath());

                //원본 주소에 있는 파일 삭제
                deleteFiles(String.valueOf(originalFile.toPath()));
                deleteFiles(String.valueOf(thumbFile.toPath()));

                //변경된 사진 추가
                photos.add(photo);
            }else{
                throw  new EntityNotFoundException("error");
            }
        }
        return PhotoMapper.converToDtoList(photos);
    }
        private void deleteFiles(String url) throws IOException {
            Files.delete(Path.of(url));
        }
}
