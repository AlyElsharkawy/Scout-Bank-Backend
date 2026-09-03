package org.sportingscout.scout_bank_backend.configuration;

import org.sportingscout.scout_bank_backend.dtos.articles.CachedArticlePage;
import org.springframework.boot.cache.autoconfigure.CacheProperties.Redis;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

@EnableCaching
@Configuration
public class RedisConfig {
  private final ObjectMapper objectMapper;

  RedisConfig() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Bean
  public RedisCacheConfiguration defaultCacheConfiguration() {

    Jackson2JsonRedisSerializer<Object> genericSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,
        Object.class);

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5))
        .disableCachingNullValues()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer));
  }

  @Bean
  public RedisCacheManager redisCacheManagerBuilderCustomizer(RedisConnectionFactory connectionFactory) {
    Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

    Jackson2JsonRedisSerializer<CachedArticlePage> articleSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,
        CachedArticlePage.class);

    Jackson2JsonRedisSerializer<String> mediaSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,
        String.class);

    configurations.put("media",
        defaultCacheConfiguration()
            .entryTtl(Duration.ofDays(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(mediaSerializer)));

    configurations.put("articles",
        defaultCacheConfiguration().entryTtl(Duration.ofHours(2))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(articleSerializer)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultCacheConfiguration())
        .withInitialCacheConfigurations(configurations)
        .build();
  }
}
