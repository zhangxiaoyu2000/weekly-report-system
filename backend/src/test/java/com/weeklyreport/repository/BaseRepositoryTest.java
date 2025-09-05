package com.weeklyreport.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base repository test class providing common test infrastructure.
 * This class serves as a template and common configuration for all repository tests.
 * 
 * @DataJpaTest provides:
 * - Only loads @Entity classes and Spring Data JPA repositories
 * - Uses in-memory database (H2) for tests
 * - Configures TestEntityManager for direct entity operations
 * - Each test method runs in a transaction that's rolled back
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @Test
    void contextLoads() {
        assertThat(entityManager).isNotNull();
    }

    /**
     * Helper method to flush and clear the persistence context.
     * Useful for ensuring that database operations are actually executed.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Helper method to persist an entity and flush immediately.
     * @param entity The entity to persist
     * @param <T> The entity type
     * @return The persisted entity
     */
    protected <T> T persistAndFlush(T entity) {
        T result = entityManager.persistAndFlush(entity);
        entityManager.clear(); // Clear to ensure we read from database
        return result;
    }

    /**
     * Helper method to find an entity by ID and ensure it's detached.
     * @param entityClass The entity class
     * @param id The entity ID
     * @param <T> The entity type
     * @param <ID> The ID type
     * @return The found entity or null
     */
    protected <T, ID> T findDetached(Class<T> entityClass, ID id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.detach(entity);
        }
        return entity;
    }
}

/*
 * Template for individual repository test classes:
 * 
 * @DataJpaTest
 * @ActiveProfiles("test") 
 * class UserRepositoryTest extends BaseRepositoryTest {
 *
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     @Test
 *     void canSaveAndFindUser() {
 *         // Given
 *         User user = new User();
 *         user.setUsername("testuser");
 *         user.setEmail("test@example.com");
 *
 *         // When
 *         User savedUser = userRepository.save(user);
 *         flushAndClear();
 *         
 *         Optional<User> foundUser = userRepository.findById(savedUser.getId());
 *
 *         // Then
 *         assertThat(foundUser).isPresent();
 *         assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
 *         assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
 *     }
 *
 *     @Test
 *     void canFindByUsername() {
 *         // Given
 *         User user = createTestUser("testuser", "test@example.com");
 *         persistAndFlush(user);
 *
 *         // When
 *         Optional<User> foundUser = userRepository.findByUsername("testuser");
 *
 *         // Then
 *         assertThat(foundUser).isPresent();
 *         assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
 *     }
 *
 *     @Test
 *     void canDeleteUser() {
 *         // Given
 *         User user = createTestUser("testuser", "test@example.com");
 *         User savedUser = persistAndFlush(user);
 *
 *         // When
 *         userRepository.deleteById(savedUser.getId());
 *         flushAndClear();
 *
 *         // Then
 *         Optional<User> foundUser = userRepository.findById(savedUser.getId());
 *         assertThat(foundUser).isEmpty();
 *     }
 *
 *     private User createTestUser(String username, String email) {
 *         User user = new User();
 *         user.setUsername(username);
 *         user.setEmail(email);
 *         user.setPassword("encoded_password");
 *         user.setCreatedAt(LocalDateTime.now());
 *         user.setActive(true);
 *         return user;
 *     }
 * }
 */