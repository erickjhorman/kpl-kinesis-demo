package com.example.kinesis.kplkinesislibrary;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.example.kinesis.kplkinesislibrary.model.Car;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class KplKinesisLibraryApplication implements CommandLineRunner {

	private final KinesisProducer kinesisProducer;
	private final ObjectMapper objectMapper;
	private final FutureCallback<UserRecordResult> futureCallback;

	public static void main(String[] args) {
		SpringApplication.run(KplKinesisLibraryApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String data;
		try {
			data = objectMapper.writeValueAsString(new Car(UUID.randomUUID().toString(),"Toyota"));
		} catch (JsonProcessingException e) {
			log.info("Failed in convert object" + e.getMessage());
			throw new RuntimeException(e);
		}

		var resultListenableFuture = kinesisProducer.addUserRecord("aws-data-stream",
				UUID.randomUUID().toString(),
				ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));
		Futures.addCallback(resultListenableFuture, futureCallback, MoreExecutors.directExecutor());
		kinesisProducer.flushSync();
		log.info("finish sending log to kinesis");
	}
}
