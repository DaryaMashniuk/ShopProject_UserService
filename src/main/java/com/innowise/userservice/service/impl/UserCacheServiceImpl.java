package com.innowise.userservice.service.impl;

import com.innowise.userservice.constants.CacheNames;
import com.innowise.userservice.service.UserCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class UserCacheServiceImpl implements UserCacheService {
  @Override
  @CacheEvict(value = CacheNames.USERS_WITH_CARDS, key = "#userId")
  public void evictUserCacheWithCards(Long userId) {
  }
}
