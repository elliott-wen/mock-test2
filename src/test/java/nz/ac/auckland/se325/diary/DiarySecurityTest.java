package nz.ac.auckland.se325.diary;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attacks that must be stopped. These FAIL on the starter and must all PASS after your fix.
 * Do not edit or delete them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiarySecurityTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    String loginJson(String username, String password) throws Exception {
        return json.writeValueAsString(java.util.Map.of("username", username, "password", password));
    }

    MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, password)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    // ---- login screen ----------------------------------------------------------------

    @Test
    void commentingOutThePasswordCheckDoesNotLogIn() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("bob' --", "whatever")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alwaysTruePasswordDoesNotLogIn() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alice", "' OR '1'='1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quoteInUsernameFailsSafely() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("o'brien", "x")))
                .andExpect(status().isUnauthorized());
    }

    // ---- search box ------------------------------------------------------------------

    @Test
    void searchCannotLeakOtherUsersEntries() throws Exception {
        mvc.perform(get("/api/entries").param("q", "')) OR 1=1 --").session(login("alice", "alice123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchForAWordWithAnApostropheWorks() throws Exception {
        mvc.perform(get("/api/entries").param("q", "don't").session(login("alice", "alice123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---- new-entry form --------------------------------------------------------------

    @Test
    void apostropheInAnEntryIsSavedAsTyped() throws Exception {
        mvc.perform(post("/api/entries").session(login("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-10-06\",\"title\":\"Mum's birthday\",\"body\":\"It's today!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mum's birthday"))
                .andExpect(jsonPath("$.body").value("It's today!"));
    }

    @Test
    void entryBodyCannotWriteIntoAnotherUsersDiary() throws Exception {
        String payload = "x'), (2, '2026-10-06', 'Planted', 'alice was here') --";
        mvc.perform(post("/api/entries").session(login("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of(
                                "date", "2026-10-06", "title", "Innocent", "body", payload))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value(payload));

        mvc.perform(get("/api/entries").session(login("bob", "B0b-Secret!")))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
