package com.grievance.grievance_tracker.controller;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.grievance.grievance_tracker.dto.ComplaintRequest;
import com.grievance.grievance_tracker.model.Comment;
import com.grievance.grievance_tracker.model.Complaint;
import com.grievance.grievance_tracker.model.ComplaintStatus;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.service.ComplaintService;
import com.grievance.grievance_tracker.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/citizen")
public class CitizenController {

    private static final Logger log = LoggerFactory.getLogger(CitizenController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

@GetMapping("/dashboard")
public String dashboard(
        @RequestParam(defaultValue = "0") int page,
        Authentication auth,
        Model model) {
    User citizen = getLoggedInUser(auth);
    Page<Complaint> complaintPage = complaintService.getComplaintsByCitizen(citizen, page);

    model.addAttribute("citizen", citizen);
    model.addAttribute("complaints", complaintPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", complaintPage.getTotalPages());
    model.addAttribute("hasNext", complaintPage.hasNext());
    model.addAttribute("hasPrevious", complaintPage.hasPrevious());
    model.addAttribute("total", complaintService.getCitizenComplaintCount(citizen));
    model.addAttribute("pending", complaintService.getCitizenStatusCount(citizen, ComplaintStatus.PENDING));
    model.addAttribute("inProgress", complaintService.getCitizenStatusCount(citizen, ComplaintStatus.IN_PROGRESS));
    model.addAttribute("resolved", complaintService.getCitizenStatusCount(citizen, ComplaintStatus.RESOLVED));

    return "citizen/dashboard";
}

    @GetMapping("/submit-complaint")
    public String submitComplaintPage(Model model) {
        model.addAttribute("complaintRequest", new ComplaintRequest());
        return "citizen/submit-complaint";
    }

    @PostMapping("/submit-complaint")
    public String submitComplaint(
            @Valid @ModelAttribute("complaintRequest") ComplaintRequest complaintRequest,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication auth,
            Model model,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .get(0).getDefaultMessage();
            model.addAttribute("errorMessage", errorMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "citizen/submit-complaint";
        }

        try {
            User citizen = getLoggedInUser(auth);
            Complaint complaint = new Complaint();
            complaint.setTitle(complaintRequest.getTitle());
            complaint.setDescription(complaintRequest.getDescription());
            complaint.setCategory(complaintRequest.getCategory());
            complaint.setLocation(complaintRequest.getLocation());

            complaintService.submitComplaint(complaint, citizen, imageFile);
            log.info("Complaint submitted successfully.");
            return "redirect:/citizen/dashboard?success=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "citizen/submit-complaint";

        } catch (IOException e) {
            model.addAttribute("errorMessage", "File upload failed. Please try again.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "citizen/submit-complaint";
        }
    }

    @GetMapping("/complaint/{id}")
    public String viewComplaint(@PathVariable Long id,
                                Authentication auth,
                                Model model) {
        User citizen = getLoggedInUser(auth);

        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found"));

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            return "redirect:/citizen/dashboard";
        }

        List<Comment> comments = complaintService.getCommentsByComplaint(id);
        model.addAttribute("complaint", complaint);
        model.addAttribute("comments", comments);
        return "citizen/complaint-detail";
    }
}
