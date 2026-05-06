package com.jukebox.backend.controller;

import com.jukebox.backend.dto.ActivityResponse;
import com.jukebox.backend.dto.ApiResponse;
import com.jukebox.backend.dto.UserSummaryResponse;
import com.jukebox.backend.model.UserAccount;
import com.jukebox.backend.service.ActivityService;
import com.jukebox.backend.service.RequestUserService;
import com.jukebox.backend.service.UserService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RequestUserService requestUserService;
    private final UserService userService;
    private final ActivityService activityService;

    public UserController(RequestUserService requestUserService, UserService userService, ActivityService activityService) {
        this.requestUserService = requestUserService;
        this.userService = userService;
        this.activityService = activityService;
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse> followUser(@PathVariable String userId, HttpServletRequest request) {
        UserAccount currentUser = requestUserService.requireUser(request);
        userService.follow(currentUser, userId);
        return ResponseEntity.ok(new ApiResponse(true, "User followed successfully.", null, null, null));
    }

    @GetMapping("/me")
    public UserSummaryResponse currentUser(HttpServletRequest request) {
        UserAccount currentUser = requestUserService.requireUser(request);
        return userService.toSummary(currentUser);
    }

    @GetMapping
    public List<UserSummaryResponse> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/me/activity")
    public List<ActivityResponse> friendActivity(HttpServletRequest request) {
        UserAccount currentUser = requestUserService.requireUser(request);
        return activityService.listFriendActivity(currentUser);
    }
}
