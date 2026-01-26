package com.innowise.userservice.service.impl;

import com.innowise.userservice.service.UserCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class UserCacheServiceImpl implements UserCacheService {

  /**
   * Evicts cached user data with associated payment cards.
   * Cache eviction is handled declaratively via {@link CacheEvict} annotation.
   *
   * @param userId ID of the user whose cached data should be evicted
   */
  @Override
  @CacheEvict(value = "users-with-cards", key = "#userId")
  public void evictUserCacheWithCards(Long userId) {
    // Cache eviction is handled declaratively via @CacheEvict annotation
    // No explicit implementation needed
  }
}
