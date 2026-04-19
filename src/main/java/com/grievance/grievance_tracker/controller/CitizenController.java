package com.grievance.grievance_tracker.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.service.ComplaintService;
import com.grievance.grievance_tracker.service.UserService;

@Controller
@RequestMapping("/citizen")
public class CitizenController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    // Helper method to get logged-in user
    private User getLoggedInUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Citizen Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User citizen = getLoggedInUser(auth);
        List<Complaint> complaints = complaintService.getComplaintsByCitizen(citizen);

        // Stats for dashboard cards
        long total = complaints.size();
        long pending = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.PENDING).count();
        long inProgress = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();

        model.addAttribute("citizen", citizen);
        model.addAttribute("complaints", complaints);
        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("resolved", resolved);

        return "citizen/dashboard";
    }

    // Show complaint submission form
    @GetMapping("/submit-complaint")
    public String submitComplaintPage(Model model) {
        model.addAttribute("complaint", new Complaint());
        return "citizen/submit-complaint";
    }

    // Handle complaint form submission
    @PostMapping("/submit-complaint")
    public String submitComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam(value = "imageFile", required = false)
                MultipartFile imageFile,
            Authentication auth,
            Model model) {

        try {
            User citizen = getLoggedInUser(auth);
            Complaint complaint = new Complaint();
            complaint.setTitle(title);
            complaint.setDescription(description);
            complaint.setCategory(category);
            complaint.setLocation(location);

            complaintService.submitComplaint(complaint, citizen, imageFile);
            return "redirect:/citizen/dashboard?success=true";

        } catch (IOException e) {
            model.addAttribute("errorMessage", "File upload failed: " + e.getMessage());
            return "citizen/submit-complaint";
        }
    }

    // View a single complaint detail
    @GetMapping("/complaint/{id}")
    public String viewComplaint(@PathVariable Long id,
                                 Authentication auth,
                                 Model model) {
        User citizen = getLoggedInUser(auth);

        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Security check — citizen can only see their own complaints
        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            return "redirect:/citizen/dashboard";
        }

        List<Comment> comments = complaintService.getCommentsByComplaint(id);
        model.addAttribute("complaint", complaint);
        model.addAttribute("comments", comments);
        return "citizen/complaint-detail";
    }
}