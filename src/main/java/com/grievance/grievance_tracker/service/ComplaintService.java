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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        String originalName = Paths.get(imageFile.getOriginalFilename()).getFileName().toString();
        String extension = originalName.contains(".")
        ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
        : "";

        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        String fileName = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadPath.normalize())) {
            throw new SecurityException("Invalid file path detected.");
        }

        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + fileName;
    }

// Get paginated complaints by citizen
public Page<Complaint> getComplaintsByCitizen(User citizen, int page) {
    Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
    return complaintRepository.findByCitizen(citizen, pageable);
}

// Get paginated all complaints for admin
public Page<Complaint> getAllComplaints(int page) {
    Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
    return complaintRepository.findAll(pageable);
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
    // Count all complaints for a citizen
public long getCitizenComplaintCount(User citizen) {
    return complaintRepository.countByCitizen(citizen);
}

// Count complaints for a citizen by status
public long getCitizenStatusCount(User citizen, ComplaintStatus status) {
    return complaintRepository.countByCitizenAndStatus(citizen, status);
}

}
