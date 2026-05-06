package com.jukebox.backend.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_listen_list", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "song_id", nullable = false)
    private Set<String> listenList = new LinkedHashSet<String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_following", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "target_user_id", nullable = false)
    private Set<String> following = new LinkedHashSet<String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_followers", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "follower_user_id", nullable = false)
    private Set<String> followers = new LinkedHashSet<String>();

    protected UserAccount() {
    }

    public UserAccount(String id, String provider, String displayName, String username, String email, String pictureUrl, String phoneNumber) {
        this.id = id;
        this.provider = provider;
        this.displayName = displayName;
        this.username = username;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<String> getListenList() {
        return listenList;
    }

    public Set<String> getFollowing() {
        return following;
    }

    public Set<String> getFollowers() {
        return followers;
    }
}
