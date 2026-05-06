import { useEffect, useRef, useState } from "react";

const phonePattern = /^\d{10}$/;
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://127.0.0.1:4000/api/auth";
const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";
const sessionStorageKey = "jukebox-auth-session";
const defaultBio =
  "Albums for late trains, songs with sharp bridges, and reviews written before the replay button cools down.";

const featurePoints = [
  "Queue sessions with friends in seconds",
  "Pick up on any screen without losing your place",
  "Secure sign-in for listeners and hosts"
];

const friendLogs = [
  { type: "Album", title: "Glass Receiver", artist: "Mira Lane", friend: "mira.wav", rating: "4.0", date: "May 05", accent: "rose" },
  { type: "Song", title: "Static Season", artist: "North Harbour", friend: "arjunloops", rating: "4.5", date: "May 04", accent: "gold" },
  { type: "Album", title: "Monsoon Archive", artist: "North Harbour", friend: "ninaafterdark", rating: "3.5", date: "May 03", accent: "mist" },
  { type: "Song", title: "Stay Until Static", artist: "June Static", friend: "softnoiseclub", rating: "5.0", date: "May 03", accent: "ember" },
  { type: "Album", title: "Chrome Hearts Motel", artist: "Color Motel", friend: "pujarini", rating: "4.0", date: "May 02", accent: "cobalt" },
  { type: "Song", title: "Blue Exit Sign", artist: "Alina Vale", friend: "shouri", rating: "4.5", date: "May 01", accent: "night" }
];

const profileMenuItems = ["Profile", "Reviews", "ListenList", "Settings", "Logout"];
const topAlbums = [
  { title: "Neon Afterglow", artist: "Satin Avenue", accent: "rose" },
  { title: "Glass Receiver", artist: "Mira Lane", accent: "gold" },
  { title: "Monsoon Archive", artist: "North Harbour", accent: "mist" },
  { title: "Soft Exit", artist: "June Static", accent: "ember" },
  { title: "Chrome Hearts Motel", artist: "Color Motel", accent: "cobalt" }
];
const recentReviews = [
  {
    title: "Neon Afterglow",
    artist: "Satin Avenue",
    rating: "4.5",
    note: "The hooks are immediate, but the real payoff is how the last three tracks bleed into each other.",
    date: "May 05"
  },
  {
    title: "Static Season",
    artist: "North Harbour",
    rating: "4.0",
    note: "A single built for rainy commutes and repeat listens. The chorus lands harder every time.",
    date: "May 04"
  },
  {
    title: "Soft Exit",
    artist: "June Static",
    rating: "5.0",
    note: "No wasted motion anywhere. Every verse feels like it was sharpened down to the cleanest possible line.",
    date: "May 03"
  },
  {
    title: "Chrome Hearts Motel",
    artist: "Color Motel",
    rating: "3.5",
    note: "More mood than punch, but the sequencing keeps the whole record moving beautifully.",
    date: "May 02"
  },
  {
    title: "Blue Exit Sign",
    artist: "Alina Vale",
    rating: "4.0",
    note: "Short, precise, and devastating in the bridge. Exactly the kind of song you restart before it ends.",
    date: "May 01"
  }
];
const profileStats = [
  { value: "987", label: "Songs listened" },
  { value: "143", label: "This year" },
  { value: "18", label: "Playlists" },
  { value: "36", label: "Following" },
  { value: "61", label: "Followers" }
];

function resolveHandle(user) {
  if (user?.email) {
    return user.email.split("@")[0];
  }

  if (user?.phoneNumber) {
    return user.phoneNumber.slice(-10);
  }

  return user?.subject || "listener";
}

export default function App() {
  const googleButtonRef = useRef(null);
  const profileMenuRef = useRef(null);
  const [mode, setMode] = useState("mobile");
  const [phone, setPhone] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState("home");
  const [authSession, setAuthSession] = useState(() => {
    const stored = window.sessionStorage.getItem(sessionStorageKey);
    return stored ? JSON.parse(stored) : null;
  });
  const [message, setMessage] = useState("Use your Google account or request a one-time code.");
  const [profileDraft, setProfileDraft] = useState(() => {
    const stored = window.sessionStorage.getItem(sessionStorageKey);
    const session = stored ? JSON.parse(stored) : null;

    return {
      username: session?.user ? resolveHandle(session.user) : "",
      pictureUrl: session?.user?.pictureUrl || "",
      bio: defaultBio
    };
  });

  const phoneLooksValid = phonePattern.test(phone);
  const otpLooksValid = otp.trim().length === 6;

  useEffect(() => {
    if (mode !== "google" || !googleButtonRef.current || !googleClientId || authSession) {
      return;
    }

    function renderGoogleButton() {
      if (!window.google || !googleButtonRef.current) {
        return;
      }

      googleButtonRef.current.innerHTML = "";
      window.google.accounts.id.initialize({
        client_id: googleClientId,
        callback: handleGoogleCredentialResponse
      });
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: "outline",
        size: "large",
        shape: "pill",
        text: "continue_with",
        width: 320
      });
    }

    if (window.google) {
      renderGoogleButton();
      return;
    }

    const existingScript = document.querySelector('script[src="https://accounts.google.com/gsi/client"]');

    if (existingScript) {
      existingScript.addEventListener("load", renderGoogleButton, { once: true });
      return () => existingScript.removeEventListener("load", renderGoogleButton);
    }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.addEventListener("load", renderGoogleButton, { once: true });
    document.head.appendChild(script);

    return () => script.removeEventListener("load", renderGoogleButton);
  }, [authSession, mode]);

  useEffect(() => {
    if (!isProfileMenuOpen) {
      return;
    }

    function handlePointerDown(event) {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target)) {
        setIsProfileMenuOpen(false);
      }
    }

    function handleEscape(event) {
      if (event.key === "Escape") {
        setIsProfileMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [isProfileMenuOpen]);

  async function handleSendOtp(event) {
    event.preventDefault();

    if (!phoneLooksValid) {
      setMessage("Enter a valid 10-digit mobile number before requesting a code.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch(`${apiBaseUrl}/send-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phone })
      });
      const data = await response.json();

      if (!response.ok) {
        setMessage(data.message || "Could not send OTP.");
        return;
      }

      setOtpSent(true);
      setOtp("");
      setMessage(data.devOtp ? `OTP sent to +91 ${phone}. Dev code: ${data.devOtp}` : `OTP sent to +91 ${phone}.`);
    } catch {
      setMessage("Backend is unreachable. Start the backend server and try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleVerifyOtp(event) {
    event.preventDefault();

    if (!otpLooksValid) {
      setMessage("Enter the 6-digit code sent to your phone.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch(`${apiBaseUrl}/verify-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phone, otp })
      });
      const data = await response.json();

      if (!response.ok) {
        setMessage(data.message || "Verification failed.");
        return;
      }

      persistSession(data);
      setMessage(data.message || "Verification completed.");
    } catch {
      setMessage("Backend is unreachable. Start the backend server and try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleGoogleCredentialResponse(response) {
    setIsSubmitting(true);

    try {
      const apiResponse = await fetch(`${apiBaseUrl}/google`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ credential: response.credential })
      });
      const data = await apiResponse.json();

      if (!apiResponse.ok) {
        setMessage(data.message || "Google sign-in failed.");
        return;
      }

      persistSession(data);
      setMessage(data.message || "Google sign-in complete.");
    } catch {
      setMessage("Backend is unreachable. Start the backend server and try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  function persistSession(data) {
    if (!data.token || !data.user) {
      return;
    }

    const session = { token: data.token, user: data.user };
    window.sessionStorage.setItem(sessionStorageKey, JSON.stringify(session));
    setAuthSession(session);
    setCurrentPage("home");
    setProfileDraft({
      username: resolveHandle(session.user),
      pictureUrl: session.user.pictureUrl || "",
      bio: defaultBio
    });
  }

  function handleSignOut() {
    window.sessionStorage.removeItem(sessionStorageKey);
    setAuthSession(null);
    setIsProfileMenuOpen(false);
    setCurrentPage("home");
    setOtp("");
    setOtpSent(false);
    setPhone("");
    setMessage("Signed out. Use your Google account or request a one-time code.");

    if (window.google) {
      window.google.accounts.id.disableAutoSelect();
    }
  }

  function handleProfileMenuClick(item) {
    setIsProfileMenuOpen(false);

    if (item === "Logout") {
      handleSignOut();
      return;
    }

    if (item === "Profile") {
      setCurrentPage("profile");
      return;
    }

    setCurrentPage("home");
  }

  if (authSession) {
    const displayName =
      authSession.user.name || authSession.user.email || authSession.user.phoneNumber || "Listener";
    const handle = profileDraft.username || resolveHandle(authSession.user);
    const profileImage = profileDraft.pictureUrl || authSession.user.pictureUrl || "";
    const profileBio = profileDraft.bio || defaultBio;
    const avatarLabel = handle.trim().charAt(0).toUpperCase() || "J";

    function renderSignedInPage() {
      if (currentPage === "profile") {
        return (
          <section className="profile-page">
            <div className="profile-card">
              <div className="profile-hero">
                {profileImage ? (
                  <img
                    className="profile-photo"
                    src={profileImage}
                    alt={handle}
                    referrerPolicy="no-referrer"
                  />
                ) : (
                  <div className="profile-photo profile-photo-fallback">{avatarLabel}</div>
                )}
                <div className="profile-copy">
                  <div className="profile-headline-row">
                    <div className="profile-main">
                      <div className="profile-title-row">
                        <h1>{handle}</h1>
                        <button
                          type="button"
                          className="secondary-action inline-edit-button"
                          onClick={() => setCurrentPage("editProfile")}
                        >
                          Edit profile
                        </button>
                      </div>
                      <p className="profile-display-name">{displayName}</p>
                      <p className="profile-bio">{profileBio}</p>
                    </div>
                    <div className="profile-stats-row">
                      {profileStats.map((stat) => (
                        <div key={stat.label} className="profile-stat">
                          <strong>{stat.value}</strong>
                          <span>{stat.label}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="top-albums-section">
                    <div className="top-albums-header">
                      <h2>Top 5 albums</h2>
                      <span>Current all-time favorites</span>
                    </div>
                    <div className="top-albums-grid">
                      {topAlbums.map((album) => (
                        <article key={album.title} className={`top-album-card accent-${album.accent}`}>
                          <div className="top-album-cover">
                            <span>Album</span>
                          </div>
                          <div className="top-album-copy">
                            <strong>{album.title}</strong>
                            <p>{album.artist}</p>
                          </div>
                        </article>
                      ))}
                    </div>
                  </div>
                  <div className="recent-reviews-section">
                    <div className="top-albums-header">
                      <h2>Recent reviews</h2>
                      <span>Five latest notes from the listening diary</span>
                    </div>
                    <div className="recent-reviews-list">
                      {recentReviews.map((review) => (
                        <article key={`${review.title}-${review.date}`} className="recent-review-card">
                          <div className="recent-review-topline">
                            <div>
                              <strong>{review.title}</strong>
                              <p>{review.artist}</p>
                            </div>
                            <span>{review.rating} ★</span>
                          </div>
                          <p className="recent-review-note">{review.note}</p>
                          <span className="recent-review-date">{review.date}</span>
                        </article>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        );
      }

      if (currentPage === "editProfile") {
        return (
          <section className="profile-page">
            <div className="edit-panel">
              <div className="section-header-row profile-header-row">
                <p className="section-kicker">Edit profile</p>
                <button
                  type="button"
                  className="ghost-link"
                  onClick={() => setCurrentPage("profile")}
                >
                  Back to profile
                </button>
              </div>

              <form
                className="edit-profile-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  setCurrentPage("profile");
                }}
              >
                <label htmlFor="username">Username</label>
                <input
                  id="username"
                  type="text"
                  value={profileDraft.username}
                  onChange={(event) => setProfileDraft((current) => ({
                    ...current,
                    username: event.target.value
                  }))}
                />

                <label htmlFor="pictureUpload">Display picture</label>
                <div className="upload-block">
                  <div className="upload-preview">
                    {profileDraft.pictureUrl ? (
                      <img
                        className="upload-preview-image"
                        src={profileDraft.pictureUrl}
                        alt={profileDraft.username || "Profile preview"}
                      />
                    ) : (
                      <div className="upload-preview-image upload-preview-fallback">{avatarLabel}</div>
                    )}
                  </div>
                  <label htmlFor="pictureUpload" className="upload-button">
                    Choose image
                  </label>
                  <input
                    id="pictureUpload"
                    className="upload-input"
                    type="file"
                    accept="image/*"
                    onChange={(event) => {
                      const file = event.target.files && event.target.files[0];

                      if (!file) {
                        return;
                      }

                      const reader = new FileReader();
                      reader.onload = () => {
                        if (typeof reader.result === "string") {
                          setProfileDraft((current) => ({
                            ...current,
                            pictureUrl: reader.result
                          }));
                        }
                      };
                      reader.readAsDataURL(file);
                    }}
                  />
                </div>

                <label htmlFor="bio">Bio</label>
                <textarea
                  id="bio"
                  rows="5"
                  value={profileDraft.bio}
                  onChange={(event) => setProfileDraft((current) => ({
                    ...current,
                    bio: event.target.value
                  }))}
                />

                <div className="edit-actions">
                  <button type="submit" className="primary-action">Save profile</button>
                  <button
                    type="button"
                    className="secondary-action"
                    onClick={() => setCurrentPage("profile")}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </section>
        );
      }

      return (
        <section className="feed-shell">
          <div className="welcome-band">
            <h1>
              Welcome back, <span>{handle}</span>. Here&apos;s what your friends have been
              listening...
            </h1>
          </div>

          <div className="section-header-row">
            <p className="section-kicker">New from friends</p>
            <span className="ghost-link">All activity</span>
          </div>

          <section className="friend-log-grid">
            {friendLogs.map((entry) => (
              <article key={`${entry.friend}-${entry.title}`} className={`friend-log-card accent-${entry.accent}`}>
                <div className="cover-tile">
                  <span className="cover-type">{entry.type}</span>
                  <div className="cover-copy">
                    <h2>{entry.title}</h2>
                    <p>{entry.artist}</p>
                  </div>
                </div>

                <div className="log-user-row">
                  <div className="mini-avatar">{entry.friend.charAt(0).toUpperCase()}</div>
                  <span>{entry.friend}</span>
                </div>

                <div className="log-meta-row">
                  <span>{entry.rating} ★</span>
                  <span>{entry.date}</span>
                </div>
              </article>
            ))}
          </section>
        </section>
      );
    }

    return (
      <main className="page-shell">
        <section className="signed-shell">
          <header className="topbar">
            <div className="brand-mark">jukebox</div>
            <div className="search-slot">
              <input
                className="search-input"
                type="search"
                placeholder="Search albums, songs, artists"
                aria-label="Search albums, songs, artists"
              />
            </div>
            <div className="account-menu" ref={profileMenuRef}>
              <button
                type="button"
                className="account-chip"
                onClick={() => setIsProfileMenuOpen((current) => !current)}
                aria-expanded={isProfileMenuOpen}
                aria-haspopup="menu"
              >
                {profileImage ? (
                  <img
                    className="account-avatar"
                    src={profileImage}
                    alt={displayName}
                    referrerPolicy="no-referrer"
                  />
                ) : (
                  <div className="account-avatar account-fallback">{avatarLabel}</div>
                )}
                <span className="account-name">{handle}</span>
                <span className={`account-caret${isProfileMenuOpen ? " open" : ""}`}>▾</span>
              </button>

              {isProfileMenuOpen ? (
                <div className="profile-dropdown" role="menu" aria-label="Profile menu">
                  {profileMenuItems.map((item) => (
                    <button
                      key={item}
                      type="button"
                      className={`profile-menu-item${item === "Profile" && currentPage === "profile" ? " active" : ""}`}
                      onClick={() => handleProfileMenuClick(item)}
                      role="menuitem"
                    >
                      <span className="menu-icon" aria-hidden="true">
                        {item === "Profile" && "●"}
                        {item === "Reviews" && "★"}
                        {item === "ListenList" && "♫"}
                        {item === "Settings" && "⚙"}
                        {item === "Logout" && "↪"}
                      </span>
                      <span>{item}</span>
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
          </header>

          {renderSignedInPage()}
        </section>
      </main>
    );
  }

  return (
    <main className="page-shell">
      <section className="hero-band">
        <div className="hero-copy">
          <p className="eyebrow">Jukebox</p>
          <h1>Music rooms for people who want the queue to move fast.</h1>
          <p className="lead">
            Start with a clean sign-in flow now, then wire Google OAuth and
            mobile OTP delivery when the backend is ready.
          </p>
          <ul className="feature-list">
            {featurePoints.map((point) => (
              <li key={point}>{point}</li>
            ))}
          </ul>
        </div>

        <div className="auth-panel">
          <div className="panel-head">
            <span className="status-dot" />
            <p>Access</p>
          </div>

          <div className="mode-switch" role="tablist" aria-label="Sign in methods">
            <button
              type="button"
              className={mode === "google" ? "active" : ""}
              onClick={() => setMode("google")}
            >
              Google
            </button>
            <button
              type="button"
              className={mode === "mobile" ? "active" : ""}
              onClick={() => setMode("mobile")}
            >
              Mobile OTP
            </button>
          </div>

          {mode === "google" ? (
            <div className="auth-flow">
              <h2>Continue with Google</h2>
              <p className="support-copy">
                Use Google Identity Services, then let the Spring backend verify the returned ID token.
              </p>
              {authSession ? (
                <div className="session-card">
                  <p className="session-label">Signed in</p>
                  <strong>{authSession.user.name || authSession.user.email || authSession.user.phoneNumber}</strong>
                  <span>{authSession.user.email || authSession.user.phoneNumber}</span>
                  <button type="button" className="secondary-action" onClick={handleSignOut}>
                    Sign out
                  </button>
                </div>
              ) : googleClientId ? (
                <>
                  <div className="google-button-shell" ref={googleButtonRef} />
                  <p className="helper-note">
                    Google returns an ID token to the frontend. The backend verifies it before issuing the app token.
                  </p>
                </>
              ) : (
                <p className="helper-note">
                  Set `VITE_GOOGLE_CLIENT_ID` in the frontend environment before testing Google sign-in.
                </p>
              )}
            </div>
          ) : (
            <div className="auth-flow">
              <h2>Sign in with mobile OTP</h2>
              <p className="support-copy">
                Request a real SMS verification code, then let the backend exchange the successful verification for an app token.
              </p>

              {authSession ? (
                <div className="session-card">
                  <p className="session-label">Signed in</p>
                  <strong>{authSession.user.name || authSession.user.phoneNumber}</strong>
                  <span>{authSession.user.phoneNumber || authSession.user.email}</span>
                  <button type="button" className="secondary-action" onClick={handleSignOut}>
                    Sign out
                  </button>
                </div>
              ) : (
                <>
                  <form className="email-form" onSubmit={otpSent ? handleVerifyOtp : handleSendOtp}>
                    <label htmlFor="phone">Mobile number</label>
                    <input
                      id="phone"
                      type="tel"
                      inputMode="numeric"
                      maxLength={10}
                      placeholder="9876543210"
                      value={phone}
                      onChange={(event) => setPhone(event.target.value.replace(/\D/g, ""))}
                    />

                    {otpSent ? (
                      <>
                        <label htmlFor="otp">Verification code</label>
                        <input
                          id="otp"
                          type="text"
                          inputMode="numeric"
                          maxLength={6}
                          placeholder="123456"
                          value={otp}
                          onChange={(event) => setOtp(event.target.value.replace(/\D/g, ""))}
                        />
                        <button type="submit" className="primary-action">
                          {isSubmitting ? "Verifying..." : "Verify OTP"}
                        </button>
                      </>
                    ) : (
                      <button type="submit" className="primary-action">
                        {isSubmitting ? "Sending..." : "Send OTP"}
                      </button>
                    )}
                  </form>

                  <p className="helper-note">
                    In development, the backend can still fall back to a mock OTP unless you switch `APP_SMS_PROVIDER` to `twilio`.
                  </p>
                </>
              )}
            </div>
          )}

          <div className="message-strip" aria-live="polite">
            {message}
          </div>
        </div>
      </section>
    </main>
  );
}
