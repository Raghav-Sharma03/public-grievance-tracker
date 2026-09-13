package com.grievance.grievance_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Count complaints by citizen and status
    long countByCitizenAndStatus(User citizen, ComplaintStatus status);

    // Paginated complaints by citizen
    Page<Complaint> findByCitizen(User citizen, Pageable pageable);

    // Paginated all complaints for admin
    Page<Complaint> findAll(Pageable pageable);

    // Count complaints by status
    long countByStatus(ComplaintStatus status);

    // Count by citizen
    long countByCitizen(User citizen);
}