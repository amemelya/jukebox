package com.jukebox.backend.controller;

import com.jukebox.backend.dto.ApiResponse;
import com.jukebox.backend.dto.RateSongRequest;
import com.jukebox.backend.model.SongItem;
import com.jukebox.backend.model.UserAccount;
import com.jukebox.backend.service.ActivityService;
import com.jukebox.backend.service.CatalogService;
import com.jukebox.backend.service.RatingService;
import com.jukebox.backend.service.RequestUserService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final RequestUserService requestUserService;
    private final CatalogService catalogService;
    private final RatingService ratingService;
    private final ActivityService activityService;

    public SongController(
            RequestUserService requestUserService,
            CatalogService catalogService,
            RatingService ratingService,
            ActivityService activityService
    ) {
        this.requestUserService = requestUserService;
        this.catalogService = catalogService;
        this.ratingService = ratingService;
        this.activityService = activityService;
    }

    @PostMapping("/{songId}/ratings")
    public ResponseEntity<ApiResponse> rateSong(
            @PathVariable String songId,
            @Valid @RequestBody RateSongRequest request,
            HttpServletRequest servletRequest
    ) {
        UserAccount user = requestUserService.requireUser(servletRequest);
        SongItem song = catalogService.getRequiredSong(songId);
        ratingService.rateSong(user.getId(), songId, request.getStars());
        activityService.record(user, "rated_song_" + request.getStars() + "_stars", songId, song.getTitle());
        return ResponseEntity.ok(new ApiResponse(true, "Song rating saved.", null, null, null));
    }
}
