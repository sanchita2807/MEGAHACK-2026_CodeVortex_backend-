package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop10ByOrderByTimestampDesc();
}
