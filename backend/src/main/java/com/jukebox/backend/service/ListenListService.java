package com.jukebox.backend.service;

import com.jukebox.backend.dto.SongResponse;
import com.jukebox.backend.model.SongItem;
import com.jukebox.backend.model.UserAccount;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ListenListService {

    private final CatalogService catalogService;
    private final ActivityService activityService;
    private final UserService userService;

    public ListenListService(CatalogService catalogService, ActivityService activityService, UserService userService) {
        this.catalogService = catalogService;
        this.activityService = activityService;
        this.userService = userService;
    }

    @Transactional
    public void addToListenList(UserAccount user, String songId) {
        UserAccount storedUser = userService.getRequiredUser(user.getId());
        SongItem song = catalogService.getRequiredSong(songId);
        storedUser.getListenList().add(songId);
        userService.save(storedUser);
        activityService.record(storedUser, "added_to_listen_list", songId, song.getTitle());
    }

    public List<SongResponse> list(UserAccount user) {
        UserAccount storedUser = userService.getRequiredUser(user.getId());
        List<SongResponse> response = new ArrayList<SongResponse>();
        for (String songId : storedUser.getListenList()) {
            response.add(catalogService.toResponse(catalogService.getRequiredSong(songId)));
        }
        return response;
    }
}
