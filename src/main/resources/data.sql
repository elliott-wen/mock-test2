-- INSERT OR IGNORE means re-running on an already-seeded diary.db is a no-op.
-- Passwords are stored in plain text in this legacy system. That is a known issue and
-- is OUT OF SCOPE for this question.
INSERT OR IGNORE INTO users (id, username, password, full_name) VALUES
    (1, 'alice', 'alice123',    'Alice Chen'),
    (2, 'bob',   'B0b-Secret!', 'Bob Smith');

INSERT OR IGNORE INTO entries (id, user_id, entry_date, title, body) VALUES
    (101, 1, '2026-10-01', 'First day of term',  'New timetable. SE325 on Tuesdays and Thursdays.'),
    (102, 1, '2026-10-03', 'Gym',                'Finally went to the rec centre. Legs are dead.'),
    (103, 1, '2026-10-05', 'Assignment panic',   'SE325 assignment due Friday and I have not started.'),
    (201, 2, '2026-10-02', 'PRIVATE',            'My bank PIN is 4071. Do not forget it again.'),
    (202, 2, '2026-10-04', 'Thinking of quitting', 'Not telling anyone yet, but I might drop out.');
