"use strict";

const $ = (id) => document.getElementById(id);

let me = null;

async function api(path, options = {}) {
  const res = await fetch(path, { headers: { "Content-Type": "application/json" }, ...options });
  const body = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) throw new Error((body && (body.error || body.message)) || `HTTP ${res.status}`);
  return body;
}

function today() {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}

function prettyDate(iso) {
  return new Date(iso + "T00:00:00").toLocaleDateString(undefined,
      { weekday: "long", day: "numeric", month: "long", year: "numeric" });
}

// ---------------------------------------------------------------- session

async function refreshSession() {
  try { me = await api("/api/me"); } catch { me = null; }
  render();
}

function render() {
  const inApp = !!me;
  $("login-view").classList.toggle("hidden", inApp);
  $("app-view").classList.toggle("hidden", !inApp);
  $("logout").classList.toggle("hidden", !inApp);
  $("who").textContent = inApp ? me.fullName : "Not logged in";
  if (inApp) {
    $("entry-date").value = today();
    loadEntries();
  }
}

$("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("login-error").textContent = "";
  try {
    me = await api("/api/login", {
      method: "POST",
      body: JSON.stringify({ username: $("username").value, password: $("password").value }),
    });
    $("password").value = "";
    render();
  } catch (err) { $("login-error").textContent = err.message; }
});

$("logout").addEventListener("click", async () => {
  await api("/api/logout", { method: "POST" });
  me = null;
  $("search").value = "";
  render();
});

// ---------------------------------------------------------------- entries

async function loadEntries() {
  const q = $("search").value.trim();
  const list = $("entries");
  try {
    const entries = await api("/api/entries" + (q ? "?q=" + encodeURIComponent(q) : ""));
    list.replaceChildren(...entries.map(entryCard));
    $("count").textContent = q
        ? `${entries.length} entr${entries.length === 1 ? "y" : "ies"} matching “${q}”`
        : `${entries.length} entr${entries.length === 1 ? "y" : "ies"}`;
  } catch (err) {
    list.replaceChildren();
    $("count").textContent = "Could not load entries: " + err.message;
  }
}

function entryCard(entry) {
  const node = $("entry-tpl").content.firstElementChild.cloneNode(true);
  node.querySelector(".entry-title").textContent = entry.title;
  node.querySelector(".entry-date").textContent = prettyDate(entry.date);
  node.querySelector(".entry-date").dateTime = entry.date;
  node.querySelector(".entry-body").textContent = entry.body;
  node.querySelector(".danger").addEventListener("click", async () => {
    if (!confirm(`Delete “${entry.title}”?`)) return;
    await api("/api/entries/" + entry.id, { method: "DELETE" });
    loadEntries();
  });
  return node;
}

$("search-form").addEventListener("submit", (e) => {
  e.preventDefault();
  loadEntries();
});

$("entry-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("entry-error").textContent = "";
  try {
    await api("/api/entries", {
      method: "POST",
      body: JSON.stringify({
        date: $("entry-date").value,
        title: $("entry-title").value,
        body: $("entry-body").value,
      }),
    });
    $("entry-title").value = "";
    $("entry-body").value = "";
    loadEntries();
  } catch (err) { $("entry-error").textContent = err.message; }
});

refreshSession();
