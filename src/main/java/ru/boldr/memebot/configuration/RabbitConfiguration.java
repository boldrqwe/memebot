package ru.boldr.memebot.configuration;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.*;

@Configuration
public class RabbitConfiguration {
    private String  sss;

    @Bean
    ConnectionFactory connectionFactory(){
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        return connectionFactory;


    }





}
