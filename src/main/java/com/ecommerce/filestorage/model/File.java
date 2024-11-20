package com.ecommerce.filestorage.model;

import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class File {
    private String id;
    private String name;
    private String extension;
    private long size;
    private byte[] data;
    private String contentType;
    private Date createdAt;
    private Date updatedAt;
}
