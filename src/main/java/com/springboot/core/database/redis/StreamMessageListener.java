package com.springboot.core.database.redis;

import com.google.common.collect.ImmutableMap;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import javax.annotation.PostConstruct;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Component;

@Component
public class StreamMessageListener implements
    StreamListener<String, MapRecord<String, String, String>> {

  private final static String STREAM_NAME = "TEST1";

  private final StringRedisTemplate stringRedisTemplate;
  private final RedisConnectionFactory connectionFactory;

  public StreamMessageListener(
      StringRedisTemplate stringRedisTemplate, RedisConnectionFactory connectionFactory) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    this.stringRedisTemplate.opsForStream().acknowledge(STREAM_NAME, message);
  }

  @PostConstruct
  public void init() {
    String ip = null;
    try {
      InetAddress address = InetAddress.getLocalHost();
      byte[] ipAddr = address.getAddress();
      StringBuilder ipAddrStr = new StringBuilder();
      for (int i = 0; i < ipAddr.length; i++) {
        if (i > 0) {
          ipAddrStr.append(".");
        }
        ipAddrStr.append(ipAddr[i] & 0xFF);
      }
      ip = ipAddrStr.toString();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    createStreamMessageContainer(ip);
  }

  private void createStreamMessageContainer(String ip) {
    StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
        .builder()
        .batchSize(10)
        .pollTimeout(Duration.ofMillis(100))
        .serializer(new StringRedisSerializer())
        .build();
    StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
        .create(connectionFactory, options);
    container.receive(Consumer.from(ip, ip),
        StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
        this);
    stringRedisTemplate.opsForStream()
        .add(MapRecord.create(STREAM_NAME, ImmutableMap.of(STREAM_NAME, "first")));
    try {
      stringRedisTemplate.opsForStream().createGroup(STREAM_NAME, ip);
    } catch (Exception e) {
      e.printStackTrace();
    }
    container.start();
  }
}
