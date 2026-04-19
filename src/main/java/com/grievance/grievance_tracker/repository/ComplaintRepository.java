package com.grievance.grievance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>{

    //SELECT * FROM complaints WHERE user_id = ?
    List<Complaint> findByCitizen(User citizen);

    //SELECT * FROM complaints WHERE status = ?
    List<Complaint> findByStatus(String status);

    //SELECT * FROM complaints WHERE user_id = ? AND status = ?
    List<Complaint> findByCitizenAndStatus(User citizen, ComplaintStatus status);

    // Count how many complaints a citizen has submitted
    long countByCitizen(User citizen);

    // Count complaints by status (for admin dashboard stats)
    long countByStatus(ComplaintStatus status);
}
