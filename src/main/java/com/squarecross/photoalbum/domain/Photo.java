package com.squarecross.photoalbum.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="photo", schema = "photo_album", uniqueConstraints = {@UniqueConstraint(columnNames = "photo_id")})
@Getter
@Setter
@NoArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id",unique = true,nullable = false)
    private Long photoId;

    @Column(name = "file_name", unique = false, nullable = true)
    private String fileName;
    @Column(name = "file_size", unique = false, nullable = true)
    private int fileSize;

    @Column(name = "thumb_url", unique = false, nullable = true)
    private String thumbUrl;

    @Column(name="original_url", unique = false, nullable = true)
    private String originalUrl;

    @Column(name="uploaded_at", unique = false, nullable = true)
    @CreationTimestamp
    private Date uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;
}
