package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.ActivityResponse;
import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.dto.UserProfile;
import com.jukebox.backend.model.UserAccount;
import com.jukebox.backend.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureFlowServiceTest {

    @Test
    void listenListAndFriendActivityFlowWorks() {
        Map<String, UserAccount> users = new LinkedHashMap<String, UserAccount>();
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        when(userAccountRepository.count()).thenAnswer(invocation -> Long.valueOf(users.size()));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            users.put(user.getId(), user);
            return user;
        });
        when(userAccountRepository.saveAll(any(Iterable.class))).thenAnswer(invocation -> {
            Iterable<UserAccount> batch = invocation.getArgument(0);
            List<UserAccount> saved = new ArrayList<UserAccount>();
            for (UserAccount user : batch) {
                users.put(user.getId(), user);
                saved.add(user);
            }
            return saved;
        });
        when(userAccountRepository.findById(any(String.class))).thenAnswer(invocation -> {
            String userId = invocation.getArgument(0);
            return Optional.ofNullable(users.get(userId));
        });
        when(userAccountRepository.findAllByOrderByUsernameAsc()).thenAnswer(invocation -> {
            List<UserAccount> ordered = new ArrayList<UserAccount>(users.values());
            ordered.sort((left, right) -> left.getUsername().compareTo(right.getUsername()));
            return ordered;
        });

        UserService userService = new UserService(userAccountRepository);
        userService.seedDemoUsers();
        ActivityService activityService = new ActivityService();
        RatingService ratingService = new RatingService();
        CatalogService catalogService = new CatalogService(ratingService);
        ListenListService listenListService = new ListenListService(catalogService, activityService, userService);

        UserAccount currentUser = userService.getOrCreateUser(
                new UserProfile("google", "user-1", "Test Listener", "listener@example.com", null, null)
        );

        userService.follow(currentUser, "demo-mira");
        listenListService.addToListenList(currentUser, "song-1");

        List<SongResponse> listenList = listenListService.list(currentUser);
        List<ActivityResponse> friendActivity = activityService.listFriendActivity(currentUser);

        assertFalse(listenList.isEmpty());
        assertTrue(listenList.stream().anyMatch(song -> "song-1".equals(song.getId())));
        assertFalse(friendActivity.isEmpty());
        assertTrue(friendActivity.stream().anyMatch(entry -> "mira.wav".equals(entry.getUsername())));
    }

    @Test
    void jwtRoundTripProducesReadableUserProfile() {
        AppProperties properties = new AppProperties();
        properties.setJwtSecret("12345678901234567890123456789012");
        properties.setJwtTtlSeconds(3600L);
        JwtService jwtService = new JwtService(properties);

        UserProfile profile = new UserProfile(
                "google",
                "subject-123",
                "Listener Name",
                "listener@example.com",
                "https://img.example/avatar.jpg",
                null
        );

        String token = jwtService.issueToken(profile);
        UserProfile parsed = jwtService.parseToken(token);

        assertTrue("subject-123".equals(parsed.getSubject()));
        assertTrue("Listener Name".equals(parsed.getName()));
        assertTrue("listener@example.com".equals(parsed.getEmail()));
    }
}
