package nz.ac.auckland.se325.diary.web;

import org.springframework.http.HttpStatus;

import nz.ac.auckland.se325.diary.model.User;
import jakarta.servlet.http.HttpSession;

/** Tiny session helper. The logged-in user is stored in the HTTP session by AuthController. */
public final class CurrentUser {

    static final String SESSION_KEY = "diary.user";

    private CurrentUser() {
    }

    public static User require(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_KEY);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return user;
    }
}
