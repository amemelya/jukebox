package com.jukebox.backend.controller;

import com.jukebox.backend.dto.ApiResponse;
import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.model.UserAccount;
import com.jukebox.backend.service.ListenListService;
import com.jukebox.backend.service.RequestUserService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listen-list")
public class ListenListController {

    private final RequestUserService requestUserService;
    private final ListenListService listenListService;

    public ListenListController(RequestUserService requestUserService, ListenListService listenListService) {
        this.requestUserService = requestUserService;
        this.listenListService = listenListService;
    }

    @PostMapping("/{songId}")
    public ResponseEntity<ApiResponse> addSong(@PathVariable String songId, HttpServletRequest request) {
        UserAccount user = requestUserService.requireUser(request);
        listenListService.addToListenList(user, songId);
        return ResponseEntity.ok(new ApiResponse(true, "Song added to listen list.", null, null, null));
    }

    @GetMapping
    public List<SongResponse> getListenList(HttpServletRequest request) {
        UserAccount user = requestUserService.requireUser(request);
        return listenListService.list(user);
    }
}
