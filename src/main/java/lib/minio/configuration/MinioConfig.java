package lib.minio.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lib.minio.configuration.property.MinioProp;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient(MinioProp props) {
        return MinioClient.builder()
                .endpoint(props.getUrl())
                .credentials(props.getUsername(), props.getPassword())
                .build();
    }
}
