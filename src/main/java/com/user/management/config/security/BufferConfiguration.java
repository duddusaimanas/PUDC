package com.user.management.config.security;

import java.io.IOException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.user.management.entity.LocalKeyDetails;
import com.user.management.entity.LocalPortalDetails;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BufferConfiguration {

    @Bean
    public RedisTemplate<String, LocalKeyDetails> redisTemplateForKey(RedisConnectionFactory connectionFactory) {
        var redisTemplate = new RedisTemplate<String, LocalKeyDetails>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(
                new Jackson2JsonRedisSerializer<LocalKeyDetails>(objectMapper(), LocalKeyDetails.class));
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, LocalPortalDetails> redisTemplateForPortal(
            RedisConnectionFactory connectionFactory) {
        var redisTemplate = new RedisTemplate<String, LocalPortalDetails>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(
                new Jackson2JsonRedisSerializer<LocalPortalDetails>(objectMapper(), LocalPortalDetails.class));
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var module = new SimpleModule();
        module.addSerializer(new KeySerializer());
        module.addDeserializer(SecretKey.class, new KeyDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    class KeySerializer extends StdSerializer<SecretKey> {

        KeySerializer() {
            super(SecretKey.class);
        }

        @Override
        public void serialize(SecretKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeBinary(value.getEncoded());
        }
    }

    class KeyDeserializer extends StdDeserializer<SecretKey> {

        KeyDeserializer() {
            super(SecretKey.class);
        }

        @Override
        public SecretKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            var secret = Encoders.BASE64.encode(p.getBinaryValue());
            return new SecretKeySpec(Decoders.BASE64.decode(secret), "AES");
        }
    }
}
