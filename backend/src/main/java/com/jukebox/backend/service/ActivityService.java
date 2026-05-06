package com.jukebox.backend.service;

import com.jukebox.backend.dto.ActivityResponse;
import com.jukebox.backend.model.ActivityEntry;
import com.jukebox.backend.model.UserAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final List<ActivityEntry> entries = new CopyOnWriteArrayList<ActivityEntry>();

    public ActivityService() {
        seedActivity();
    }

    public void record(UserAccount user, String action, String targetId, String targetTitle) {
        entries.add(new ActivityEntry(
                user.getId(),
                user.getUsername(),
                action,
                targetId,
                targetTitle,
                System.currentTimeMillis()
        ));
    }

    public List<ActivityResponse> listFriendActivity(UserAccount user) {
        List<ActivityResponse> response = new ArrayList<ActivityResponse>();
        for (ActivityEntry entry : entries) {
            if (user.getFollowing().contains(entry.getUserId())) {
                response.add(new ActivityResponse(
                        entry.getUsername(),
                        entry.getAction(),
                        entry.getTargetId(),
                        entry.getTargetTitle(),
                        entry.getCreatedAt()
                ));
            }
        }
        Collections.sort(response, Comparator.comparingLong(ActivityResponse::getCreatedAt).reversed());
        return response;
    }

    private void seedActivity() {
        long now = System.currentTimeMillis();
        entries.add(new ActivityEntry("demo-mira", "mira.wav", "rated_song_5_stars", "song-5", "Glass Receiver", now - 1000L));
        entries.add(new ActivityEntry("demo-arjun", "arjunloops", "added_to_listen_list", "song-2", "Static Season", now - 2000L));
        entries.add(new ActivityEntry("demo-nina", "ninaafterdark", "rated_song_4_stars", "song-3", "Stay Until Static", now - 3000L));
    }
}
