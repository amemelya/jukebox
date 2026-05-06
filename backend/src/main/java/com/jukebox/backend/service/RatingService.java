package com.jukebox.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final Map<String, Map<String, Integer>> ratingsBySong = new ConcurrentHashMap<String, Map<String, Integer>>();

    public void rateSong(String userId, String songId, int stars) {
        Map<String, Integer> ratings = ratingsBySong.computeIfAbsent(songId, key -> new ConcurrentHashMap<String, Integer>());
        ratings.put(userId, Integer.valueOf(stars));
    }

    public double getAverageRating(String songId) {
        Map<String, Integer> ratings = ratingsBySong.get(songId);
        if (ratings == null || ratings.isEmpty()) {
            return 0.0d;
        }

        int total = 0;
        for (Integer value : ratings.values()) {
            total += value.intValue();
        }

        BigDecimal average = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
        return average.doubleValue();
    }

    public int getRatingCount(String songId) {
        Map<String, Integer> ratings = ratingsBySong.get(songId);
        return ratings == null ? 0 : ratings.size();
    }
}
