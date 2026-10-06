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
it appears**. Fixing only the two screens above is not enough.

After your fix:

- Nobody can log in without the correct username **and** password.
- A user can only see, add or delete entries in **their own** diary.
- Text containing quotes, apostrophes, dashes and similar characters is handled correctly.
  It is saved and shown **exactly as typed**. A login or search with such text just fails
  normally ("invalid username or password", or no results), never with an error.
- Everything else works as before.

## Rules

- Fix the cause of the problem. Don't block, strip or escape particular characters yourself.
  Your fix has to work for **any** input, not just the inputs used in the tests.
- Keep your changes small and focused. Only change what the fix needs.
- The app stores passwords in plain text. That is a real problem, but it is **not** part of
  this question, so leave it alone.

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
