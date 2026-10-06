package nz.ac.auckland.se325.diary;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

/** Functional behaviour of the diary. These must keep passing after your fix. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiaryFeatureTest {

    @Autowired
    MockMvc mvc;

    MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    MockHttpSession alice() throws Exception {
        return login("alice", "alice123");
    }

    @Test
    void validLoginReturnsUser() throws Exception {
        mvc.perform(get("/api/me").session(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.fullName").value("Alice Chen"));
    }

    @Test
    void passwordWithSymbolsWorks() throws Exception {
        mvc.perform(get("/api/me").session(login("bob", "B0b-Secret!")))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserIsRejected() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mallory\",\"password\":\"alice123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mustBeLoggedInToReadTheDiary() throws Exception {
        mvc.perform(get("/api/entries")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = alice();
        mvc.perform(post("/api/logout").session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void listsOnlyMyEntriesNewestFirst() throws Exception {
        mvc.perform(get("/api/entries").session(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(103, 102, 101)));
    }

    @Test
    void searchMatchesTitleOrBodyIgnoringCase() throws Exception {
        mvc.perform(get("/api/entries").param("q", "se325").session(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(103, 101)));
        mvc.perform(get("/api/entries").param("q", "GYM").session(alice()))
                .andExpect(jsonPath("$[*].id", contains(102)));
    }

    @Test
    void searchWithNoMatchesIsEmpty() throws Exception {
        mvc.perform(get("/api/entries").param("q", "holiday").session(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void canReadOneOfMyEntries() throws Exception {
        mvc.perform(get("/api/entries/102").session(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gym"))
                .andExpect(jsonPath("$.date").value("2026-10-03"));
    }

    @Test
    void cannotReadSomeoneElsesEntry() throws Exception {
        mvc.perform(get("/api/entries/201").session(alice()))
                .andExpect(status().isNotFound());
    }

    @Test
    void canWriteAndDeleteAnEntry() throws Exception {
        MockHttpSession session = alice();
        String created = mvc.perform(post("/api/entries").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-10-06\",\"title\":\"Test day\",\"body\":\"Wrote some tests.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-10-06"))
                .andExpect(jsonPath("$.title").value("Test day"))
                .andExpect(jsonPath("$.body").value("Wrote some tests."))
                .andReturn().getResponse().getContentAsString();

        long id = Long.parseLong(created.replaceAll(".*\"id\":(\\d+).*", "$1"));
        mvc.perform(get("/api/entries").session(session))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$", hasSize(4)));

        mvc.perform(delete("/api/entries/" + id).session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/entries").session(session)).andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void entryNeedsATitle() throws Exception {
        mvc.perform(post("/api/entries").session(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-10-06\",\"title\":\"  \",\"body\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotDeleteSomeoneElsesEntry() throws Exception {
        mvc.perform(delete("/api/entries/201").session(alice()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/entries").session(login("bob", "B0b-Secret!")))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
