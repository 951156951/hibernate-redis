/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.cache.redis.client;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.codec.SnappyCodec;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Factory class for RedisClient.
 *
 * @author debop sunghyouk.bae@gmail.com
 */
@Slf4j
public final class RedisClientFactory {

  private RedisClientFactory() {}

  /**
   * Create {@link RedisClient} instance by properties
   *
   * @param redissonYamlUrl redisson yaml setting file URL
   * @return {@link RedisClient} instance
   */
  public static RedisClient createRedisClient(@NonNull final URL redissonYamlUrl) {
    try {
      Config config = Config.fromYAML(redissonYamlUrl);

      if (config.getCodec() == null) {
        config.setCodec(new SnappyCodec());
      }
      log.info("Set Redisson Codec = {}", config.getCodec().getClass().getName());

      RedissonClient redisson = Redisson.create(config);
      return new RedisClient(redisson);
    } catch (IOException e) {
      log.error("Error in create RedisClient", e);
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  public static RedisClient createRedisClient(@NonNull final String redissonYamlPath) {
    log.debug("load redisson config yaml file. path={}", redissonYamlPath);

    if (redissonYamlPath.startsWith("classpath:")) {
      String path = redissonYamlPath.substring("classpath:".length());
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);

      log.debug("load redisson yaml. path={}, url={}", path, url);
      return createRedisClient(url);
    } else {
      String path = redissonYamlPath;
      if (redissonYamlPath.startsWith("file:")) {
        path = path.substring("file:".length());
      }
      File file = new File(path);
      URL url = file.toURI().toURL();

      log.debug("load redisson yaml. path={}, url={}", path, url);
      return createRedisClient(url);
    }
  }
}
