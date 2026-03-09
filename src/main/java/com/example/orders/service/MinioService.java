package com.example.orders.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void uploadFile(String orderId, String fileName, MultipartFile file) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            String objectName = orderId + "/" + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки в MinIO: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String orderId, String fileName) {
        try {
            String objectName = orderId + "/" + fileName;

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Ошибка скачивания из MinIO: " + e.getMessage());
        }
    }

    public void deleteFile(String orderId, String fileName) {
        try {
            String objectName = orderId + "/" + fileName;

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Ошибка удаления из MinIO: " + e.getMessage());
        }
    }
}