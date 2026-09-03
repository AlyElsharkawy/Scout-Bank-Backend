package org.sportingscout.scout_bank_backend.configuration;

import static org.sportingscout.scout_bank_backend.configuration.RedisNamespaces.*;
import org.sportingscout.scout_bank_backend.dtos.articles.CachedArticlePage;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleVersionWithMedia;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.JavaType;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

    Jackson2JsonRedisSerializer<CachedArticlePage> articlePageSerializer = new Jackson2JsonRedisSerializer<>(
        objectMapper,
        CachedArticlePage.class);

    Jackson2JsonRedisSerializer<ArticleVersionWithMedia> articleVersionSerializer = new Jackson2JsonRedisSerializer<>(
        objectMapper,
        ArticleVersionWithMedia.class);

    JavaType listType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, ArticleVersionWithMedia.class);
    Jackson2JsonRedisSerializer<List<ArticleVersionWithMedia>> articleVersionGroupSerializer = new Jackson2JsonRedisSerializer<>(
        objectMapper,
        listType);

    configurations.put(MEDIA,
        defaultCacheConfiguration()
            .entryTtl(Duration.ofDays(5)));

    configurations.put(ARTICLES,
        defaultCacheConfiguration().entryTtl(Duration.ofHours(2))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(articlePageSerializer)));

    /*
     * configurations.put(ARTICLE_VERSIONS,
     * defaultCacheConfiguration().entryTtl(Duration.ofMinutes(30))
     * .serializeValuesWith(
     * RedisSerializationContext.SerializationPair.fromSerializer(
     * articleVersionSerializer)));
     */

    configurations.put(ARTICLE_VERSION_GROUP,
        defaultCacheConfiguration().entryTtl(Duration.ofDays(14))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(articleVersionGroupSerializer)));

    configurations.put(ARTICLE_TAGS,
        defaultCacheConfiguration().entryTtl(Duration.ofDays(14)));

    configurations.put(ARTICLE_TYPES,
        defaultCacheConfiguration().entryTtl(Duration.ofDays(14)));

    configurations.put(ORGANIZATIONS,
        defaultCacheConfiguration().entryTtl(Duration.ZERO));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultCacheConfiguration())
        .withInitialCacheConfigurations(configurations)
        .build();
  }
}
