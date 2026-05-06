package com.jukebox.backend.service;

import com.jukebox.backend.dto.UserProfile;
import com.jukebox.backend.dto.UserSummaryResponse;
import com.jukebox.backend.model.UserAccount;
import com.jukebox.backend.repository.UserAccountRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;

    public UserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @PostConstruct
    public void seedDemoUsers() {
        if (userAccountRepository.count() > 0L) {
            return;
        }

        userAccountRepository.saveAll(Arrays.asList(
                new UserAccount(
                        "demo-mira",
                        "seed",
                        "Mira Lane",
                        "mira.wav",
                        "mira@jukebox.local",
                        null,
                        null
                ),
                new UserAccount(
                        "demo-arjun",
                        "seed",
                        "Arjun Loops",
                        "arjunloops",
                        "arjun@jukebox.local",
                        null,
                        null
                ),
                new UserAccount(
                        "demo-nina",
                        "seed",
                        "Nina Afterdark",
                        "ninaafterdark",
                        "nina@jukebox.local",
                        null,
                        null
                )
        ));
    }

    @Transactional
    public UserAccount getOrCreateUser(UserProfile profile) {
        Optional<UserAccount> existing = userAccountRepository.findById(profile.getSubject());
        if (existing.isPresent()) {
            UserAccount current = existing.get();
            synchronize(current, profile);
            return userAccountRepository.save(current);
        }

        UserAccount created = new UserAccount(
                profile.getSubject(),
                profile.getProvider(),
                profile.getName(),
                resolveUsername(profile),
                profile.getEmail(),
                profile.getPictureUrl(),
                profile.getPhoneNumber()
        );
        return userAccountRepository.save(created);
    }

    public UserAccount getRequiredUser(String userId) {
        UserAccount user = userAccountRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }

    @Transactional
    public void follow(UserAccount follower, String targetUserId) {
        UserAccount storedFollower = getRequiredUser(follower.getId());
        UserAccount target = getRequiredUser(targetUserId);
        if (storedFollower.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Users cannot follow themselves.");
        }
        storedFollower.getFollowing().add(targetUserId);
        target.getFollowers().add(storedFollower.getId());
        userAccountRepository.save(target);
        userAccountRepository.save(storedFollower);
    }

    public List<UserSummaryResponse> listUsers() {
        List<UserAccount> values = userAccountRepository.findAllByOrderByUsernameAsc();
        java.util.ArrayList<UserSummaryResponse> response = new java.util.ArrayList<UserSummaryResponse>();
        for (UserAccount user : values) {
            response.add(toSummary(user));
        }
        return response;
    }

    @Transactional
    public UserAccount save(UserAccount user) {
        return userAccountRepository.save(user);
    }

    public UserSummaryResponse toSummary(UserAccount user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPictureUrl(),
                user.getFollowers().size(),
                user.getFollowing().size(),
                user.getListenList().size()
        );
    }

    private void synchronize(UserAccount existing, UserProfile profile) {
        existing.setDisplayName(profile.getName());
        existing.setEmail(profile.getEmail());
        existing.setPictureUrl(profile.getPictureUrl());
        existing.setPhoneNumber(profile.getPhoneNumber());
        if (existing.getUsername() == null || existing.getUsername().trim().isEmpty()) {
            existing.setUsername(resolveUsername(profile));
        }
    }

    private String resolveUsername(UserProfile profile) {
        if (profile.getEmail() != null && profile.getEmail().contains("@")) {
            return profile.getEmail().substring(0, profile.getEmail().indexOf('@'));
        }
        if (profile.getPhoneNumber() != null && profile.getPhoneNumber().length() >= 4) {
            return "user" + profile.getPhoneNumber().substring(profile.getPhoneNumber().length() - 4);
        }
        return profile.getSubject();
    }
}
