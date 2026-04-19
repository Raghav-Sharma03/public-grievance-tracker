package com.grievance.grievance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    //SELECT * FROM comments WHERE complaint_id = ?
    List<Comment> findByComplaint(Complaint complaint);

    // Get all comments for a complaint ordered by newest first
    List<Comment> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
}
