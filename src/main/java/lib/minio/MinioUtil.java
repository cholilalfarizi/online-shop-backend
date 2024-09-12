package lib.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lib.minio.MinioSrvc.UploadOption;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinioUtil {
    private final MinioSrvc minioService;

    public static final String BUCKET_NAME = "simple-online-shop";

    public String getImageURL(String filename, String defaultFile) {
        return filename != null ? removeQueryString(minioService.getFileUrl(filename, defaultFile)) : "";
    }

    private String removeQueryString(String url) {
        int questionMarkIndex = url.indexOf("?");

        if (questionMarkIndex != -1) {
            return url.substring(0, questionMarkIndex);
        }

        return url;
    }

    public String uploadFileMinio(MultipartFile file, String name, String type, String number) {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        String fileName = name + "_" + type + "_" + number + "_" + System.currentTimeMillis() + extension;

        minioService.upload(file, BUCKET_NAME, o -> UploadOption.builder()
                .filename(fileName)
                .build());

        return fileName;
    }

    public void deleteFileMinio(String filename) {
        if (filename != null && !filename.isEmpty()) {
            try {
                minioService.delete(filename);
            } catch (Exception e) {
                System.err.println("Error deleting file from Minio: " + e.getMessage());
            }
        }
    }

}
