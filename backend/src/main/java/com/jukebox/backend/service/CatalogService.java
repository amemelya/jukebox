package com.jukebox.backend.service;

import com.jukebox.backend.dto.CreateSongRequest;
import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.model.SongItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final Map<String, SongItem> songs = new ConcurrentHashMap<String, SongItem>();
    private final RatingService ratingService;

    public CatalogService(RatingService ratingService) {
        this.ratingService = ratingService;
        seedSongs();
    }

    public List<SongResponse> listActiveSongs() {
        List<SongItem> items = new ArrayList<SongItem>(songs.values());
        items.sort(Comparator.comparing(SongItem::getArtist).thenComparing(SongItem::getTitle));

        List<SongResponse> response = new ArrayList<SongResponse>();
        for (SongItem item : items) {
            if (item.isActive()) {
                response.add(toResponse(item));
            }
        }
        return response;
    }

    public List<SongResponse> listInventory() {
        List<SongItem> items = new ArrayList<SongItem>(songs.values());
        items.sort(Comparator.comparing(SongItem::getArtist).thenComparing(SongItem::getTitle));

        List<SongResponse> response = new ArrayList<SongResponse>();
        for (SongItem item : items) {
            response.add(toResponse(item));
        }
        return response;
    }

    public SongResponse createSong(CreateSongRequest request) {
        String id = UUID.randomUUID().toString();
        SongItem item = new SongItem(
                id,
                request.getTitle().trim(),
                request.getArtist().trim(),
                request.getAlbum().trim(),
                request.getCoverUrl(),
                request.getReleaseYear(),
                true
        );
        songs.put(id, item);
        return toResponse(item);
    }

    public SongResponse updateSong(String songId, CreateSongRequest request) {
        SongItem item = getRequiredSong(songId);
        item.setTitle(request.getTitle().trim());
        item.setArtist(request.getArtist().trim());
        item.setAlbum(request.getAlbum().trim());
        item.setCoverUrl(request.getCoverUrl());
        item.setReleaseYear(request.getReleaseYear());
        item.setActive(true);
        return toResponse(item);
    }

    public void deactivateSong(String songId) {
        getRequiredSong(songId).setActive(false);
    }

    public SongItem getRequiredSong(String songId) {
        SongItem item = songs.get(songId);
        if (item == null) {
            throw new IllegalArgumentException("Song not found.");
        }
        return item;
    }

    public SongResponse toResponse(SongItem item) {
        return new SongResponse(
                item.getId(),
                item.getTitle(),
                item.getArtist(),
                item.getAlbum(),
                item.getCoverUrl(),
                item.getReleaseYear(),
                item.isActive(),
                ratingService.getAverageRating(item.getId()),
                ratingService.getRatingCount(item.getId())
        );
    }

    private void seedSongs() {
        songs.put("song-1", new SongItem("song-1", "Neon Afterglow", "Satin Avenue", "Neon Afterglow", null, 2026, true));
        songs.put("song-2", new SongItem("song-2", "Static Season", "North Harbour", "Monsoon Archive", null, 2025, true));
        songs.put("song-3", new SongItem("song-3", "Stay Until Static", "June Static", "Soft Exit", null, 2026, true));
        songs.put("song-4", new SongItem("song-4", "Blue Exit Sign", "Alina Vale", "Blue Exit Sign", null, 2024, true));
        songs.put("song-5", new SongItem("song-5", "Glass Receiver", "Mira Lane", "Glass Receiver", null, 2024, true));
    }
}
