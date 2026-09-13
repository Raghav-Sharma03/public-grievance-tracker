package com.grievance.grievance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.StatusHistory;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByComplaintOrderByChangedAtDesc(Complaint complaint);
}
