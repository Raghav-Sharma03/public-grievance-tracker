package com.grievance.grievance_tracker.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.repository.CommentRepository;
import com.grievance.grievance_tracker.repository.ComplaintRepository;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CommentRepository commentRepository;

    // Submit a new complaint
    public Complaint submitComplaint(Complaint complaint , User citizen,MultipartFile imageFile) throws IOException {
        
        complaint.setCitizen(citizen);
        complaint.setStatus(ComplaintStatus.PENDING);

        // Handle image upload if provided
        if(imageFile != null && !imageFile.isEmpty()){
            String imagePath = saveImage(imageFile);
            complaint.setImagePath(imagePath);
        }
        return complaintRepository.save(complaint);

    }

    private String saveImage(MultipartFile imageFile) throws IOException {

         // Create uploads folder if it doesn't exist
        String uploadDir = "uploads/complaints/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Give the file a unique name to avoid conflicts
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uploadDir + fileName;
    }

    // Get all complaints by a citizen (for citizen dashboard)
    public List<Complaint> getComplaintsByCitizen(User citizen) {
        return complaintRepository.findByCitizen(citizen);
    }

    // Get all complaints (for admin dashboard)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }
    // Get a single complaint by ID
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }
     // Update complaint status (admin/officer action)
    public Complaint updateStatus(Long complaintId, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found!"));
        complaint.setStatus(newStatus);
        return complaintRepository.save(complaint);
    }

    // Add a comment/remark to a complaint
    public Comment addComment(Long complaintId, String content, User author) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found!"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setComplaint(complaint);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    // Get all comments for a complaint
    public List<Comment> getCommentsByComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found!"));
        return commentRepository.findByComplaintOrderByCreatedAtDesc(complaint);
    }

    // Dashboard stats for admin
    public long getTotalComplaints() {
        return complaintRepository.count();
    }

    public long getPendingCount() {
        return complaintRepository.countByStatus(ComplaintStatus.PENDING);
    }

    public long getResolvedCount() {
        return complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
    }

    public long getInProgressCount() {
        return complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
    }

}
