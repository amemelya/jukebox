package com.jukebox.backend.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class RateSongRequest {

    @Min(value = 1, message = "Rating must be between 1 and 5.")
    @Max(value = 5, message = "Rating must be between 1 and 5.")
    private int stars;

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
