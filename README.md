# Question — Dear Diary

**Time guide:** ~30 minutes · **You may use any AI coding assistant, in fact, always use them.**

---

## The scenario

**Dear Diary** is a small web app for keeping a private diary. After logging in, you can write
dated entries, search them, and delete them. It is a Spring Boot application backed by a SQLite
database.

Every diary is meant to be **private**: only its owner should be able to see it. The app has
two users, `alice` and `bob`.

## Getting started

You'll need **JDK 21 (or newer)** and **Maven**.

**Run the app:**

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** and log in as alice:

| Username | Password   |
|----------|------------|
| `alice`  | `alice123` |

**Run the tests:**

```bash
mvn test
```

There are two test classes:

| Test class          | Tests | On the starter | After your fix |
|---------------------|-------|----------------|----------------|
| `DiaryFeatureTest`  | 14    | pass           | must still pass |
| `DiarySecurityTest` | 7     | **fail**       | must pass      |

`DiaryFeatureTest` checks that the app's normal features work. `DiarySecurityTest` tries to
break in through the login page, the search box and the new-entry form. Don't edit or delete
any of these tests.

**The database** is the file `diary.db`, created the first time you run the app. To look inside,
run, for example, `sqlite3 diary.db "SELECT * FROM entries;"`. To reset to the starting data,
delete the file.

---

## Your task

> **SEC-201 (Critical).** A tester reports three problems:
>
> 1. On the **login page**, they typed some special characters into the username box and
>    got logged in as **bob** without knowing his password.
> 2. While logged in as alice, they typed some special characters into the **search box** and
>    saw **bob's private entries**.
> 3. Saving an entry whose title contains an apostrophe, such as `Mum's birthday`, fails with
>    a server error.

All three problems come from **the same mistake** in the code. Find it and fix it **everywhere
it appears**.

After your fix:

- Logging in succeeds **only** when the username and password both exactly match a stored user.
  Anything else returns `401` with "Invalid username or password".
- Every list, read, add and delete request affects **only** the logged-in user's own entries.
- Any text, including `'`, `"`, `--`, `;` and `%`, is saved and returned **exactly as typed**,
  character for character.
- No input to any screen produces a `500` server error.

## Rules

- Change only files under `src/main/java/`. Don't change anything under `src/test/`, or
  `schema.sql`, `data.sql`, or `pom.xml`.
- Don't change, remove, escape or reject any characters in what the user types. The value that
  reaches the database must be exactly what the user typed.
- Don't change the API. Keep the same URLs, request bodies and response bodies.
- Don't change how passwords are stored. They stay in plain text, which is out of scope for
  this question.

## Checking your work

- All **21** tests pass when you run `mvn test`.
- None of the three problems above can be reproduced in the running app.

## What to hand in

Submit **only your `git diff`**, the changes you made to the starter code:

```bash
git diff > solution.patch
```

Hand in `solution.patch` and nothing else: no full project, no `target/` folder, and no
`diary.db`.
