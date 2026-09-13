package com.grievance.grievance_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT c FROM Complaint c WHERE "
            + "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(c.category) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(c.citizen.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND (:status IS NULL OR c.status = :status) "
            + "ORDER BY c.createdAt DESC")
    Page<Complaint> searchComplaints(
            @Param("search") String search,
            @Param("status") ComplaintStatus status,
            Pageable pageable);
}
