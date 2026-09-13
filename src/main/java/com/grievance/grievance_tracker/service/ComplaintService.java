package com.grievance.grievance_tracker.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.StatusHistory;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.repository.CommentRepository;
import com.grievance.grievance_tracker.repository.ComplaintRepository;
import com.grievance.grievance_tracker.repository.StatusHistoryRepository;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

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

        // Render's free-tier filesystem is ephemeral; production deployments should
        // point app.upload.dir to persistent storage or use object storage such as S3/R2.
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

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

        return "uploads/complaints/" + fileName;
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

public Page<Complaint> searchComplaints(String search, String statusFilter, int page) {
    String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
    ComplaintStatus status = statusFilter == null || statusFilter.isBlank()
            ? null
            : ComplaintStatus.valueOf(statusFilter.trim());
    Pageable pageable = PageRequest.of(page, 10);
    return complaintRepository.searchComplaints(normalizedSearch, status, pageable);
}
    // Get a single complaint by ID
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }
     // Update complaint status (admin/officer action)
    @Transactional
    public Complaint updateStatus(Long complaintId, ComplaintStatus newStatus, User changedBy) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found!"));

        ComplaintStatus currentStatus = complaint.getStatus();
        boolean isAllowedTransition =
                (currentStatus == ComplaintStatus.PENDING
                        && (newStatus == ComplaintStatus.IN_PROGRESS
                                || newStatus == ComplaintStatus.REJECTED))
                || (currentStatus == ComplaintStatus.IN_PROGRESS
                        && (newStatus == ComplaintStatus.RESOLVED
                                || newStatus == ComplaintStatus.REJECTED
                                || newStatus == ComplaintStatus.PENDING));

        if (!isAllowedTransition) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        complaint.setStatus(newStatus);
        Complaint savedComplaint = complaintRepository.save(complaint);
        saveStatusHistory(savedComplaint, changedBy, currentStatus, newStatus, null);
        return savedComplaint;
    }

    @Transactional
    public void cancelComplaint(Long complaintId, User citizen) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found!"));

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new SecurityException("Not authorized");
        }
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalArgumentException("Only pending complaints can be cancelled");
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.REJECTED);
        Complaint savedComplaint = complaintRepository.save(complaint);
        saveStatusHistory(
                savedComplaint,
                citizen,
                oldStatus,
                ComplaintStatus.REJECTED,
                "Cancelled by citizen");
    }

    public List<StatusHistory> getStatusHistory(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found!"));
        return statusHistoryRepository.findByComplaintOrderByChangedAtDesc(complaint);
    }

    private void saveStatusHistory(
            Complaint complaint,
            User changedBy,
            ComplaintStatus oldStatus,
            ComplaintStatus newStatus,
            String remarks) {
        StatusHistory history = new StatusHistory();
        history.setComplaint(complaint);
        history.setChangedBy(changedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        statusHistoryRepository.save(history);
    }

    // Add a comment/remark to a complaint
    public Comment addComment(Long complaintId, String content, User author) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found!"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setComplaint(complaint);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    // Get all comments for a complaint
    public List<Comment> getCommentsByComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found!"));
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
