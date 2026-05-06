package com.jukebox.backend.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class CreateSongRequest {

    @NotBlank(message = "Song title is required.")
    private String title;

    @NotBlank(message = "Artist is required.")
    private String artist;

    @NotBlank(message = "Album is required.")
    private String album;

    private String coverUrl;

    @Min(value = 1900, message = "Release year must be valid.")
    @Max(value = 2100, message = "Release year must be valid.")
    private int releaseYear;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }
}
