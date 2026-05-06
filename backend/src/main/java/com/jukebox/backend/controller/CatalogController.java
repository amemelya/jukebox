package com.jukebox.backend.controller;

import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.service.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/songs")
    public List<SongResponse> listSongs() {
        return catalogService.listActiveSongs();
    }
}
