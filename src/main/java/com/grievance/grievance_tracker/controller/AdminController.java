package com.grievance.grievance_tracker.controller;

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

import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.service.ComplaintService;
import com.grievance.grievance_tracker.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User admin = getLoggedInUser(auth);
        List<Complaint> complaints = complaintService.getAllComplaints();

        model.addAttribute("admin", admin);
        model.addAttribute("complaints", complaints);
        model.addAttribute("total", complaintService.getTotalComplaints());
        model.addAttribute("pending", complaintService.getPendingCount());
        model.addAttribute("inProgress", complaintService.getInProgressCount());
        model.addAttribute("resolved", complaintService.getResolvedCount());

        return "admin/dashboard";
    }

    // View single complaint + add remarks
    @GetMapping("/complaint/{id}")
    public String viewComplaint(@PathVariable Long id,
                                Authentication auth,
                                Model model) {
        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        List<Comment> comments = complaintService.getCommentsByComplaint(id);

        model.addAttribute("complaint", complaint);
        model.addAttribute("comments", comments);
        model.addAttribute("statuses", ComplaintStatus.values());

        return "admin/complaint-detail";
    }

    // Update complaint status
    @PostMapping("/complaint/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") String status) {
        ComplaintStatus newStatus = ComplaintStatus.valueOf(status);
        complaintService.updateStatus(id, newStatus);
        return "redirect:/admin/complaint/" + id + "?updated=true";
    }

    // Add remark/comment
    @PostMapping("/complaint/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam("content") String content,
                             Authentication auth) {
        User admin = getLoggedInUser(auth);
        complaintService.addComment(id, content, admin);
        return "redirect:/admin/complaint/" + id + "?commented=true";
    }

    // View all users
    @GetMapping("/users")
    public String viewUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
}