package com.jukebox.backend.repository;

import com.jukebox.backend.model.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    List<UserAccount> findAllByOrderByUsernameAsc();
}
