package com.jukebox.backend.dto;

public class SongResponse {

    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private final String coverUrl;
    private final int releaseYear;
    private final boolean active;
    private final double averageRating;
    private final int ratingCount;

    public SongResponse(
            String id,
            String title,
            String artist,
            String album,
            String coverUrl,
            int releaseYear,
            boolean active,
            double averageRating,
            int ratingCount
    ) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.releaseYear = releaseYear;
        this.active = active;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public boolean isActive() {
        return active;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }
}
