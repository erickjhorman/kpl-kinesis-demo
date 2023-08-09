package com.example.kinesis.kplkinesislibrary.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordFailedException;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KinesisConfig {

    @Bean
    public KinesisProducer kinesisProducer() {
        return new KinesisProducer(kinesisProducerConfiguration());
    }

    @Bean
    public KinesisProducerConfiguration kinesisProducerConfiguration() {
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv("region");


        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return new KinesisProducerConfiguration()
                .setCredentialsProvider(new AWSStaticCredentialsProvider(awsCredentials))
                .setVerifyCertificate(false)
                .setRecordMaxBufferedTime(6000)
                .setMaxConnections(1)
                .setRegion(region)
                .setRecordTtl(3000);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public FutureCallback<UserRecordResult> futureCallback() {
        return new FutureCallback<>() {
            @Override
            public void onFailure(Throwable t) {
                log.error(t.getMessage());
                if(t instanceof UserRecordFailedException) {
                    log.error("sequence number" + " " + ((UserRecordFailedException) t).getResult()
                            .getSequenceNumber());
                }
            }
            @Override
            public void onSuccess(UserRecordResult result) {
                log.info(result.getShardId() + " sequence number " + result.getSequenceNumber());
            }
        };
    }

}
