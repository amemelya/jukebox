package com.jukebox.backend.controller;

import com.jukebox.backend.dto.CreateSongRequest;
import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.service.AdminAccessService;
import com.jukebox.backend.service.CatalogService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final AdminAccessService adminAccessService;
    private final CatalogService catalogService;

    public AdminInventoryController(AdminAccessService adminAccessService, CatalogService catalogService) {
        this.adminAccessService = adminAccessService;
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<SongResponse> listInventory(HttpServletRequest request) {
        adminAccessService.requireAdmin(request);
        return catalogService.listInventory();
    }

    @PostMapping
    public SongResponse createSong(@Valid @RequestBody CreateSongRequest request, HttpServletRequest servletRequest) {
        adminAccessService.requireAdmin(servletRequest);
        return catalogService.createSong(request);
    }

    @PutMapping("/{songId}")
    public SongResponse updateSong(
            @PathVariable String songId,
            @Valid @RequestBody CreateSongRequest request,
            HttpServletRequest servletRequest
    ) {
        adminAccessService.requireAdmin(servletRequest);
        return catalogService.updateSong(songId, request);
    }

    @DeleteMapping("/{songId}")
    public void deleteSong(@PathVariable String songId, HttpServletRequest request) {
        adminAccessService.requireAdmin(request);
        catalogService.deactivateSong(songId);
    }
}
