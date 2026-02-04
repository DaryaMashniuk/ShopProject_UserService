package com.innowise.userservice.service;

/**
 * Provides centralized cache eviction for user-related cached data.
 *
 * <p>
 * This service is introduced to explicitly manage cache invalidation
 * when user-related aggregates (e.g. payment cards) are modified.
 * </p>
 *
 * <p>
 * <b>Design decision:</b>
 * <ul>
 *   <li>Avoids spreading {@code @CacheEvict} annotations across multiple domain services</li>
 *   <li>Prevents coupling cache keys to DTO structures or method return values</li>
 * </ul>
 * </p>
 *
 * <p>
 * This approach is intentionally chosen over:
 * <ul>
 *   <li>Decorator pattern</li>
 *   <li>Domain events</li>
 *   <li>Aspect-oriented cache eviction</li>
 * </ul>
 * due to the small scope of the application and to keep the solution simple and maintainable.
 * </p>
 */
public interface UserCacheService {
  void evictUserCacheWithCards(Long userId);
}
