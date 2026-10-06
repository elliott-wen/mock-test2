package nz.ac.auckland.se325.diary.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.diary.model.LoginRequest;
import nz.ac.auckland.se325.diary.model.User;
import nz.ac.auckland.se325.diary.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository users;

    public AuthController(UserRepository users) {
        this.users = users;
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest login, HttpServletRequest request) {
        if (login.username() == null || login.password() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        User user = users.findByCredentials(login.username(), login.password())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        request.getSession().invalidate();
        request.getSession(true).setAttribute(CurrentUser.SESSION_KEY, user);
        return user;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public User me(HttpSession session) {
        return CurrentUser.require(session);
    }
}
