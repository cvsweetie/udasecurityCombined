package com.udacity.catpoint.imageservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AWSRekognitionClient {
    private Logger log = LoggerFactory.getLogger(LoggerFactory.class);
    //aws recommendation is to maintain only a single instance of client objects
    private static RekognitionClient rekognitionClient;

    private AWSRekognitionClient() {

    }

    public static RekognitionClient getInstance() {
        Properties props = new Properties();
        try (InputStream is = AWSRekognitionClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        } catch (IOException ioe ) {
            return null;
        }

        String awsId = props.getProperty("aws.id");
        String awsSecret = props.getProperty("aws.secret");
        String awsRegion = props.getProperty("aws.region");
        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);

        if (rekognitionClient==null) {
            rekognitionClient =RekognitionClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(Region.of(awsRegion))
                    .build();
        }
        return rekognitionClient;
    }
}
