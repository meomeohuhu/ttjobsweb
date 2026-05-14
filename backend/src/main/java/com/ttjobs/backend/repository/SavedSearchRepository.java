package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.SavedSearch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<SavedSearch> findByActiveTrueOrderByUpdatedAtDesc();
    Optional<SavedSearch> findByIdAndUserId(Long id, Long userId);
    Optional<SavedSearch> findFirstByUserIdAndNameOrderByUpdatedAtDesc(Long userId, String name);
}
