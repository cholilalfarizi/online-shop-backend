package lib.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lib.i18n.utility.MessageUtil;
import lib.minio.configuration.property.MinioProp;
import lib.minio.exception.MinioServiceDownloadException;
import lib.minio.exception.MinioServiceUploadException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinioSrvc {
    public static final Long DEFAULT_EXPIRY = TimeUnit.HOURS.toSeconds(1);

    public Long getDefaultExpiry() {
        return DEFAULT_EXPIRY;
    }

    private final MinioClient minio;
    private final MinioProp prop;
    private final MessageUtil message;

    private static String bMsg(String bucket) {
        return "bucket " + bucket;
    }

    private static String bfMsg(String bucket, String filename) {
        return bMsg(bucket) + " of file " + filename;
    }

    public String getPublicLink(String filename, String defaultFile, Long expiry) {
        return getLink(filename, defaultFile, expiry);
    }

    public String getLink(String filename, String defaultFile, Long expiry) {
        try {
            minio.statObject(
                    StatObjectArgs.builder()
                            .bucket(prop.getBucketName())
                            .object(filename)
                            .build());

            return getObjectUrl(filename);
        } catch (ErrorResponseException e) {
            // Check if the error is due to the object not existing
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                log.info("Object '{}' not found in bucket '{}': {}. Used default file for alternative.", filename,
                        prop.getBucketName(), e.getLocalizedMessage());

                return getObjectUrl(defaultFile);
            } else {
                // Handle other MinIO errors
                log.error(message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)) + ": "
                        + e.getLocalizedMessage(), e);
                throw new MinioServiceDownloadException(
                        message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)), e);
            }
        } catch (InvalidKeyException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
                | IllegalArgumentException | IOException e) {
            log.error(message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)) + ": "
                    + e.getLocalizedMessage(), e);
            throw new MinioServiceDownloadException(
                    message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)), e);
        }
    }

    public String getFileUrl(String filename, String defaultFile) {
        return filename != null ? this.getLink(filename, defaultFile, DEFAULT_EXPIRY) : "";
    }

    private String getObjectUrl(String filename) {
        try {
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(prop.getBucketName())
                            .object(filename)
                            .build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
                | IllegalArgumentException | IOException e) {
            log.error(message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)) + ": "
                    + e.getLocalizedMessage(), e);
            throw new MinioServiceDownloadException(
                    message.get(prop.getGetErrorMessage(), bfMsg(prop.getBucketName(), filename)), e);
        }
    }

    @Data
    @Builder
    public static class UploadOption {
        private String filename;
    }

    public ObjectWriteResponse upload(MultipartFile file, String bucket,
            Function<MultipartFile, UploadOption> modifier) {
        UploadOption opt = modifier.apply(file);
        try {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(opt.filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            log.error(
                    message.get(prop.getPostErrorMessage(), bfMsg(bucket, opt.filename)) + ": "
                            + e.getLocalizedMessage(),
                    e);
            throw new MinioServiceUploadException(
                    message.get(prop.getPostErrorMessage(), bucket, opt.filename), e);
        }
    }

    public ObjectWriteResponse upload(MultipartFile file, String bucket) {
        return this.upload(file, bucket,
                o -> UploadOption.builder()
                        .filename(System.currentTimeMillis() + "_-_"
                                + o.getOriginalFilename().replace(" ", "_"))
                        .build());
    }

    // ---

    public ObjectWriteResponse upload(InputStream file, String filename, String bucket) {
        try {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(file, file.available(), -1)
                            .build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            log.error(message.get(prop.getPostErrorMessage(), bfMsg(bucket, filename)) + ": " + e.getLocalizedMessage(),
                    e);
            throw new MinioServiceUploadException(
                    message.get(prop.getPostErrorMessage(), bucket, filename), e);
        }
    }

    public void delete(String objName) throws InvalidKeyException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
            ServerException, XmlParserException, IllegalArgumentException, IOException {
        minio.removeObject(RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(objName).build());
    }
}
