/**
 * ResQTap Admin Web Portal SPA Logic
 * Pengurusan papan pemuka masa nyata: pantau SOS, semak laporan insiden, livechat sokongan, dan siaran notifikasi.
 */
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.5/firebase-app.js";
import {
  getAuth,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut
} from "https://www.gstatic.com/firebasejs/10.12.5/firebase-auth.js";
import {
  getDatabase,
  ref,
  get,
  onValue,
  push,
  set,
  update,
  remove,
  serverTimestamp,
  child
} from "https://www.gstatic.com/firebasejs/10.12.5/firebase-database.js";
import {
  getStorage,
  ref as storageRef,
  uploadBytes,
  getDownloadURL
} from "https://www.gstatic.com/firebasejs/10.12.5/firebase-storage.js";

const firebaseConfig = {
  apiKey: "AIzaSyDZ8X0sDpjbaMLt20DVA4ocNOzw9rqy-Xw",
  authDomain: "resqtap-b9ff5.firebaseapp.com",
  databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com",
  projectId: "resqtap-b9ff5",
  storageBucket: "resqtap-b9ff5.firebasestorage.app"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getDatabase(app);
const storage = getStorage(app);

const ONLINE_MS = 2 * 60 * 1000;
const RECENT_MS = 15 * 60 * 1000;
const NAV_DOT_RECENT_MS = 24 * 60 * 60 * 1000;
const NAV_SEEN_STORAGE_KEY = "ResQTap_admin_nav_seen_v1";
const SOS_STALE_MS = 10 * 60 * 1000;
const LOW_BATTERY = 20;
const INVALID_KEY = /[.#$\/\[\]]/;
const NOTICE_METHOD = "app_notification_bar";
const NOTICE_SOURCE = "admin-panel";
const LIVECHAT_ADMIN_LABEL = "ResQTap Support";
const LIVECHAT_CLAIM_NOTICE = "Agent has been claimed your ticket";
const LIVECHAT_TYPING_IDLE_MS = 3000;
const LIVECHAT_TYPING_REFRESH_MS = 1500;
const AI_CHAT_EXPIRY_MS = 5 * 60 * 1000;
const NOTIFICATION_PAGE_SIZES = [10, 20, 30];
const NOTIFICATION_CATEGORIES = [
  { id: "all", label: "All", icon: "layers-3" },
  { id: "notice", label: "Notifications", icon: "bell-ring", types: ["Notifications"] },
  { id: "sos", label: "SOS", icon: "siren", types: ["SOS Alert", "SOS Cancelled"] },
  { id: "bell", label: "Bell", icon: "bell", types: ["Bell"] },
  { id: "legacy", label: "Legacy", icon: "archive", types: ["Legacy Delivery"] }
];
const REPORT_STATUSES = [
  { id: "new", label: "New", tone: "alert" },
  { id: "reviewing", label: "Reviewing", tone: "warn" },
  { id: "resolved", label: "Resolved", tone: "good" },
  { id: "rejected", label: "Rejected", tone: "" }
];
const LIVECHAT_ATTACHMENT_OPTIONS = {
  document: {
    accept: ".pdf,.txt,.doc,.docx,.xls,.xlsx,.ppt,.pptx,application/pdf,text/plain,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation"
  },
  photo: {
    accept: "image/*,video/*"
  },
  camera: {
    accept: "image/*",
    capture: "environment"
  },
  audio: {
    accept: "audio/*"
  },
  calendar: {
    accept: ".ics,text/calendar,application/ics"
  }
};

const els = {
  publicSite: document.getElementById("publicSite"),
  authScreen: document.getElementById("authScreen"),
  deniedScreen: document.getElementById("deniedScreen"),
  appShell: document.getElementById("appShell"),
  loginForm: document.getElementById("loginForm"),
  loginButton: document.getElementById("loginButton"),
  emailInput: document.getElementById("emailInput"),
  passwordInput: document.getElementById("passwordInput"),
  authMessage: document.getElementById("authMessage"),
  deniedUid: document.getElementById("deniedUid"),
  adminPathCode: document.getElementById("adminPathCode"),
  deniedSignOut: document.getElementById("deniedSignOut"),
  sidebarSignOutBtn: document.getElementById("sidebarSignOutBtn"),
  signOutButton: document.getElementById("sidebarSignOutBtn") || document.getElementById("signOutButton"),
  syncStatus: document.getElementById("syncStatus"),
  viewTitle: document.getElementById("viewTitle"),
  sidebarToggleBtn: document.getElementById("sidebarToggleBtn"),
  sidebarCloseBtn: document.getElementById("sidebarCloseBtn"),
  sidebarBackdrop: document.getElementById("sidebarBackdrop"),
  globalSearch: document.getElementById("globalSearch"),
  dashboardRangeSelect: document.getElementById("dashboardRangeSelect"),
  exportButton: document.getElementById("exportButton"),
  clearHistoryButton: document.getElementById("clearHistoryButton"),
  metricGrid: document.getElementById("metricGrid"),
  usageChart: document.getElementById("usageChart"),
  featureChart: document.getElementById("featureChart"),
  featureLegend: document.getElementById("featureLegend"),
  usersCount: document.getElementById("usersCount"),
  usersTableBody: document.getElementById("usersTableBody"),
  roomsCount: document.getElementById("roomsCount"),
  roomsTableBody: document.getElementById("roomsTableBody"),
  sosCount: document.getElementById("sosCount"),
  sosTableBody: document.getElementById("sosTableBody"),
  reportsCount: document.getElementById("reportsCount"),
  reportsTableBody: document.getElementById("reportsTableBody"),
  noticeForm: document.getElementById("noticeForm"),
  notificationAudienceInput: document.getElementById("notificationAudienceInput"),
  notificationTitleInput: document.getElementById("notificationTitleInput"),
  notificationMessageInput: document.getElementById("notificationMessageInput"),
  notificationCharCount: document.getElementById("notificationCharCount"),
  clearNotificationButton: document.getElementById("clearNotificationButton"),
  sendNotificationButton: document.getElementById("sendNotificationButton"),
  notificationPageSizeSelect: document.getElementById("notificationPageSizeSelect"),
  notificationCategoryTabs: document.getElementById("notificationCategoryTabs"),
  notificationPagination: document.getElementById("notificationPagination"),
  notificationPageSummary: document.getElementById("notificationPageSummary"),
  notificationPrevPageButton: document.getElementById("notificationPrevPageButton"),
  notificationNextPageButton: document.getElementById("notificationNextPageButton"),
  notificationsCount: document.getElementById("notificationsCount"),
  notificationsTableBody: document.getElementById("notificationsTableBody"),
  livechatCount: document.getElementById("livechatCount"),
  livechatThreadList: document.getElementById("livechatThreadList"),
  livechatSelectedTitle: document.getElementById("livechatSelectedTitle"),
  livechatSelectedSubtitle: document.getElementById("livechatSelectedSubtitle"),
  livechatMessages: document.getElementById("livechatMessages"),
  livechatEmptyState: document.getElementById("livechatEmptyState"),
  livechatReplyForm: document.getElementById("livechatReplyForm"),
  livechatReplyInput: document.getElementById("livechatReplyInput"),
  livechatAttachButton: document.getElementById("livechatAttachButton"),
  livechatAttachMenu: document.getElementById("livechatAttachMenu"),
  livechatAttachmentInput: document.getElementById("livechatAttachmentInput"),
  livechatAttachmentLabel: document.getElementById("livechatAttachmentLabel"),
  livechatReplyButton: document.getElementById("livechatReplyButton"),
  resolveChatButton: document.getElementById("resolveChatButton"),
  deleteChatButton: document.getElementById("deleteChatButton"),
  aiChatCount: document.getElementById("aiChatCount"),
  aiChatThreadList: document.getElementById("aiChatThreadList"),
  aiChatSelectedTitle: document.getElementById("aiChatSelectedTitle"),
  aiChatSelectedSubtitle: document.getElementById("aiChatSelectedSubtitle"),
  aiChatMessages: document.getElementById("aiChatMessages"),
  deleteAiChatButton: document.getElementById("deleteAiChatButton"),
  adminsCount: document.getElementById("adminsCount"),
  adminsList: document.getElementById("adminsList"),
  grantAdminForm: document.getElementById("grantAdminForm"),
  adminUidInput: document.getElementById("adminUidInput"),
  detailPanel: document.getElementById("detailPanel"),
  toast: document.getElementById("toast")
};

const PUBLIC_PAGES = new Set(["home", "features", "flow", "safety"]);
const PUBLIC_PAGE_PATHS = {
  home: "/",
  features: "/features",
  flow: "/flow",
  safety: "/safety"
};

function isAdminRoute() {
  const p = window.location.pathname.replace(/\/+$/, "");
  const search = window.location.search;
  const hash = window.location.hash;
  return p === "/admin" || p === "/admin.html" || p.startsWith("/admin/") || search.includes("admin=true") || hash === "#admin";
}

function showPublicSite() {
  if (window.location.pathname.replace(/\/+$/, "") === "/download") {
    window.location.replace("/");
    return;
  }

  cleanupDataListeners();
  document.body.classList.add("public-site-active");
  document.body.classList.remove("admin-site-active");
  if (els.publicSite) els.publicSite.classList.remove("hidden");
  els.authScreen.classList.add("hidden");
  els.deniedScreen.classList.add("hidden");
  els.appShell.classList.add("hidden");
  document.title = "ResQTap";
  setPublicPage(readPublicPageFromUrl(), false);
  initPublicAnimations();
}

function enterAdminRoute() {
  document.documentElement.classList.remove("admin-route-loading");
  document.body.classList.remove("public-site-active");
  document.body.classList.add("admin-site-active");
  if (els.publicSite) els.publicSite.classList.add("hidden");
  document.title = "ResQTap Admin";
}

let publicRevealObserver = null;
let publicRouterBound = false;

function readPublicPageFromUrl() {
  const path = window.location.pathname.replace(/\/+$/, "") || "/";
  const page = Object.entries(PUBLIC_PAGE_PATHS).find(([, pagePath]) => pagePath === path);
  return page ? page[0] : "home";
}

function setPublicPage(page, push = true) {
  const nextPage = PUBLIC_PAGES.has(page) ? page : "home";
  document.querySelectorAll("[data-public-panel]").forEach((panel) => {
    panel.classList.toggle("is-active", panel.dataset.publicPanel === nextPage);
  });
  document.querySelectorAll("[data-public-page]").forEach((link) => {
    link.classList.toggle("is-active", link.dataset.publicPage === nextPage);
  });

  if (push) {
    const url = PUBLIC_PAGE_PATHS[nextPage] || "/";
    window.history.pushState({ publicPage: nextPage }, "", url);
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
  initPublicAnimations();
}

let lenisInstance = null;

function initSmoothMomentumScroll() {
  if (isAdminRoute() || window.innerWidth <= 860 || window.matchMedia("(pointer: coarse)").matches) {
    if (lenisInstance) {
      try { lenisInstance.destroy(); } catch (e) {}
      lenisInstance = null;
    }
    return;
  }

  if (typeof window.Lenis !== "undefined") {
    try {
      if (!lenisInstance) {
        lenisInstance = new window.Lenis({
          duration: 1.1,
          easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
          orientation: "vertical",
          gestureOrientation: "vertical",
          smoothWheel: true,
          wheelMultiplier: 1.0,
          touchMultiplier: 0,
          infinite: false,
        });

        function raf(time) {
          if (lenisInstance) {
            lenisInstance.raf(time);
            requestAnimationFrame(raf);
          }
        }
        requestAnimationFrame(raf);
      }
      return;
    } catch (err) {
      console.warn("Lenis init failed, using built-in smooth scroller", err);
    }
  }

  if (window.__momentumScrollBound) return;
  window.__momentumScrollBound = true;

  let targetY = window.scrollY;
  let currentY = window.scrollY;
  let isMoving = false;

  function updateMomentum() {
    if (!isMoving) return;
    const diff = targetY - currentY;
    if (Math.abs(diff) < 0.5) {
      currentY = targetY;
      window.scrollTo(0, currentY);
      isMoving = false;
      return;
    }
    currentY += diff * 0.14;
    window.scrollTo(0, currentY);
    requestAnimationFrame(updateMomentum);
  }

  window.addEventListener("wheel", (e) => {
    if (isAdminRoute() || e.ctrlKey || e.altKey) return;
    const scrollableParent = e.target.closest(".workspace, .livechat-thread-list, .livechat-messages, .table-wrap, .detail-panel");
    if (scrollableParent) return;

    e.preventDefault();
    const delta = e.deltaY;
    const maxScroll = Math.max(0, document.documentElement.scrollHeight - window.innerHeight);
    targetY = Math.max(0, Math.min(maxScroll, targetY + delta * 1.05));

    if (!isMoving) {
      currentY = window.scrollY;
      isMoving = true;
      requestAnimationFrame(updateMomentum);
    }
  }, { passive: false });

  window.addEventListener("scroll", () => {
    if (!isMoving) {
      targetY = window.scrollY;
      currentY = window.scrollY;
    }
  }, { passive: true });
}

function bindPublicRouter() {
  if (publicRouterBound) return;
  publicRouterBound = true;

  initSmoothMomentumScroll();

  const backToTopBtn = document.getElementById("backToTopBtn");
  if (backToTopBtn) {
    backToTopBtn.addEventListener("click", () => {
      if (lenisInstance) {
        lenisInstance.scrollTo(0, { duration: 1.2 });
      } else {
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
    });
  }

  const sections = ["home", "features", "flow", "safety"];

  function setActiveNavSection(sectionId) {
    document.querySelectorAll(".pill-nav-dock .pill-nav-item, .public-nav-links a").forEach((navLink) => {
      const href = navLink.getAttribute("href") || "";
      const isCurrent = href === `#${sectionId}` || href === `/${sectionId === "home" ? "" : sectionId}`;
      navLink.classList.toggle("is-active", isCurrent);
    });
  }

  document.addEventListener("click", (event) => {
    const anchor = event.target.closest("a[href^='#']");
    if (anchor && anchor.getAttribute("href").length > 1) {
      const targetId = anchor.getAttribute("href").slice(1);
      const targetEl = document.getElementById(targetId);
      if (targetEl) {
        event.preventDefault();

        setActiveNavSection(targetId);

        if (lenisInstance) {
          lenisInstance.scrollTo(targetEl, { offset: -70, duration: 1.1 });
        } else {
          targetEl.scrollIntoView({ behavior: "smooth", block: "start" });
        }
        return;
      }
    }

    const link = event.target.closest("[data-public-page]");
    if (!link || isAdminRoute()) return;

    event.preventDefault();
    setPublicPage(link.dataset.publicPage || "home");
  });

  function updateScrollspy() {
    if (isAdminRoute()) return;

    if (backToTopBtn) {
      if (window.scrollY > 280) {
        backToTopBtn.classList.add("is-visible");
      } else {
        backToTopBtn.classList.remove("is-visible");
      }
    }

    if (window.scrollY < 100) {
      setActiveNavSection("home");
      return;
    }

    if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 60) {
      setActiveNavSection("safety");
      return;
    }

    const triggerY = window.innerHeight * 0.36;
    let currentSection = "home";

    for (let i = 0; i < sections.length; i++) {
      const id = sections[i];
      const el = document.getElementById(id);
      if (el) {
        const rect = el.getBoundingClientRect();
        if (rect.top <= triggerY && rect.bottom > triggerY) {
          currentSection = id;
          break;
        }
      }
    }

    setActiveNavSection(currentSection);
  }

  window.addEventListener("scroll", updateScrollspy, { passive: true });
  window.addEventListener("resize", updateScrollspy, { passive: true });
  updateScrollspy();

  window.addEventListener("popstate", () => {
    if (!isAdminRoute()) setPublicPage(readPublicPageFromUrl(), false);
  });
}

let katupChatbotBound = false;

function initPublicAnimations() {
  refreshIcons();
  bindPublicRouter();
  initKatupChatbot();

  const revealItems = Array.from(document.querySelectorAll(".public-site .reveal"));
  if (!revealItems.length) return;

  if (!("IntersectionObserver" in window)) {
    revealItems.forEach((item) => item.classList.add("is-visible"));
    return;
  }

  if (!publicRevealObserver) {
    publicRevealObserver = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add("is-visible");
      });
    }, {
      rootMargin: "0px 0px -8% 0px",
      threshold: 0.12
    });
  }

  revealItems.forEach((item) => {
    publicRevealObserver.observe(item);
  });
}

function initKatupChatbot() {
  if (katupChatbotBound) return;

  const launcherBtn = document.getElementById("katupLauncherBtn");
  const chatBox = document.getElementById("katupChatBox");
  const closeBtn = document.getElementById("katupCloseBtn");
  const chatForm = document.getElementById("katupChatForm");
  const chatInput = document.getElementById("katupChatInput");
  const chatMessages = document.getElementById("katupChatMessages");
  const suggestions = document.getElementById("katupSuggestions");
  const langBmBtn = document.getElementById("katupLangBm");
  const langEnBtn = document.getElementById("katupLangEn");
  const welcomeMsg = document.getElementById("katupWelcomeMsg");
  const helperLabel = document.getElementById("katupHelperLabel");

  if (!launcherBtn || !chatBox) return;
  katupChatbotBound = true;

  let currentLang = "bm";

  const suggestionPresets = {
    bm: [
      { label: "Cara guna SOS?", prompt: "Bagaimana cara fungsi SOS ResQTap berfungsi?" },
      { label: "Jejak Live Location?", prompt: "Bagaimanakah Live Location berfungsi dalam ResQTap?" },
      { label: "Apa itu Rooms?", prompt: "Ceritakan tentang fungsi Rooms dan koordinasi kecemasan." },
      { label: "Muat turun APK", prompt: "Bagaimana cara muat turun APK ResQTap?" },
      { label: "Siapa cipta ResQTap?", prompt: "Siapakah pembangun projek ResQTap ini?" },
      { label: "Hospital berdekatan", prompt: "Bagaimana cari hospital atau klinik berdekatan?" }
    ],
    en: [
      { label: "How SOS works?", prompt: "How does ResQTap SOS alert work?" },
      { label: "Live GPS tracking?", prompt: "How does Live Location tracking work in ResQTap?" },
      { label: "What are Rooms?", prompt: "Tell me about Rooms and emergency coordination." },
      { label: "Download APK", prompt: "How do I download the ResQTap APK?" },
      { label: "Who built ResQTap?", prompt: "Who is the creator and developer of ResQTap?" },
      { label: "Nearby Hospitals", prompt: "How to find nearby hospitals and clinics?" }
    ]
  };

  function updateLanguage(lang, announce = true) {
    currentLang = lang;
    if (langBmBtn) langBmBtn.classList.toggle("is-active", lang === "bm");
    if (langEnBtn) langEnBtn.classList.toggle("is-active", lang === "en");

    if (chatInput) {
      chatInput.placeholder = lang === "bm"
        ? "Tanya apa-apa... / Ask anything..."
        : "Ask anything about ResQTap...";
    }
    if (helperLabel) {
      helperLabel.textContent = lang === "bm"
        ? "Cadangan Soalan / Quick Questions"
        : "Quick Suggestions";
    }

    if (suggestions) {
      const items = suggestionPresets[lang] || suggestionPresets.bm;
      suggestions.innerHTML = items
        .map((item) => `<button class="katup-pill-btn" type="button" data-prompt="${escapeHtml(item.prompt)}">${escapeHtml(item.label)}</button>`)
        .join("");
    }

    if (announce) {
      if (lang === "bm") {
        appendMessage("**Bahasa Melayu dipilih.** Saya sedia menjawab sebarang soalan mengenai ResQTap.", "bot");
      } else {
        appendMessage("**English selected.** I am ready to answer any questions about ResQTap.", "bot");
      }
    }
  }

  if (langBmBtn) {
    langBmBtn.addEventListener("click", (e) => {
      e.preventDefault();
      updateLanguage("bm");
    });
  }

  if (langEnBtn) {
    langEnBtn.addEventListener("click", (e) => {
      e.preventDefault();
      updateLanguage("en");
    });
  }

  function toggleChat(forceOpen) {
    const shouldOpen = typeof forceOpen === "boolean" ? forceOpen : !chatBox.classList.contains("is-open");
    if (shouldOpen) {
      chatBox.classList.add("is-open");
      chatBox.setAttribute("aria-hidden", "false");
      setTimeout(() => {
        if (chatInput) chatInput.focus();
        scrollChatToBottom();
      }, 200);
    } else {
      chatBox.classList.remove("is-open");
      chatBox.setAttribute("aria-hidden", "true");
    }
  }

  launcherBtn.addEventListener("click", (e) => {
    e.preventDefault();
    e.stopPropagation();
    toggleChat();
  });

  if (closeBtn) {
    closeBtn.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();
      toggleChat(false);
    });
  }

  document.addEventListener("click", (e) => {
    if (chatBox.classList.contains("is-open")) {
      if (!chatBox.contains(e.target) && !launcherBtn.contains(e.target)) {
        toggleChat(false);
      }
    }
  });

  function handleInnerWheel(e) {
    if (!chatMessages) return;
    e.stopPropagation();
    chatMessages.scrollTop += e.deltaY;
    e.preventDefault();
  }

  chatMessages.addEventListener("wheel", handleInnerWheel, { passive: false });
  chatBox.addEventListener("wheel", (e) => {
    if (chatMessages && e.target !== chatMessages && !chatMessages.contains(e.target)) {
      handleInnerWheel(e);
    }
  }, { passive: false });

  function scrollChatToBottom() {
    if (chatMessages) {
      chatMessages.scrollTop = chatMessages.scrollHeight;
    }
  }

  function appendMessage(text, sender = "bot") {
    if (!chatMessages) return;
    const msgDiv = document.createElement("div");
    msgDiv.className = `katup-msg katup-msg-${sender}`;

    const bubble = document.createElement("div");
    bubble.className = "katup-msg-bubble";

    const formatted = text
      .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
      .replace(/\n/g, "<br>");
    bubble.innerHTML = `<p>${formatted}</p>`;

    msgDiv.appendChild(bubble);
    chatMessages.appendChild(msgDiv);
    scrollChatToBottom();
  }

  function showTypingIndicator() {
    if (!chatMessages) return;
    const typing = document.createElement("div");
    typing.id = "katupTypingIndicator";
    typing.className = "katup-typing-indicator";
    typing.innerHTML = "<span></span><span></span><span></span>";
    chatMessages.appendChild(typing);
    scrollChatToBottom();
  }

  function removeTypingIndicator() {
    const typing = document.getElementById("katupTypingIndicator");
    if (typing) typing.remove();
  }

  function detectQueryLanguage(text) {
    const t = text.toLowerCase();
    const enTokens = ["how", "what", "where", "who", "when", "why", "can", "is", "are", "tell", "download", "apk", "help", "thanks", "thank you", "location", "hospital", "creator", "developer", "emergency"];
    const bmTokens = ["apa", "bagaimana", "macam", "cara", "siapa", "mana", "kenapa", "bila", "boleh", "tolong", "muat turun", "cipta", "bilik", "lokasi", "terima kasih", "salam", "khabar"];

    let enScore = 0;
    let bmScore = 0;

    enTokens.forEach((k) => { if (t.includes(k)) enScore++; });
    bmTokens.forEach((k) => { if (t.includes(k)) bmScore++; });

    if (enScore > bmScore) return "en";
    if (bmScore > enScore) return "bm";
    return currentLang;
  }

  function getKatupResponse(query) {
    const q = query.toLowerCase().trim();
    const detectedLang = detectQueryLanguage(query);

    if (q.includes("speak english") || q.includes("in english") || q.includes("use english")) {
      updateLanguage("en", false);
      return "Sure! I have switched to **English**. Feel free to ask me anything about ResQTap.";
    }
    if (q.includes("cakap melayu") || q.includes("dalam melayu") || q.includes("bahasa melayu")) {
      updateLanguage("bm", false);
      return "Baiklah, saya telah bertukar kepada **Bahasa Melayu**. Sila tanya apa sahaja tentang ResQTap.";
    }

    if (detectedLang === "en") {

      if (q.includes("sos") || q.includes("alert") || q.includes("emergency") || q.includes("help") || q.includes("panic")) {
        return "**ResQTap SOS feature** lets users broadcast instant emergency alerts with a single tap.\n\nIt activates an emergency alarm, broadcasts real-time GPS coordinates directly to your **Room members**, and immediately notifies administrators.";
      }
      if (q.includes("location") || q.includes("gps") || q.includes("tracking") || q.includes("live") || q.includes("map")) {
        return "**Live Location Tracking** in ResQTap is powered by Firebase Realtime Database. When an emergency is active, your trusted Room members and responders can track your position continuously on the map.";
      }
      if (q.includes("room") || q.includes("group") || q.includes("member") || q.includes("contact")) {
        return "**Rooms** connect you with your trusted circle and emergency contacts.\n\nEach Room displays live safety states, battery percentage, and latest GPS coordinates so family and friends can stay aware.";
      }
      if (q.includes("download") || q.includes("apk") || q.includes("install") || q.includes("app")) {
        return "You can download the **ResQTap APK** directly for free. Click the **Download APK** button on the home section, or download the APK file from this website.";
      }
      if (q.includes("who built") || q.includes("creator") || q.includes("developer") || q.includes("chesster") || q.includes("author") || q.includes("project")) {
        return "**ResQTap** was created and engineered by **Chesster** as a Final Year Project (OneTapSOS). Built with Android (Java), Firebase Realtime Backend, and OpenRouter AI to safeguard lives during critical moments.";
      }
      if (q.includes("hospital") || q.includes("clinic") || q.includes("medical") || q.includes("doctor")) {
        return "ResQTap includes a built-in **Nearby Hospital** feature that automatically discovers nearby healthcare centers and emergency clinics with quick navigation.";
      }
      if (q.includes("hi") || q.includes("hello") || q.includes("hey") || q.includes("how are you")) {
        return "Hello, I am **Katup**, your assistant for ResQTap. How can I assist you with SOS alerts, live tracking, or emergency rooms today?";
      }
      if (q.includes("thank") || q.includes("thanks")) {
        return "You are very welcome. Stay safe, and let me know if you need anything else.";
      }

      return "Thank you for asking. I am **Katup**, your ResQTap assistant. You can ask me about **SOS alerts**, **Live Location sharing**, **Emergency Rooms**, or **how to download the APK**.";
    }

    if (q.includes("sos") || q.includes("kecemasan") || q.includes("alert") || q.includes("bantuan") || q.includes("panik")) {
      return "Fungsi **SOS ResQTap** membolehkan anda menghantar amaran kecemasan pantas dalam 1 sentuhan.\n\nIa akan membunyikan amaran kecemasan, menghantar koordinat GPS secara langsung ke semua ahli **Room** anda, dan memberi amaran kepada admin dengan serta-merta.";
    }
    if (q.includes("location") || q.includes("lokasi") || q.includes("gps") || q.includes("live tracking") || q.includes("jejak") || q.includes("peta")) {
      return "**Live Location Tracking** ResQTap menggunakan Firebase Realtime Database untuk menyegerakkan lokasi GPS anda secara langsung. Ahli Room dan responder boleh menjejaki pergerakan anda pada peta secara tepat semasa insiden berlaku.";
    }
    if (q.includes("room") || q.includes("bilik") || q.includes("group") || q.includes("ahli") || q.includes("contact") || q.includes("kenalan")) {
      return "**Fungsi Rooms** menghubungkan anda dengan kenalan kecemasan dipercayai (trusted contacts).\n\nSetiap Room memaparkan status keselamatan ahli, tahap bateri telefon, dan lokasi terkini bagi memastikan semua orang sentiasa selamat.";
    }
    if (q.includes("download") || q.includes("muat turun") || q.includes("apk") || q.includes("install") || q.includes("pasang")) {
      return "Anda boleh muat turun **ResQTap APK** secara percuma. Sila klik butang **Download APK** di halaman utama, atau muat turun fail APK terus dari laman ini.";
    }
    if (q.includes("chesster") || q.includes("who built") || q.includes("siapa cipta") || q.includes("pembangun") || q.includes("developer") || q.includes("story") || q.includes("pasukan") || q.includes("projek")) {
      return "**ResQTap** dibangunkan oleh **Chesster** sebagai projek tahun akhir (Final Year Project - OneTapSOS). Dibina dengan Android (Java), Firebase Realtime Backend, dan OpenRouter AI untuk menyelamatkan nyawa semasa waktu kecemasan.";
    }
    if (q.includes("hospital") || q.includes("klinik") || q.includes("medical") || q.includes("rawatan") || q.includes("doktor")) {
      return "ResQTap dilengkapi fungsi **Nearby Hospital** untuk mencari pusat rawatan dan hospital berdekatan secara automatik berserta navigasi terus dari aplikasi.";
    }
    if (q.includes("hai") || q.includes("hello") || q.includes("apa khabar") || q.includes("hi") || q.includes("salam") || q.includes("hey")) {
      return "Hai, saya **Katup**, pembantu pintar anda untuk ResQTap. Ada apa-apa soalan tentang fungsi SOS, perkongsian lokasi, atau bilik kecemasan yang boleh saya bantu?";
    }
    if (q.includes("terima kasih") || q.includes("tq") || q.includes("thanks")) {
      return "Sama-sama. Kekal selamat dan beritahu saya jika ada apa-apa soalan lain.";
    }

    return "Terima kasih kerana bertanya. Saya **Katup**, pembantu pintar ResQTap. Anda boleh bertanya saya tentang **fungsi SOS**, **perkongsian Live Location**, **Rooms**, atau cara **muat turun APK** ResQTap.";
  }

  function handleUserSend(text) {
    if (!text || !text.trim()) return;
    const cleanText = text.trim();

    appendMessage(cleanText, "user");
    if (chatInput) chatInput.value = "";

    showTypingIndicator();

    setTimeout(() => {
      removeTypingIndicator();
      const reply = getKatupResponse(cleanText);
      appendMessage(reply, "bot");
    }, 550);
  }

  if (chatForm) {
    chatForm.addEventListener("submit", (e) => {
      e.preventDefault();
      if (chatInput) handleUserSend(chatInput.value);
    });
  }

  if (suggestions) {
    suggestions.addEventListener("click", (e) => {
      const btn = e.target.closest(".katup-pill-btn");
      if (!btn) return;
      const prompt = btn.getAttribute("data-prompt") || btn.innerText;
      handleUserSend(prompt);
    });
  }

  updateLanguage("bm", false);
}

let uiAudioCtx = null;
function playUiClickSound() {
  try {
    if (!uiAudioCtx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) uiAudioCtx = new AudioCtx();
    }
    if (!uiAudioCtx) return;
    if (uiAudioCtx.state === "suspended") {
      uiAudioCtx.resume();
    }

    const now = uiAudioCtx.currentTime;
    const osc = uiAudioCtx.createOscillator();
    const gain = uiAudioCtx.createGain();

    osc.type = "sine";
    osc.frequency.setValueAtTime(800, now);
    osc.frequency.exponentialRampToValueAtTime(320, now + 0.035);

    gain.gain.setValueAtTime(0.09, now);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.035);

    osc.connect(gain);
    gain.connect(uiAudioCtx.destination);

    osc.start(now);
    osc.stop(now + 0.04);
  } catch (err) {

  }
}

document.addEventListener("click", (e) => {
  const clickable = e.target.closest("button, a, .katup-pill-btn, .katup-lang-btn, .public-nav-link, .hero-btn, input[type='submit'], [role='button']");
  if (clickable) {
    playUiClickSound();
  }
}, true);

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initKatupChatbot);
} else {
  initKatupChatbot();
}

const state = {
  currentUser: null,
  activeView: "dashboard",
  search: "",
  dashboardRange: "today",
  users: {},
  rooms: {},
  userRooms: {},
  admins: {},
  legacyAlerts: {},
  incidentReports: {},
  adminNotifications: {},
  supportChats: {},
  aiChats: {},
  highlights: {},
  selectedChatUid: "",
  selectedAiChatUid: "",
  livechatAttachment: null,
  livechatAttachmentKind: "",
  livechatSending: false,
  livechatTypingActive: false,
  livechatTypingUid: "",
  livechatTypingLastSentAt: 0,
  livechatTypingTimer: null,
  notificationCategory: "all",
  notificationPage: 1,
  notificationPageSize: 10,
  dashboardSignature: "",
  dashboardMetricValues: {},
  unsubscribers: [],
  selected: null,
  detailWidth: 318,
  lastSyncAt: 0,
  navSeenSignatures: readNavSeenSignatures(),
  aiChatCleanupTimer: null,
  aiChatCleanupRunning: false
};

const DETAIL_PANEL_MIN_WIDTH = 318;
const DETAIL_PANEL_MAX_WIDTH = 760;
const DETAIL_MAIN_MIN_WIDTH = 520;

function isRecord(value) {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function asRecord(value) {
  return isRecord(value) ? value : {};
}

function entries(value) {
  return Object.entries(asRecord(value));
}

function readNavSeenSignatures() {
  try {
    const raw = window.localStorage.getItem(NAV_SEEN_STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : {};
    return isRecord(parsed) ? parsed : {};
  } catch (error) {
    return {};
  }
}

function saveNavSeenSignatures() {
  try {
    window.localStorage.setItem(NAV_SEEN_STORAGE_KEY, JSON.stringify(state.navSeenSignatures));
  } catch (error) {
    console.warn(error);
  }
}

function signatureFromItems(items) {
  return items
    .map((item) => text(item).trim())
    .filter(Boolean)
    .sort()
    .join("|");
}

function text(value) {
  return String(value === null || value === undefined ? "" : value);
}

function escapeHtml(value) {
  return text(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function icon(name) {
  return `<i data-lucide="${escapeHtml(name)}" aria-hidden="true"></i>`;
}

function refreshIcons() {
  if (window.lucide && typeof window.lucide.createIcons === "function") {
    window.lucide.createIcons({
      attrs: {
        "stroke-width": 2.4,
        "aria-hidden": "true"
      }
    });
  }
}

function millis(value) {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  if (typeof value === "string" && value.trim() !== "") {
    const parsed = Number(value);
    if (Number.isFinite(parsed)) return parsed;
  }
  return 0;
}

function formatDate(value) {
  const ms = millis(value);
  if (!ms) return "-";
  return new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(ms));
}

function ageLabel(value) {
  const ms = millis(value);
  if (!ms) return "-";
  const diff = Math.max(0, Date.now() - ms);
  const min = Math.floor(diff / 60000);
  if (min < 1) return "just now";
  if (min < 60) return `${min}m ago`;
  const hours = Math.floor(min / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

function futureAgeLabel(value) {
  const ms = millis(value);
  if (!ms) return "-";
  const diff = ms - Date.now();
  if (diff <= 0) return "expired";
  const sec = Math.ceil(diff / 1000);
  if (sec < 60) return `${sec}s`;
  const min = Math.ceil(sec / 60);
  if (min < 60) return `${min}m`;
  const hours = Math.ceil(min / 60);
  if (hours < 24) return `${hours}h`;
  const days = Math.ceil(hours / 24);
  return `${days}d`;
}

function formatNumber(value) {
  return new Intl.NumberFormat(undefined).format(Number(value) || 0);
}

function initials(name, email, uid) {
  const source = text(name).trim() || text(email).trim() || text(uid).trim();
  const parts = source.split(/\s+/).filter(Boolean);
  if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  return source.slice(0, 2).toUpperCase() || "OT";
}

function avatarHtml(item, uid) {
  const name = text(item.name || item.senderName || item.fromName);
  const email = text(item.email);
  const photoUrl = text(item.photoUrl || item.photoUri).trim();
  const b64 = text(item.photoB64).trim();
  const label = escapeHtml(initials(name, email, uid));
  const src = photoUrl || (b64 && b64.length < 250000 ? `data:image/jpeg;base64,${b64}` : "");
  if (src) {
    return `<span class="avatar"><img src="${escapeHtml(src)}" alt=""></span>`;
  }
  return `<span class="avatar avatar-default" title="${label}">${icon("user-round")}</span>`;
}

function userName(uid) {
  const user = asRecord(state.users[uid]);
  return text(user.name || user.email || uid).trim() || uid;
}

function validPathSegment(value) {
  const v = text(value).trim();
  return v !== "" && !INVALID_KEY.test(v);
}

function safeStorageName(value) {
  const clean = text(value)
    .trim()
    .replace(/[\\/:*?"<>|#%\[\]]+/g, "_")
    .replace(/\s+/g, "_")
    .slice(0, 90);
  return clean || "attachment";
}

function loadImageFromUrl(url) {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.onload = () => resolve(image);
    image.onerror = () => reject(new Error("Unable to load image."));
    image.src = url;
  });
}

async function imageFileToInlineDataUrl(file) {
  const objectUrl = URL.createObjectURL(file);
  try {
    const image = await loadImageFromUrl(objectUrl);
    const maxSide = 1280;
    const scale = Math.min(1, maxSide / Math.max(image.naturalWidth || 1, image.naturalHeight || 1));
    const width = Math.max(1, Math.round((image.naturalWidth || 1) * scale));
    const height = Math.max(1, Math.round((image.naturalHeight || 1) * scale));
    const canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext("2d");
    ctx.fillStyle = "#ffffff";
    ctx.fillRect(0, 0, width, height);
    ctx.drawImage(image, 0, 0, width, height);
    return canvas.toDataURL("image/jpeg", 0.82);
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}

function canInlineImage(file) {
  const mime = text(file && file.type).toLowerCase();
  return mime.startsWith("image/")
    && mime !== "image/gif"
    && mime !== "image/svg+xml";
}

function isAdminValue(value) {
  return value === true
    || value === "true"
    || value === 1
    || (isRecord(value) && (value.active === true || value.active === "true" || value.active === 1));
}

async function isCurrentUserAdmin(uid) {
  const adminSnapshot = await get(ref(db, `admins/${uid}`));
  if (isAdminValue(adminSnapshot.val())) return true;

  const activeSnapshot = await get(ref(db, `admins/${uid}/active`));
  return isAdminValue(activeSnapshot.val());
}

function audienceLabel(audience) {
  const labels = {
    all: "All app users",
    live: "Online users",
    admins: "Admins"
  };
  return labels[audience] || labels.all;
}

function setScreen(name) {
  enterAdminRoute();
  document.documentElement.classList.remove("admin-route-loading");
  if (els.authScreen) els.authScreen.classList.toggle("hidden", name !== "auth");
  if (els.deniedScreen) els.deniedScreen.classList.toggle("hidden", name !== "denied");
  if (els.appShell) els.appShell.classList.toggle("hidden", name !== "app");
}

function showToast(message) {
  els.toast.textContent = message;
  els.toast.classList.remove("hidden");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => els.toast.classList.add("hidden"), 3200);
}

function setSyncStatus(message) {
  els.syncStatus.textContent = message;
}

function cleanupDataListeners() {
  state.unsubscribers.forEach((unsubscribe) => {
    try {
      unsubscribe();
    } catch (error) {
      console.warn(error);
    }
  });
  state.unsubscribers = [];
  if (state.aiChatCleanupTimer) {
    window.clearInterval(state.aiChatCleanupTimer);
    state.aiChatCleanupTimer = null;
  }
}

function subscribe(path, callback) {
  const unsubscribe = onValue(
    ref(db, path),
    (snapshot) => {
      callback(snapshot.val());
      state.lastSyncAt = Date.now();
      setSyncStatus(`Online sync: ${new Date(state.lastSyncAt).toLocaleTimeString()}`);
      render();
    },
    (error) => {
      setSyncStatus("Sync blocked");
      showToast(error.message || "Unable to read database.");
    }
  );
  state.unsubscribers.push(unsubscribe);
}

function startDataListeners() {
  cleanupDataListeners();
  setSyncStatus("Syncing");
  subscribe("users", (value) => {
    state.users = asRecord(value);
  });
  subscribe("rooms", (value) => {
    state.rooms = asRecord(value);
  });
  subscribe("userRooms", (value) => {
    state.userRooms = asRecord(value);
  });
  subscribe("admins", (value) => {
    state.admins = asRecord(value);
  });
  subscribe("sos_alerts", (value) => {
    state.legacyAlerts = asRecord(value);
  });
  subscribe("incidentReports", (value) => {
    state.incidentReports = asRecord(value);
  });
  subscribe("admin_notifications", (value) => {
    state.adminNotifications = asRecord(value);
  });
  subscribe("supportChats", (value) => {
    state.supportChats = asRecord(value);
  });
  subscribe("aiChats", (value) => {
    state.aiChats = asRecord(value);
    cleanupExpiredAiChats().catch((error) => console.warn(error));
  });
  subscribe("highlights", (value) => {
    state.highlights = asRecord(value);
  });
  state.aiChatCleanupTimer = window.setInterval(() => {
    cleanupExpiredAiChats().catch((error) => console.warn(error));
  }, 30000);
}

function userRoomsFor(uid) {
  return asRecord(state.userRooms[uid]);
}

function contactsFor(user) {
  return entries(user.emergencyContacts);
}

function getUserLastSeen(uid, user) {
  let last = Math.max(millis(user.updatedAt), millis(user.createdAt));
  entries(state.rooms).forEach(([, room]) => {
    const member = asRecord(asRecord(room.members)[uid]);
    last = Math.max(last, millis(member.updatedAt));
  });
  return last;
}

function getUsers() {
  return entries(state.users).map(([uid, user]) => ({
    uid,
    data: asRecord(user),
    rooms: userRoomsFor(uid),
    contacts: contactsFor(asRecord(user)),
    lastSeen: getUserLastSeen(uid, asRecord(user))
  }));
}

function getRoomMembers(room) {
  return entries(room.members).map(([uid, member]) => {
    const data = asRecord(member);
    return {
      uid: text(data.uid).trim() || uid,
      data,
      updatedAt: millis(data.updatedAt),
      batteryPct: Number.isFinite(Number(data.batteryPct)) ? Number(data.batteryPct) : null,
      lat: Number(data.lat),
      lng: Number(data.lng)
    };
  });
}

function getRooms() {
  return entries(state.rooms).map(([code, roomValue]) => {
    const room = asRecord(roomValue);
    const members = getRoomMembers(room);
    const alerts = entries(room.sosAlerts).map(([alertId, alert]) => ({
      id: alertId,
      data: asRecord(alert)
    }));
    const lastMemberUpdate = members.reduce((max, member) => Math.max(max, member.updatedAt), 0);
    return {
      code,
      data: room,
      members,
      alerts,
      liveCount: members.filter((member) => Date.now() - member.updatedAt <= ONLINE_MS).length,
      lowBatteryCount: members.filter((member) => member.batteryPct !== null && member.batteryPct <= LOW_BATTERY).length,
      updatedAt: Math.max(millis(room.updatedAt), lastMemberUpdate, millis(room.createdAt))
    };
  });
}

function alertCreatedAt(alert) {
  return millis(alert.createdAt) || millis(alert.at) || millis(alert.clientAt);
}

function isCancelled(alert) {
  const status = text(alert.status || "").toLowerCase();
  return status === "cancelled" || Boolean(alert.cancelledAt || alert.cancelledClientAt);
}

function getSosAlerts() {
  const roomAlerts = [];
  entries(state.rooms).forEach(([roomId, room]) => {
    entries(asRecord(room).sosAlerts).forEach(([alertId, alertValue]) => {
      const alert = asRecord(alertValue);
      const createdAt = alertCreatedAt(alert);
      const senderUid = text(alert.senderUid || alert.fromUid).trim();
      const active = !isCancelled(alert);
      roomAlerts.push({
        id: text(alert.alertId).trim() || alertId,
        key: alertId,
        roomId,
        senderUid,
        senderName: text(alert.senderName || alert.fromName || userName(senderUid)).trim(),
        createdAt,
        cancelledAt: millis(alert.cancelledAt) || millis(alert.cancelledClientAt),
        active,
        stale: active && createdAt > 0 && Date.now() - createdAt > SOS_STALE_MS,
        source: "room",
        data: alert
      });
    });
  });

  entries(state.legacyAlerts).forEach(([alertId, alertValue]) => {
    const alert = asRecord(alertValue);
    const createdAt = alertCreatedAt(alert);
    const senderUid = text(alert.fromUid || alert.senderUid).trim();
    roomAlerts.push({
      id: alertId,
      key: alertId,
      roomId: text(alert.roomCode || alert.roomId).trim(),
      senderUid,
      senderName: text(alert.fromName || alert.senderName || userName(senderUid)).trim(),
      createdAt,
      cancelledAt: 0,
      active: !isCancelled(alert),
      stale: false,
      source: "legacy",
      data: alert
    });
  });

  return roomAlerts.sort((a, b) => b.createdAt - a.createdAt);
}

function reportStatusMeta(status) {
  const normalized = text(status || "new").toLowerCase();
  return REPORT_STATUSES.find((item) => item.id === normalized) || REPORT_STATUSES[0];
}

function reportAttachments(report) {
  const raw = report.attachments;
  if (Array.isArray(raw)) return raw.map(asRecord).filter((item) => Object.keys(item).length);
  return entries(raw).map(([, item]) => asRecord(item)).filter((item) => Object.keys(item).length);
}

function getIncidentReports() {
  return entries(state.incidentReports).map(([id, value]) => {
    const report = asRecord(value);
    const reportId = text(report.reportId).trim() || id;
    const senderUid = text(report.senderUid).trim();
    const user = asRecord(state.users[senderUid]);
    return {
      id: reportId,
      key: id,
      senderUid,
      senderName: text(report.senderName || user.name || user.email || senderUid).trim(),
      senderEmail: text(report.senderEmail || user.email).trim(),
      publicId: text(report.publicId || user.publicId).trim(),
      category: text(report.category).trim(),
      categoryLabel: text(report.categoryLabel || report.category).trim() || "Report",
      details: text(report.details).trim(),
      address: text(report.address).trim(),
      latitude: Number(report.latitude),
      longitude: Number(report.longitude),
      status: text(report.status || "new").trim(),
      createdAt: millis(report.createdAt),
      updatedAt: millis(report.updatedAt),
      attachments: reportAttachments(report),
      data: report
    };
  }).sort((a, b) => b.createdAt - a.createdAt || b.updatedAt - a.updatedAt);
}

function getStats() {
  const users = getUsers();
  const rooms = getRooms();
  const sosAlerts = getSosAlerts();
  const reports = getIncidentReports();
  const activeSos = sosAlerts.filter((alert) => alert.active && !alert.stale);
  const staleSos = sosAlerts.filter((alert) => alert.active && alert.stale);
  const liveUserIds = new Set();
  const lowBatteryIds = new Set();
  let contactCount = 0;

  users.forEach((user) => {
    contactCount += user.contacts.length;
  });

  rooms.forEach((room) => {
    room.members.forEach((member) => {
      if (Date.now() - member.updatedAt <= ONLINE_MS) liveUserIds.add(member.uid);
      if (member.batteryPct !== null && member.batteryPct <= LOW_BATTERY) lowBatteryIds.add(member.uid);
    });
  });

  return {
    users: users.length,
    rooms: rooms.length,
    liveUsers: liveUserIds.size,
    activeSos: activeSos.length,
    staleSos: staleSos.length,
    openReports: reports.filter((report) => report.status !== "resolved" && report.status !== "rejected").length,
    lowBattery: lowBatteryIds.size,
    contacts: contactCount
  };
}

function isOpenReport(status) {
  const normalized = text(status || "new").trim().toLowerCase();
  return normalized !== "resolved" && normalized !== "rejected";
}

function isRecentForNavDot(value) {
  const ms = millis(value);
  return !ms || Date.now() - ms <= NAV_DOT_RECENT_MS;
}

function getNavAttentionState() {
  const sosAlerts = getSosAlerts();
  const reports = getIncidentReports();
  const supportThreads = getSupportThreads();
  const aiThreads = getAiThreads();
  const notificationHistory = getNotificationHistory();
  const activeSos = sosAlerts.filter((alert) => alert.active);
  const openReports = reports.filter((report) => isOpenReport(report.status));
  const recentNotices = entries(state.adminNotifications)
    .map(([id, value]) => ({ id, value: asRecord(value) }))
    .filter((notice) => isRecentForNavDot(notice.value.sentAt));
  const recentHistory = notificationHistory.filter((notification) => isRecentForNavDot(notification.sentAt));
  const unreadSupport = supportThreads.filter((thread) => {
    const status = text(thread.status).trim().toLowerCase();
    return thread.unread && status !== "resolved";
  });
  const unreadAi = aiThreads.filter((thread) => {
    const status = text(thread.status).trim().toLowerCase();
    return thread.unread && status !== "expired" && status !== "answered";
  });

  return {
    sos: {
      count: activeSos.length,
      signature: signatureFromItems(activeSos.map((alert) => `${alert.source}:${alert.roomId}:${alert.key}:${alert.createdAt}:${alert.cancelledAt}`))
    },
    reports: {
      count: openReports.length,
      signature: signatureFromItems(openReports.map((report) => `${report.key}:${report.status}:${report.createdAt}:${report.updatedAt}`))
    },
    notices: {
      count: recentNotices.length,
      signature: signatureFromItems(recentNotices.map((notice) => `${notice.id}:${notice.value.status}:${notice.value.sentAt}`))
    },
    logs: {
      count: recentHistory.length,
      signature: signatureFromItems(recentHistory.map((notification) => `${notification.source}:${notification.id}:${notification.type}:${notification.sentAt}`))
    },
    livechat: {
      count: unreadSupport.length,
      signature: signatureFromItems(unreadSupport.map((thread) => `${thread.uid}:${thread.updatedAt}:${thread.messageCount}:${thread.lastSender}`))
    },
    aichat: {
      count: unreadAi.length,
      signature: signatureFromItems(unreadAi.map((thread) => `${thread.uid}:${thread.updatedAt}:${thread.messageCount}:${thread.lastSender}`))
    }
  };
}

function markNavViewSeen(view, attentionState) {
  const attention = asRecord(attentionState)[view];
  if (!attention || !attention.signature) return;
  if (state.navSeenSignatures[view] === attention.signature) return;
  state.navSeenSignatures[view] = attention.signature;
  saveNavSeenSignatures();
}

function localDayStart(offset = 0) {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + offset);
  return date.getTime();
}

function localDateStart(date) {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function monthStart(date, offset = 0) {
  return new Date(date.getFullYear(), date.getMonth() + offset, 1).getTime();
}

function yearStart(date, offset = 0) {
  return new Date(date.getFullYear() + offset, 0, 1).getTime();
}

function weekStart(date, offset = 0) {
  const start = localDateStart(date);
  const day = start.getDay();
  const mondayOffset = day === 0 ? -6 : 1 - day;
  start.setDate(start.getDate() + mondayOffset + offset * 7);
  return start.getTime();
}

function addMs(ms, amount) {
  return ms + amount;
}

function addDays(ms, amount) {
  return addMs(ms, amount * 24 * 60 * 60 * 1000);
}

function rangeLabel(range) {
  const labels = {
    today: "Today",
    week: "This Week",
    month: "This Month",
    year: "This Year"
  };
  return labels[range] || labels.today;
}

function comparisonLabel(range) {
  const labels = {
    today: "from yesterday",
    week: "from last week",
    month: "from last month",
    year: "from last year"
  };
  return labels[range] || labels.today;
}

function dashboardRangeWindow(range, offset = 0) {
  const now = new Date();
  if (range === "week") {
    const start = weekStart(now, offset);
    return { start, end: addDays(start, 7) };
  }
  if (range === "month") {
    return { start: monthStart(now, offset), end: monthStart(now, offset + 1) };
  }
  if (range === "year") {
    return { start: yearStart(now, offset), end: yearStart(now, offset + 1) };
  }
  return { start: localDayStart(offset), end: localDayStart(offset + 1) };
}

function makeBucket(start, end, label) {
  return { start, end, label };
}

function dashboardBuckets(range, period) {
  if (range === "today") {
    const step = 4 * 60 * 60 * 1000;
    return Array.from({ length: 6 }, (_, index) => {
      const start = period.start + step * index;
      const hour = new Intl.DateTimeFormat("en-US", {
        hour: "2-digit",
        minute: "2-digit"
      }).format(new Date(start));
      return makeBucket(start, Math.min(start + step, period.end), hour);
    });
  }

  if (range === "week") {
    return Array.from({ length: 7 }, (_, index) => {
      const start = addDays(period.start, index);
      return makeBucket(start, addDays(start, 1), new Intl.DateTimeFormat("en-US", {
        weekday: "short"
      }).format(new Date(start)));
    });
  }

  if (range === "month") {
    const buckets = [];
    let start = period.start;
    while (start < period.end) {
      const end = Math.min(addDays(start, 7), period.end);
      buckets.push(makeBucket(start, end, new Intl.DateTimeFormat("en-US", {
        day: "numeric",
        month: "short"
      }).format(new Date(start))));
      start = end;
    }
    return buckets;
  }

  return Array.from({ length: 12 }, (_, index) => {
    const start = new Date(new Date(period.start).getFullYear(), index, 1).getTime();
    const end = new Date(new Date(period.start).getFullYear(), index + 1, 1).getTime();
    return makeBucket(start, end, new Intl.DateTimeFormat("en-US", {
      month: "short"
    }).format(new Date(start)));
  });
}

function countInRange(values, start, end) {
  return values.filter((value) => {
    const ms = millis(value);
    return ms >= start && ms < end;
  }).length;
}

function valuesByBuckets(values, buckets) {
  return buckets.map((bucket) => countInRange(values, bucket.start, bucket.end));
}

function trendMeta(current, previous, compareText) {
  const diff = current - previous;
  const pct = previous > 0 ? Math.round((Math.abs(diff) / previous) * 100) : (current > 0 ? 100 : 0);
  return {
    tone: diff < 0 ? "down" : "up",
    icon: diff < 0 ? "arrow-down" : "arrow-up",
    text: `${pct}% ${compareText}`
  };
}

function getBellEvents() {
  const events = [];
  entries(state.rooms).forEach(([roomId, roomValue]) => {
    const room = asRecord(roomValue);
    entries(room.bells).forEach(([toUid, bells]) => {
      entries(bells).forEach(([bellId, bellValue]) => {
        const bell = asRecord(bellValue);
        events.push({
          id: bellId,
          roomId,
          toUid,
          fromUid: text(bell.fromUid || bell.senderUid).trim(),
          fromName: text(bell.fromName || bell.senderName).trim(),
          at: millis(bell.at) || millis(bell.createdAt) || millis(bell.clientAt) || millis(room.updatedAt),
          data: bell
        });
      });
    });
  });
  return events;
}

function getNotificationHistory() {
  const bellNotifications = getBellEvents().map((bell) => ({
    id: bell.id,
    type: "Bell",
    recipient: userName(bell.toUid),
    recipientMeta: bell.toUid,
    sender: bell.fromName || userName(bell.fromUid),
    senderMeta: bell.fromUid,
    roomId: bell.roomId,
    sentAt: bell.at,
    source: "rooms/bells"
  }));

  const sosNotifications = getSosAlerts().map((alert) => ({
    id: alert.id,
    type: alert.active ? "SOS Alert" : "SOS Cancelled",
    recipient: alert.source === "legacy" && alert.data.toUid ? userName(alert.data.toUid) : "Room members",
    recipientMeta: alert.source === "legacy" ? text(alert.data.toUid || "") : "excluding sender",
    sender: alert.senderName || userName(alert.senderUid),
    senderMeta: alert.senderUid,
    roomId: alert.roomId,
    sentAt: alert.cancelledAt || alert.createdAt,
    source: alert.source === "legacy" ? "sos_alerts" : "rooms/sosAlerts"
  }));

  const adminNotifications = entries(state.adminNotifications).map(([id, value]) => {
    const notification = asRecord(value);
    const successCount = Number(notification.successCount) || 0;
    const failureCount = Number(notification.failureCount) || 0;
    const status = text(notification.status || (failureCount ? "partial" : "sent"));
    const databaseNotice = text(notification.method) === NOTICE_METHOD;
    const failureReasons = Array.isArray(notification.failureReasons)
      ? notification.failureReasons.map((reason) => text(reason)).filter(Boolean)
      : [];
    const recipientMeta = databaseNotice
      ? `${status}: notifications`
      : `${status}: ${successCount} sent / ${failureCount} failed`;
    return {
      id,
      type: databaseNotice ? "Notifications" : "Legacy Delivery",
      canDelete: true,
      recipient: audienceLabel(text(notification.audience || "all")),
      recipientMeta,
      sender: userName(notification.sentBy),
      senderMeta: text(notification.sentBy),
      title: text(notification.title),
      message: failureReasons.length ? failureReasons.join(", ") : text(notification.message),
      roomId: "-",
      sentAt: millis(notification.sentAt),
      source: databaseNotice ? `admin_notifications:${NOTICE_METHOD}` : `admin_notifications:${status}`
    };
  });

  return bellNotifications
    .concat(adminNotifications)
    .concat(sosNotifications)
    .sort((a, b) => b.sentAt - a.sentAt);
}

function supportMessageTime(message) {
  return millis(message.createdAt) || millis(message.sentAt) || millis(message.clientAt);
}

function getSupportChatMessages(thread) {
  return entries(asRecord(thread).messages)
    .map(([id, value]) => {
      const data = asRecord(value);
      const attachment = asRecord(data.attachment);
      return {
        id,
        data,
        text: text(data.text).trim(),
        topic: text(data.topic).trim(),
        sender: text(data.sender).trim(),
        senderUid: text(data.senderUid).trim(),
        senderName: text(data.senderName).trim(),
        source: text(data.source).trim(),
        createdAt: supportMessageTime(data),
        attachment: {
          name: text(attachment.name).trim(),
          mimeType: text(attachment.mimeType).trim(),
          downloadUrl: text(attachment.downloadUrl).trim(),
          size: Number(attachment.size) || 0
        }
      };
    })
    .filter((message) => message.text || message.attachment.downloadUrl)
    .sort((a, b) => a.createdAt - b.createdAt || a.id.localeCompare(b.id));
}

function chatStatusLabel(status) {
  const normalized = text(status).trim().toLowerCase();
  if (normalized === "resolved") return "Resolved";
  if (normalized === "answered") return "Answered";
  return "Open";
}

function chatStatusTone(status) {
  const normalized = text(status).trim().toLowerCase();
  if (normalized === "resolved") return "good";
  if (normalized === "answered") return "";
  return "warn";
}

function getSupportThreads() {
  return entries(state.supportChats).filter(([, value]) => {
    const thread = asRecord(value);
    const messages = getSupportChatMessages(thread);
    // Hanya senaraikan sesi livechat yang mempunyai sekurang-kurangnya 1 mesej.
    // Jika thread kosong atau hanya mempunyai rekod rating, jangan buka di admin dashboard!
    return messages.length > 0;
  }).map(([uid, value]) => {
    const thread = asRecord(value);
    const meta = asRecord(thread.meta);
    const user = asRecord(state.users[uid]);
    const messages = getSupportChatMessages(thread);
    const last = messages[messages.length - 1] || {};
    const name = text(meta.userName || user.name || user.email || meta.userEmail || uid).trim() || uid;
    const email = text(meta.userEmail || user.email).trim();
    const updatedAt = Math.max(millis(meta.updatedAt), millis(last.createdAt));
    const lastSender = text(meta.lastSender || last.sender).trim();
    const lastAttachment = asRecord(last.attachment);
    const topic = text(meta.topic || last.topic).trim();
    const lastMessage = text(meta.lastMessage || last.text).trim()
      || (lastAttachment.downloadUrl ? `Attachment: ${text(lastAttachment.name || "file")}` : "");

    return {
      uid,
      meta,
      messages,
      name,
      email,
      topic,
      publicId: text(meta.publicId || user.publicId).trim(),
      photoUrl: text(user.photoUrl || meta.photoUrl).trim(),
      photoB64: text(user.photoB64 || meta.photoB64).trim(),
      status: text(meta.status).trim() || (messages.length > 0 ? "open" : "resolved"),
      updatedAt,
      lastMessage,
      lastSender,
      unread: (lastSender === "user" || lastSender === "ai") && text(meta.status).trim() !== "resolved",
      messageCount: messages.length
    };
  }).sort((a, b) => b.updatedAt - a.updatedAt || a.name.localeCompare(b.name));
}

function aiMessageTime(message) {
  return millis(message.createdAt) || millis(message.sentAt) || millis(message.clientAt);
}

function getAiChatMessages(thread) {
  return entries(asRecord(thread).messages)
    .map(([id, value]) => {
      const data = asRecord(value);
      const role = text(data.role || data.sender).trim().toLowerCase();
      return {
        id,
        data,
        text: text(data.text || data.content).trim(),
        role: role === "ai" ? "assistant" : role,
        senderUid: text(data.senderUid).trim(),
        senderName: text(data.senderName).trim(),
        source: text(data.source).trim(),
        createdAt: aiMessageTime(data)
      };
    })
    .filter((message) => message.text && (message.role === "user" || message.role === "assistant"))
    .sort((a, b) => a.createdAt - b.createdAt || a.id.localeCompare(b.id));
}

function getAiThreads() {
  const records = new Map();
  entries(state.supportChats).forEach(([uid, value]) => {
    const aiAssistant = asRecord(asRecord(value).aiAssistant);
    if (Object.keys(aiAssistant).length > 0) {
      records.set(uid, { uid, value: aiAssistant, store: "supportChats" });
    }
  });
  entries(state.aiChats).forEach(([uid, value]) => {
    records.set(uid, { uid, value, store: "aiChats" });
  });

  return Array.from(records.values()).map((record) => {
    const { uid, value, store } = record;
    const thread = asRecord(value);
    const meta = asRecord(thread.meta);
    const user = asRecord(state.users[uid]);
    const messages = getAiChatMessages(thread);
    const last = messages[messages.length - 1] || {};
    const lastUser = [...messages].reverse().find((message) => message.role === "user") || {};
    const name = text(meta.userName || user.name || user.email || meta.userEmail || uid).trim() || uid;
    const email = text(meta.userEmail || user.email).trim();
    const lastUserAt = millis(meta.lastUserAt) || millis(meta.lastUserClientAt) || millis(lastUser.createdAt);
    const expiresAt = millis(meta.expiresAt) || (lastUserAt ? lastUserAt + AI_CHAT_EXPIRY_MS : 0);
    const updatedAt = Math.max(millis(meta.updatedAt), millis(last.createdAt), lastUserAt);
    const lastMessage = text(meta.lastMessage || last.text).trim();
    const lastSender = text(meta.lastSender || last.role).trim();
    const expired = expiresAt > 0 && expiresAt <= Date.now();

    return {
      uid,
      meta,
      messages,
      name,
      email,
      publicId: text(meta.publicId || user.publicId).trim(),
      photoUrl: text(user.photoUrl || meta.photoUrl).trim(),
      photoB64: text(user.photoB64 || meta.photoB64).trim(),
      status: expired ? "expired" : text(meta.status || "active").trim() || "active",
      store,
      updatedAt,
      expiresAt,
      lastMessage,
      lastSender,
      unread: lastSender === "user",
      messageCount: messages.length
    };
  }).sort((a, b) => b.updatedAt - a.updatedAt || a.name.localeCompare(b.name));
}

function getDashboardModel(range = state.dashboardRange) {
  const users = getUsers();
  const rooms = getRooms();
  const alerts = getSosAlerts();
  const reports = getIncidentReports();
  const bells = getBellEvents();
  const supportMessages = getSupportThreads().flatMap((thread) => thread.messages);
  const aiMessages = getAiThreads().flatMap((thread) => thread.messages);
  const aiAssistanceTimes = aiMessages
    .filter((message) => message.role === "assistant")
    .map((message) => message.createdAt)
    .filter(Boolean);
  const userTimes = users
    .map((user) => millis(user.data.createdAt) || millis(user.data.updatedAt) || user.lastSeen)
    .filter(Boolean);
  const communicationTimes = [];

  rooms.forEach((room) => {
    if (room.updatedAt) communicationTimes.push(room.updatedAt);
    room.members.forEach((member) => {
      if (member.updatedAt) communicationTimes.push(member.updatedAt);
    });
  });
  bells.forEach((bell) => {
    if (bell.at) communicationTimes.push(bell.at);
  });
  supportMessages.forEach((message) => {
    if (message.createdAt) communicationTimes.push(message.createdAt);
  });
  aiMessages.forEach((message) => {
    if (message.createdAt) communicationTimes.push(message.createdAt);
  });

  const bellTimes = bells.map((bell) => bell.at).filter(Boolean);
  const alertTimes = alerts.map((alert) => alert.createdAt).filter(Boolean);
  const reportTimes = reports.map((report) => report.createdAt).filter(Boolean);
  const emergencyTimes = alertTimes.concat(reportTimes);
  const current = dashboardRangeWindow(range, 0);
  const previous = dashboardRangeWindow(range, -1);
  const compareText = comparisonLabel(range);
  const buckets = dashboardBuckets(range, current);
  const currentUsers = countInRange(userTimes, current.start, current.end);
  const previousUsers = countInRange(userTimes, previous.start, previous.end);
  const currentCommunication = countInRange(communicationTimes, current.start, current.end);
  const previousCommunication = countInRange(communicationTimes, previous.start, previous.end);
  const currentEmergency = countInRange(emergencyTimes, current.start, current.end);
  const previousEmergency = countInRange(emergencyTimes, previous.start, previous.end);
  const currentSignals = countInRange(bellTimes, current.start, current.end);
  const currentAiAssistance = countInRange(aiAssistanceTimes, current.start, current.end);
  const featureGroups = [
    { label: "Chat & Calls", value: currentCommunication, color: "#3B82F6" },
    { label: "Active Users", value: currentUsers, color: "#10B981" },
    { label: "AI Assistance", value: currentAiAssistance, color: "#8B5CF6" },
    { label: "Signals & Bells", value: currentSignals, color: "#F59E0B" },
    { label: "SOS Alerts", value: countInRange(alertTimes, current.start, current.end), color: "#E60067" },
    { label: "Incident Reports", value: countInRange(reportTimes, current.start, current.end), color: "#EC4899" }
  ];

  return {
    rangeLabel: rangeLabel(range),
    totals: {
      users: currentUsers,
      communication: currentCommunication,
      emergency: currentEmergency
    },
    trends: {
      users: trendMeta(currentUsers, previousUsers, compareText),
      communication: trendMeta(currentCommunication, previousCommunication, compareText),
      emergency: trendMeta(currentEmergency, previousEmergency, compareText)
    },
    buckets,
    series: [
      { label: "Communication (Chat & Calls)", values: valuesByBuckets(communicationTimes, buckets), color: "#3B82F6" },
      { label: "Active Users", values: valuesByBuckets(userTimes, buckets), color: "#10B981" },
      { label: "Emergency & SOS", values: valuesByBuckets(alertTimes.concat(bellTimes), buckets), color: "#E60067" }
    ],
    featureGroups
  };
}

function matchesSearch(fields) {
  const query = state.search.trim().toLowerCase();
  if (!query) return true;
  return fields.some((field) => text(field).toLowerCase().includes(query));
}

function notificationCategory(categoryId) {
  return NOTIFICATION_CATEGORIES.find((category) => category.id === categoryId) || NOTIFICATION_CATEGORIES[0];
}

function matchesNotificationCategory(notification, categoryId) {
  const category = notificationCategory(categoryId);
  if (!category.types) return true;
  return category.types.includes(notification.type);
}

function renderNotificationCategoryTabs(rows, activeCategoryId) {
  els.notificationCategoryTabs.innerHTML = NOTIFICATION_CATEGORIES.map((category) => {
    const count = rows.filter((notification) => matchesNotificationCategory(notification, category.id)).length;
    const active = category.id === activeCategoryId;
    return `
      <button
        class="category-tab${active ? " is-active" : ""}"
        data-notification-category="${escapeHtml(category.id)}"
        type="button"
        role="tab"
        aria-selected="${active ? "true" : "false"}"
      >
        ${icon(category.icon)}
        <span>${escapeHtml(category.label)}</span>
        <span class="category-tab-count">${count}</span>
      </button>
    `;
  }).join("");
}

function emptyRow(colspan, message) {
  return `<tr><td colspan="${colspan}" class="muted">${escapeHtml(message)}</td></tr>`;
}

function pill(label, tone = "") {
  const cls = tone ? ` status-pill ${tone}` : "status-pill";
  return `<span class="${cls.trim()}">${escapeHtml(label)}</span>`;
}

function niceChartMax(value) {
  if (value <= 4) return 4;
  if (value <= 8) return 8;
  const magnitude = 10 ** Math.floor(Math.log10(value));
  return Math.ceil(value / magnitude) * magnitude;
}

function renderLineChart(days, series) {
  const width = 1000;
  const height = 360;
  const left = 44;
  const right = 28;
  const top = 34;
  const bottom = 54;
  const plotWidth = width - left - right;
  const plotHeight = height - top - bottom;
  const maxValue = niceChartMax(Math.max(1, ...series.flatMap((item) => item.values)));
  const ticks = [0, Math.round(maxValue * 0.25), Math.round(maxValue * 0.5), Math.round(maxValue * 0.75), maxValue];
  const uniqueTicks = [...new Set(ticks)].sort((a, b) => a - b);
  
  const x = (index) => left + (days.length <= 1 ? plotWidth / 2 : (plotWidth / (days.length - 1)) * index);
  const y = (value) => top + plotHeight - (Math.max(0, value) / maxValue) * plotHeight;

  const defs = `
    <defs>
      <linearGradient id="grad-blue" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.25"/>
        <stop offset="100%" stop-color="#3B82F6" stop-opacity="0.0"/>
      </linearGradient>
      <linearGradient id="grad-green" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#10B981" stop-opacity="0.25"/>
        <stop offset="100%" stop-color="#10B981" stop-opacity="0.0"/>
      </linearGradient>
      <linearGradient id="grad-pink" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#E60067" stop-opacity="0.25"/>
        <stop offset="100%" stop-color="#E60067" stop-opacity="0.0"/>
      </linearGradient>
    </defs>
  `;

  const gradMap = { "#3B82F6": "url(#grad-blue)", "#10B981": "url(#grad-green)", "#E60067": "url(#grad-pink)" };

  const grid = uniqueTicks.map((tick) => {
    const yPos = y(tick).toFixed(1);
    return `
      <g>
        <line x1="${left}" y1="${yPos}" x2="${width - right}" y2="${yPos}" class="chart-grid-line" stroke="#F1F5F9" stroke-width="1.2" stroke-dasharray="4 4"></line>
        <text x="${left - 10}" y="${Number(yPos) + 4}" class="chart-axis-text" text-anchor="end" fill="#94A3B8" font-size="12" font-weight="750">${escapeHtml(formatNumber(tick))}</text>
      </g>
    `;
  }).join("");

  const labels = days.map((day, index) => {
    const xPos = x(index).toFixed(1);
    return `
      <text x="${xPos}" y="${height - 14}" class="chart-axis-text chart-x-label" text-anchor="middle" fill="#64748B" font-size="12" font-weight="700">${escapeHtml(day.label)}</text>
    `;
  }).join("");

  const layers = series.map((item, seriesIndex) => {
    const pts = item.values.map((value, index) => ({ x: x(index).toFixed(1), y: y(value).toFixed(1), val: value }));
    const pointsStr = pts.map((p) => `${p.x},${p.y}`).join(" ");
    const areaD = `M ${pts[0].x} ${y(0).toFixed(1)} L ${pts.map(p => `${p.x} ${p.y}`).join(" L ")} L ${pts[pts.length - 1].x} ${y(0).toFixed(1)} Z`;
    const gradFill = gradMap[item.color] || "none";

    const circles = pts.map((p, index) => {
      const showBadge = p.val > 0 ? `
        <g class="chart-value-badge">
          <rect x="${Number(p.x) - 10}" y="${Number(p.y) - 22}" width="20" height="16" rx="4" fill="${item.color}"/>
          <text x="${p.x}" y="${Number(p.y) - 11}" fill="#FFFFFF" font-size="10" font-weight="800" text-anchor="middle">${p.val}</text>
        </g>
      ` : "";
      return `
        <circle class="chart-point" cx="${p.x}" cy="${p.y}" r="5.5" fill="${item.color}" stroke="#FFFFFF" stroke-width="2.5" style="--point-index:${index};"></circle>
        ${showBadge}
      `;
    }).join("");

    return `
      <path d="${areaD}" fill="${gradFill}" />
      <polyline class="chart-line" pathLength="100" points="${pointsStr}" fill="none" stroke="${item.color}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" style="--line-index:${seriesIndex};"></polyline>
      ${circles}
    `;
  }).join("");

  const legend = series.map((item) => `
    <span class="chart-legend-item">
      <span class="legend-dot" style="background:${item.color};"></span>
      <strong>${escapeHtml(item.label)}</strong>
    </span>
  `).join("");

  return `
    <div class="chart-legend">${legend}</div>
    <svg class="chart-svg" viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" role="img" aria-label="Usage statistics chart">
      ${defs}
      ${grid}
      ${labels}
      ${layers}
    </svg>
  `;
}

function renderFeatureDonut(features) {
  const total = features.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return `
      <div class="donut-chart is-empty">
        <svg class="donut-svg" viewBox="0 0 120 120" role="img" aria-hidden="true">
          <circle class="donut-track" cx="60" cy="60" r="44"></circle>
          <circle class="donut-center" cx="60" cy="60" r="28"></circle>
        </svg>
      </div>
    `;
  }

  let offset = 0;
  const segments = features
    .filter((item) => item.value > 0)
    .map((item, index) => {
      const dash = (item.value / total) * 100;
      const gap = Math.max(0, 100 - dash);
      const dashOffset = -offset;
      offset += dash;
      return `
        <circle
          class="donut-segment"
          cx="60"
          cy="60"
          r="44"
          pathLength="100"
          stroke="${item.color}"
          stroke-dasharray="${dash.toFixed(3)} ${gap.toFixed(3)}"
          style="--dash-offset:${dashOffset.toFixed(3)}; --segment-index:${index};"
        ></circle>
      `;
    }).join("");

  return `
    <div class="donut-chart">
      <svg class="donut-svg" viewBox="0 0 120 120" role="img" aria-hidden="true">
        <circle class="donut-track" cx="60" cy="60" r="44"></circle>
        ${segments}
        <circle class="donut-center" cx="60" cy="60" r="28"></circle>
      </svg>
    </div>
  `;
}

function renderFeatureLegend(features) {
  const total = features.reduce((sum, item) => sum + item.value, 0);
  return features.map((item) => {
    const pct = total > 0 ? Math.round((item.value / total) * 100) : 0;
    return `
      <div class="feature-legend-item">
        <span class="legend-dot" style="background:${item.color}"></span>
        <span>${escapeHtml(item.label)}</span>
        <strong>${pct}%</strong>
      </div>
    `;
  }).join("");
}

function dashboardRenderSignature(model, cards) {
  return JSON.stringify({
    range: state.dashboardRange,
    cards: cards.map((card) => ({
      label: card.label,
      value: card.value,
      trendTone: card.trend.tone,
      trendText: card.trend.text
    })),
    buckets: model.buckets.map((bucket) => bucket.label),
    series: model.series.map((item) => item.values),
    features: model.featureGroups.map((item) => ({
      label: item.label,
      value: item.value
    }))
  });
}

function easeOutCubic(value) {
  return 1 - ((1 - value) ** 3);
}

function animateNumber(el, from, to) {
  const start = performance.now();
  const duration = 620;
  const diff = to - from;

  function tick(now) {
    const progress = Math.min(1, (now - start) / duration);
    const current = Math.round(from + (diff * easeOutCubic(progress)));
    el.textContent = formatNumber(current);
    if (progress < 1) requestAnimationFrame(tick);
  }

  requestAnimationFrame(tick);
}

function animateDashboardMetrics(cards) {
  cards.forEach((card, index) => {
    const valueEl = els.metricGrid.querySelector(`[data-metric-index="${index}"]`);
    if (!valueEl) return;

    const previous = Number(state.dashboardMetricValues[card.label]);
    const next = Number(card.value) || 0;
    if (Number.isFinite(previous) && previous !== next) {
      animateNumber(valueEl, previous, next);
    } else {
      valueEl.textContent = formatNumber(next);
    }
    state.dashboardMetricValues[card.label] = next;
  });
}

function render() {
  if (els.appShell.classList.contains("hidden")) return;
  renderNav();
  renderDashboard();
  renderUsers();
  renderRooms();
  renderSos();
  renderReports();
  renderNotices();
  renderLivechat();
  renderAiChat();
  renderAdmins();
  renderHighlights();
  renderDetail();
  refreshIcons();
}

function renderNav() {
  const titles = {
    dashboard: "Dashboard",
    users: "Users",
    rooms: "Rooms",
    sos: "SOS Alerts",
    reports: "Reports",
    notices: "Notifications",
    livechat: "Livechat",
    aichat: "AI Chat",
    highlights: "Highlights (Mobile Carousel)",
    logs: "Logs",
    admins: "Admins"
  };
  const attentionState = getNavAttentionState();
  markNavViewSeen(state.activeView, attentionState);
  els.viewTitle.textContent = titles[state.activeView] || "Dashboard";
  document.querySelectorAll(".nav-button").forEach((button) => {
    const view = button.dataset.view || "";
    const attention = attentionState[view] || { count: 0, signature: "" };
    const attentionCount = Number(attention.count) || 0;
    const attentionSignature = text(attention.signature);
    const hasUnseenAttention = attentionCount > 0 && attentionSignature && state.navSeenSignatures[view] !== attentionSignature;
    button.classList.toggle("is-active", view === state.activeView);
    button.classList.toggle("has-dot", hasUnseenAttention);
    if (hasUnseenAttention) {
      button.dataset.attentionCount = String(attentionCount);
      button.title = `${attentionCount} item${attentionCount === 1 ? "" : "s"} need attention`;
    } else {
      delete button.dataset.attentionCount;
      button.removeAttribute("title");
    }
  });
  document.querySelectorAll(".view-section").forEach((section) => section.classList.add("hidden"));
  const active = document.getElementById(`${state.activeView}View`);
  if (active) active.classList.remove("hidden");
}

function renderDashboard() {
  const model = getDashboardModel();
  els.dashboardRangeSelect.value = state.dashboardRange;
  const cards = [
    {
      label: "Total Users",
      value: model.totals.users,
      trend: model.trends.users,
      iconName: "user-round",
      tone: "purple"
    },
    {
      label: "Communication",
      value: model.totals.communication,
      trend: model.trends.communication,
      iconName: "message-square",
      tone: "green"
    },
    {
      label: "Emergency",
      value: model.totals.emergency,
      trend: model.trends.emergency,
      iconName: "siren",
      tone: "red"
    }
  ];
  const signature = dashboardRenderSignature(model, cards);
  if (state.dashboardSignature === signature) return;
  state.dashboardSignature = signature;

  els.metricGrid.innerHTML = cards.map((card, index) => `
    <article class="metric-card stat-card ${card.tone}">
      <div class="metric-copy">
        <span class="metric-label">${escapeHtml(card.label)}</span>
        <strong data-metric-index="${index}">${escapeHtml(formatNumber(state.dashboardMetricValues[card.label] ?? card.value))}</strong>
        <em class="metric-trend ${card.trend.tone}">
          ${icon(card.trend.icon)}
          <span>${escapeHtml(card.trend.text)}</span>
        </em>
      </div>
      <div class="stat-icon">${icon(card.iconName)}</div>
    </article>
  `).join("");

  els.usageChart.setAttribute("aria-label", `Usage statistics for ${model.rangeLabel}`);
  els.usageChart.innerHTML = renderLineChart(model.buckets, model.series);
  els.featureChart.innerHTML = renderFeatureDonut(model.featureGroups);
  els.featureLegend.innerHTML = renderFeatureLegend(model.featureGroups);
  animateDashboardMetrics(cards);
}

function renderUsers() {
  const rows = getUsers()
    .filter((user) => matchesSearch([
      user.uid,
      user.data.name,
      user.data.email,
      user.data.publicId,
      user.data.phoneNumber,
      user.data.bloodType
    ]))
    .sort((a, b) => b.lastSeen - a.lastSeen);

  els.usersCount.textContent = `${rows.length} shown`;
  els.usersTableBody.innerHTML = rows.length ? rows.map((user) => {
    const roomCount = entries(user.rooms).length;
    const contactCount = user.contacts.length;
    const medical = [user.data.bloodType || "-", user.data.allergies ? "Allergies" : "", user.data.existingConditions ? "Conditions" : ""]
      .filter(Boolean)
      .join(" / ");
    const live = Date.now() - user.lastSeen <= ONLINE_MS;
    return `
      <tr>
        <td>
          <div class="user-cell">
            ${avatarHtml(user.data, user.uid)}
            <div class="cell-main">
              <strong>${escapeHtml(user.data.name || "Unnamed")}</strong>
              <span>${escapeHtml(user.data.email || "-")}</span>
              <span>${escapeHtml(user.data.publicId || user.uid)}</span>
            </div>
          </div>
        </td>
        <td>${escapeHtml(medical)}</td>
        <td>${contactCount}</td>
        <td>${roomCount}</td>
        <td>${pill(live ? "Online" : ageLabel(user.lastSeen), live ? "good" : "")}</td>
        <td>
          <div class="row-actions">
            <button class="small-button" data-action="view-user" data-uid="${escapeHtml(user.uid)}" type="button">${icon("eye")}<span>View</span></button>
            <button class="small-button danger" data-action="delete-user" data-uid="${escapeHtml(user.uid)}" type="button">${icon("trash-2")}<span>Delete</span></button>
          </div>
        </td>
      </tr>
    `;
  }).join("") : emptyRow(6, "No users match the current search.");
}

function renderRooms() {
  const rows = getRooms()
    .filter((room) => matchesSearch([
      room.code,
      room.data.name,
      room.data.creatorUid,
      userName(room.data.creatorUid)
    ]))
    .sort((a, b) => b.updatedAt - a.updatedAt);

  els.roomsCount.textContent = `${rows.length} shown`;
  els.roomsTableBody.innerHTML = rows.length ? rows.map((room) => {
    const activeAlerts = room.alerts.filter((alert) => !isCancelled(alert.data)).length;
    return `
      <tr>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(room.data.name || room.code)}</strong>
            <span>${escapeHtml(room.code)}</span>
          </div>
        </td>
        <td>${escapeHtml(userName(room.data.creatorUid))}</td>
        <td>${room.members.length}</td>
        <td>${pill(room.liveCount, room.liveCount ? "good" : "")}</td>
        <td>${pill(activeAlerts, activeAlerts ? "alert" : "")}</td>
        <td>${escapeHtml(ageLabel(room.updatedAt))}</td>
        <td>
          <div class="row-actions">
            <button class="small-button" data-action="view-room" data-room="${escapeHtml(room.code)}" type="button">${icon("eye")}<span>View</span></button>
            <button class="small-button danger" data-action="delete-room" data-room="${escapeHtml(room.code)}" type="button">${icon("trash-2")}<span>Delete</span></button>
          </div>
        </td>
      </tr>
    `;
  }).join("") : emptyRow(7, "No rooms match the current search.");
}

function renderSos() {
  const rows = getSosAlerts()
    .filter((alert) => matchesSearch([
      alert.id,
      alert.roomId,
      alert.senderUid,
      alert.senderName,
      alert.source
    ]));

  els.sosCount.textContent = `${rows.length} shown`;
  els.sosTableBody.innerHTML = rows.length ? rows.map((alert) => {
    const label = alert.active ? (alert.stale ? "Stale" : "Active") : "Cancelled";
    const tone = alert.active ? (alert.stale ? "warn" : "alert") : "good";
    const canCancel = alert.source === "room" && alert.active;
    return `
      <tr>
        <td>${pill(label, tone)}</td>
        <td>${escapeHtml(alert.roomId || "-")}</td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(alert.senderName || "Unknown")}</strong>
            <span>${escapeHtml(alert.senderUid || "-")}</span>
          </div>
        </td>
        <td>${escapeHtml(formatDate(alert.createdAt))}</td>
        <td>${escapeHtml(ageLabel(alert.createdAt))}</td>
        <td>
          <div class="row-actions">
            <button class="small-button" data-action="call-sos" data-uid="${escapeHtml(alert.senderUid)}" data-name="${escapeHtml(alert.senderName)}" type="button">${icon("phone")}<span>Call</span></button>
            <button class="small-button" data-action="view-sos" data-room="${escapeHtml(alert.roomId)}" data-alert="${escapeHtml(alert.key)}" type="button">${icon("eye")}<span>View</span></button>
            ${canCancel ? `<button class="small-button danger" data-action="cancel-sos" data-room="${escapeHtml(alert.roomId)}" data-alert="${escapeHtml(alert.key)}" type="button">${icon("circle-x")}<span>Cancel</span></button>` : ""}
          </div>
        </td>
      </tr>
    `;
  }).join("") : emptyRow(6, "No SOS alerts match the current search.");
}

function renderReports() {
  const rows = getIncidentReports()
    .filter((report) => matchesSearch([
      report.id,
      report.senderUid,
      report.senderName,
      report.senderEmail,
      report.publicId,
      report.category,
      report.categoryLabel,
      report.status,
      report.address,
      report.details
    ]));

  els.reportsCount.textContent = `${rows.length} shown`;
  els.reportsTableBody.innerHTML = rows.length ? rows.map((report) => {
    const status = reportStatusMeta(report.status);
    const hasMap = Number.isFinite(report.latitude) && Number.isFinite(report.longitude);
    return `
      <tr>
        <td>${pill(status.label, status.tone)}</td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(report.categoryLabel)}</strong>
            <span>${escapeHtml(report.id)}</span>
          </div>
        </td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(report.senderName || "Unknown")}</strong>
            <span>${escapeHtml(report.senderEmail || report.senderUid || "-")}</span>
          </div>
        </td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(report.address || "-")}</strong>
            <span>${hasMap ? `${escapeHtml(report.latitude.toFixed(5))}, ${escapeHtml(report.longitude.toFixed(5))}` : "-"}</span>
          </div>
        </td>
        <td>${escapeHtml(formatDate(report.createdAt))}</td>
        <td>
          <div class="row-actions">
            <button class="small-button" data-action="view-report" data-report="${escapeHtml(report.key)}" type="button">${icon("eye")}<span>View</span></button>
            ${report.status !== "reviewing" ? `<button class="small-button" data-action="set-report-status" data-report="${escapeHtml(report.key)}" data-status="reviewing" type="button">${icon("clock")}<span>Review</span></button>` : ""}
            ${report.status !== "resolved" ? `<button class="small-button" data-action="set-report-status" data-report="${escapeHtml(report.key)}" data-status="resolved" type="button">${icon("check-circle-2")}<span>Resolve</span></button>` : ""}
            <button class="small-button danger" data-action="delete-report" data-report="${escapeHtml(report.key)}" type="button">${icon("trash-2")}<span>Delete</span></button>
          </div>
        </td>
      </tr>
    `;
  }).join("") : emptyRow(6, "No incident reports match the current search.");
}

function renderNotices() {
  const searchedRows = getNotificationHistory()
    .filter((notification) => matchesSearch([
      notification.id,
      notification.type,
      notification.recipient,
      notification.recipientMeta,
      notification.sender,
      notification.senderMeta,
      notification.title,
      notification.message,
      notification.roomId,
      notification.source
    ]));
  const activeCategory = notificationCategory(state.notificationCategory);
  const filteredRows = searchedRows.filter((notification) => (
    matchesNotificationCategory(notification, activeCategory.id)
  ));
  const pageSize = NOTIFICATION_PAGE_SIZES.includes(state.notificationPageSize)
    ? state.notificationPageSize
    : 10;
  const totalPages = Math.max(1, Math.ceil(filteredRows.length / pageSize));
  const page = Math.min(Math.max(Number(state.notificationPage) || 1, 1), totalPages);
  const start = (page - 1) * pageSize;
  const rows = filteredRows.slice(start, start + pageSize);

  state.notificationCategory = activeCategory.id;
  state.notificationPage = page;
  renderNotificationCategoryTabs(searchedRows, activeCategory.id);

  els.notificationPageSizeSelect.value = String(pageSize);
  els.notificationsCount.textContent = filteredRows.length
    ? `${start + 1}-${start + rows.length} of ${filteredRows.length}`
    : "0 shown";
  els.notificationPagination.classList.toggle("hidden", filteredRows.length === 0);
  els.notificationPageSummary.textContent = `Page ${page} of ${totalPages}`;
  els.notificationPrevPageButton.disabled = page <= 1;
  els.notificationNextPageButton.disabled = page >= totalPages;
  els.notificationsTableBody.innerHTML = rows.length ? rows.map((notification) => {
    const tone = notification.type.includes("SOS")
      ? (notification.type.includes("Cancelled") ? "good" : "alert")
      : "";
    return `
      <tr>
        <td>${pill(notification.type, tone)}</td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(notification.recipient || "-")}</strong>
            <span>${escapeHtml(notification.recipientMeta || "-")}</span>
          </div>
        </td>
        <td>
          <div class="cell-main">
            <strong>${escapeHtml(notification.sender || "System")}</strong>
            <span>${escapeHtml(notification.message || notification.senderMeta || "-")}</span>
          </div>
        </td>
        <td>${escapeHtml(notification.roomId || "-")}</td>
        <td>${escapeHtml(notification.sentAt ? formatDate(notification.sentAt) : "-")}</td>
        <td>${escapeHtml(notification.source)}</td>
        <td>
          <div class="row-actions">
            ${notification.canDelete ? `<button class="small-button danger" data-action="delete-notification" data-notice="${escapeHtml(notification.id)}" type="button">${icon("trash-2")}<span>Delete</span></button>` : ""}
          </div>
        </td>
      </tr>
    `;
  }).join("") : emptyRow(7, `No ${activeCategory.label.toLowerCase()} history matches the current search.`);
}

function renderLivechat() {
  const searchedThreads = getSupportThreads().filter((thread) => matchesSearch([
    thread.uid,
    thread.name,
    thread.email,
    thread.publicId,
    thread.topic,
    thread.status,
    thread.lastMessage
  ]));
  const allThreads = getSupportThreads();
  const selectedExists = state.selectedChatUid
    && allThreads.some((thread) => thread.uid === state.selectedChatUid);

  if (!selectedExists) {
    state.selectedChatUid = "";
  }

  els.livechatCount.textContent = `${searchedThreads.length} shown`;
  els.livechatThreadList.innerHTML = searchedThreads.length ? searchedThreads.map((thread) => {
    const active = thread.uid === state.selectedChatUid;
    const avatar = avatarHtml({
      name: thread.name,
      email: thread.email,
      photoUrl: thread.photoUrl,
      photoB64: thread.photoB64
    }, thread.uid);
    return `
      <button class="livechat-thread${active ? " is-active" : ""}" data-action="select-chat" data-chat-uid="${escapeHtml(thread.uid)}" type="button">
        ${avatar}
        <span class="livechat-thread-main">
          <span class="livechat-thread-head">
            <strong>${escapeHtml(thread.name)}</strong>
            ${pill(chatStatusLabel(thread.status), chatStatusTone(thread.status))}
          </span>
          ${thread.topic ? `<span class="cell-meta">${escapeHtml(thread.topic)}</span>` : ""}
          <span>${escapeHtml(thread.lastMessage || "No messages yet")}</span>
          <span class="cell-meta">${escapeHtml(thread.email || thread.uid)} - ${escapeHtml(ageLabel(thread.updatedAt))}</span>
        </span>
        ${thread.unread ? `<span class="livechat-unread" aria-label="Unread user reply"></span>` : ""}
      </button>
    `;
  }).join("") : `<div class="livechat-empty inline"><strong>No livechat threads</strong><span>App users will appear after sending a message.</span></div>`;

  const selectedThread = allThreads.find((thread) => thread.uid === state.selectedChatUid);
  if (!selectedThread) {
    els.livechatSelectedTitle.textContent = "Select a conversation";
    els.livechatSelectedSubtitle.textContent = "App users will appear here after they send a message.";
    els.livechatMessages.innerHTML = `
      <div class="livechat-empty">
        <strong>No conversation selected</strong>
        <span>Choose a livechat thread to reply.</span>
      </div>
    `;
    if (state.livechatAttachment) clearLivechatAttachment();
    updateLivechatComposer();
    els.resolveChatButton.disabled = true;
    els.deleteChatButton.disabled = true;
    return;
  }

  els.livechatSelectedTitle.textContent = selectedThread.name;
  els.livechatSelectedSubtitle.textContent = [
    selectedThread.topic ? `Topic: ${selectedThread.topic}` : "",
    selectedThread.email || selectedThread.uid,
    `${selectedThread.messageCount} messages`,
    chatStatusLabel(selectedThread.status)
  ].filter(Boolean).join(" - ");
  const isResolved = text(selectedThread.status).toLowerCase() === "resolved";
  els.resolveChatButton.disabled = isResolved;
  els.deleteChatButton.disabled = !isResolved;
  updateLivechatComposer();

  els.livechatMessages.innerHTML = selectedThread.messages.length ? selectedThread.messages.map((message) => {
    const fromAdmin = message.sender === "admin";
    const fromSystem = message.sender === "system";
    const fromAi = message.sender === "ai" || message.sender === "assistant";
    let sender = message.senderName || selectedThread.name;
    let badgeHtml = "";

    if (fromAdmin) {
      sender = (message.source === "admin-panel" || !message.senderName || message.senderName === "Admin") ? LIVECHAT_ADMIN_LABEL : message.senderName;
    } else if (fromSystem) {
      sender = "ResQTap System Notice";
      badgeHtml = ` <span class="badge-system-pill">System</span>`;
    } else if (fromAi) {
      sender = (message.senderName && message.senderName !== "ResQTap" && message.senderName !== "ResQTap Assistant" && message.senderName !== "ResQTap Support Chat Bot" && message.senderName !== "Admin") ? message.senderName : "ResQTap Support";
      badgeHtml = ` <span class="badge-ai-pill">AI</span>`;
    }

    const attachment = asRecord(message.attachment);
    const attachmentHtml = attachment.downloadUrl ? `
      ${text(attachment.downloadUrl).startsWith("data:image/")
        ? `<img class="livechat-image-preview" src="${escapeHtml(attachment.downloadUrl)}" alt="${escapeHtml(attachment.name || "Attachment")}">`
        : `<a class="livechat-attachment" href="${escapeHtml(attachment.downloadUrl)}" target="_blank" rel="noreferrer">
            ${icon("paperclip")}
            <span>${escapeHtml(attachment.name || "Attachment")}</span>
          </a>`}
    ` : "";

    const msgClass = fromAdmin ? "from-admin" : (fromSystem ? "from-system" : (fromAi ? "from-ai" : "from-user"));

    return `
      <div class="livechat-message ${msgClass}">
        <div class="livechat-bubble">
          <span class="livechat-sender">${escapeHtml(sender)}${badgeHtml}</span>
          ${message.text ? `<p>${escapeHtml(message.text)}</p>` : ""}
          ${attachmentHtml}
          <time>${escapeHtml(message.createdAt ? formatDate(message.createdAt) : "-")}</time>
        </div>
      </div>
    `;
  }).join("") : `
    <div class="livechat-empty">
      <strong>No messages yet</strong>
      <span>This thread is open but still empty.</span>
    </div>
  `;
  els.livechatMessages.scrollTop = els.livechatMessages.scrollHeight;
}

function aiChatStatusLabel(status) {
  const normalized = text(status).trim().toLowerCase();
  if (normalized === "expired") return "Expired";
  if (normalized === "answered") return "Answered";
  return "Active";
}

function aiChatStatusTone(status) {
  const normalized = text(status).trim().toLowerCase();
  if (normalized === "expired") return "warn";
  if (normalized === "answered") return "good";
  return "";
}

function renderAiChat() {
  const allThreads = getAiThreads();
  const searchedThreads = allThreads.filter((thread) => matchesSearch([
    thread.uid,
    thread.name,
    thread.email,
    thread.publicId,
    thread.status,
    thread.lastMessage
  ]));
  const selectedExists = state.selectedAiChatUid
    && allThreads.some((thread) => thread.uid === state.selectedAiChatUid);

  if (!selectedExists) {
    state.selectedAiChatUid = "";
  }

  els.aiChatCount.textContent = `${searchedThreads.length} shown`;
  els.aiChatThreadList.innerHTML = searchedThreads.length ? searchedThreads.map((thread) => {
    const active = thread.uid === state.selectedAiChatUid;
    const avatar = avatarHtml({
      name: thread.name,
      email: thread.email,
      photoUrl: thread.photoUrl,
      photoB64: thread.photoB64
    }, thread.uid);
    const expiryText = thread.expiresAt ? `Expires in ${futureAgeLabel(thread.expiresAt)}` : "No expiry";
    return `
      <button class="livechat-thread${active ? " is-active" : ""}" data-action="select-ai-chat" data-ai-chat-uid="${escapeHtml(thread.uid)}" type="button">
        ${avatar}
        <span class="livechat-thread-main">
          <span class="livechat-thread-head">
            <strong>${escapeHtml(thread.name)}</strong>
            ${pill(aiChatStatusLabel(thread.status), aiChatStatusTone(thread.status))}
          </span>
          <span>${escapeHtml(thread.lastMessage || "No messages yet")}</span>
          <span class="cell-meta">${escapeHtml(thread.email || thread.uid)} - ${escapeHtml(expiryText)}</span>
        </span>
        ${thread.unread ? `<span class="livechat-unread" aria-label="Waiting for AI reply"></span>` : ""}
      </button>
    `;
  }).join("") : `<div class="livechat-empty inline"><strong>No AI chat threads</strong><span>App users will appear after asking the assistant.</span></div>`;

  const selectedThread = allThreads.find((thread) => thread.uid === state.selectedAiChatUid);
  if (!selectedThread) {
    els.aiChatSelectedTitle.textContent = "Select a conversation";
    els.aiChatSelectedSubtitle.textContent = "App users will appear here after they ask the assistant.";
    els.aiChatMessages.innerHTML = `
      <div class="livechat-empty">
        <strong>No conversation selected</strong>
        <span>Choose an AI chat thread to monitor.</span>
      </div>
    `;
    els.deleteAiChatButton.disabled = true;
    return;
  }

  els.aiChatSelectedTitle.textContent = selectedThread.name;
  els.aiChatSelectedSubtitle.textContent = [
    selectedThread.email || selectedThread.uid,
    `${selectedThread.messageCount} messages`,
    aiChatStatusLabel(selectedThread.status),
    selectedThread.expiresAt ? `Expires ${formatDate(selectedThread.expiresAt)}` : ""
  ].filter(Boolean).join(" - ");
  els.deleteAiChatButton.disabled = false;

  els.aiChatMessages.innerHTML = selectedThread.messages.length ? selectedThread.messages.map((message) => {
    const fromAssistant = message.role === "assistant";
    const sender = fromAssistant ? "ResQTap AI" : (message.senderName || selectedThread.name);
    return `
      <div class="livechat-message ${fromAssistant ? "from-admin" : "from-user"}">
        <div class="livechat-bubble">
          <span class="livechat-sender">${escapeHtml(sender)}</span>
          <p>${escapeHtml(message.text)}</p>
          <time>${escapeHtml(message.createdAt ? formatDate(message.createdAt) : "-")}${message.source ? ` - ${escapeHtml(message.source)}` : ""}</time>
        </div>
      </div>
    `;
  }).join("") : `
    <div class="livechat-empty">
      <strong>No messages yet</strong>
      <span>This AI chat is open but still empty.</span>
    </div>
  `;
  els.aiChatMessages.scrollTop = els.aiChatMessages.scrollHeight;
}

function renderAdmins() {
  const admins = entries(state.admins)
    .map(([uid, value]) => ({
      uid,
      value,
      active: isAdminValue(value),
      grantedAt: isRecord(value) ? millis(value.grantedAt) : 0,
      grantedBy: isRecord(value) ? text(value.grantedBy) : ""
    }))
    .sort((a, b) => Number(b.active) - Number(a.active) || a.uid.localeCompare(b.uid));

  els.adminsCount.textContent = `${admins.length} records`;
  els.adminsList.innerHTML = admins.length ? admins.map((admin) => `
    <article class="list-item">
      <div>
        <strong>${escapeHtml(userName(admin.uid))}</strong>
        <span>${escapeHtml(admin.uid)}</span>
        <span>${admin.grantedAt ? `Granted ${escapeHtml(formatDate(admin.grantedAt))}` : "Manual record"}</span>
      </div>
      <div class="row-actions">
        ${pill(admin.active ? "Active" : "Inactive", admin.active ? "good" : "warn")}
        <button class="small-button danger" data-action="revoke-admin" data-uid="${escapeHtml(admin.uid)}" type="button">${icon("shield-x")}<span>Revoke</span></button>
      </div>
    </article>
  `).join("") : `<div class="list-item"><span>No admin records loaded.</span></div>`;
}

function renderHighlights() {
  const hlGrid = document.getElementById("highlightsGrid");
  const hlCount = document.getElementById("highlightsCount");
  if (!hlGrid) return;

  const items = entries(state.highlights)
    .map(([id, value]) => ({
      id,
      ...asRecord(value)
    }))
    .sort((a, b) => (Number(a.order) || 99) - (Number(b.order) || 99));

  if (hlCount) hlCount.textContent = `${items.length} banner${items.length === 1 ? "" : "s"}`;

  if (!items.length) {
    hlGrid.innerHTML = `
      <div class="empty-highlights-state">
        <i data-lucide="sparkles" style="width: 36px; height: 36px; color: #E60067; margin-bottom: 10px;"></i>
        <strong style="display: block; font-size: 16px; font-weight: 800; color: #1C1E21; margin-bottom: 6px;">No Highlights Found</strong>
        <p class="muted" style="margin-bottom: 18px; font-size: 13px; max-width: 480px; margin-left: auto; margin-right: auto;">The carousel database is currently empty. You can create a new banner above or load the default official emergency banners.</p>
        <button class="primary-button icon-button" data-action="seed-default-highlights" type="button" style="margin: 0 auto;">
          <i data-lucide="upload-cloud" aria-hidden="true"></i>
          <span>Load 4 Official Banners to Firebase</span>
        </button>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  hlGrid.innerHTML = items.map((item) => {
    const isActive = item.active !== false && item.active !== "false";
    const imgSrc = item.imageUrl || "assets/resqtap_launcher.png";
    return `
      <div class="highlight-item-card${isActive ? "" : " is-hidden"}">
        <div class="highlight-card-media">
          <img src="${escapeHtml(imgSrc)}" alt="${escapeHtml(item.title || "Banner")}">
          <div class="highlight-badges-row">
            <span class="hl-badge-order">#${escapeHtml(String(item.order || 1))}</span>
          </div>
        </div>
        <div class="highlight-card-body">
          <strong class="highlight-card-title">${escapeHtml(item.title || "Untitled Banner")}</strong>
          ${item.actionUrl ? `
            <a class="highlight-card-link" href="${escapeHtml(item.actionUrl)}" target="_blank" rel="noreferrer">
              <i data-lucide="external-link" style="width: 12px; height: 12px; flex-shrink: 0;"></i>
              <span>${escapeHtml(item.actionUrl)}</span>
            </a>
          ` : '<span class="highlight-card-nolink">No web link attached</span>'}

          <div class="highlight-card-actions">
            <button class="action-pill-btn ${isActive ? "muted" : "success"}" data-action="toggle-highlight" data-highlight-id="${escapeHtml(item.id)}" data-active="${isActive ? 'true' : 'false'}" type="button">
              ${icon(isActive ? "eye-off" : "eye")}
              <span>${isActive ? "Hide" : "Show"}</span>
            </button>
            <button class="action-pill-btn danger" data-action="delete-highlight" data-highlight-id="${escapeHtml(item.id)}" type="button">
              ${icon("trash-2")}
              <span>Delete</span>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join("");

  if (window.lucide) window.lucide.createIcons();
}

function detailPanelMaxWidth() {
  const shellWidth = els.appShell.getBoundingClientRect().width || window.innerWidth;
  const sidebarWidth = window.matchMedia("(max-width: 1320px)").matches ? 208 : 216;
  const availableWidth = shellWidth - sidebarWidth - DETAIL_MAIN_MIN_WIDTH;
  return Math.max(DETAIL_PANEL_MIN_WIDTH, Math.min(DETAIL_PANEL_MAX_WIDTH, availableWidth));
}

function setDetailWidth(width) {
  const nextWidth = Math.round(Math.min(Math.max(width, DETAIL_PANEL_MIN_WIDTH), detailPanelMaxWidth()));
  state.detailWidth = nextWidth;
  els.appShell.style.setProperty("--detail-width", `${nextWidth}px`);
}

function detailResizeHandle() {
  return `
    <div class="detail-resize-handle" role="separator" aria-orientation="vertical" aria-label="Resize detail panel" tabindex="0"></div>
  `;
}

function detailShell(title, subtitle, body, avatar = "") {
  setDetailWidth(state.detailWidth);
  els.appShell.classList.add("detail-open");
  els.detailPanel.classList.add("has-content");
  els.detailPanel.classList.remove("hidden");
  els.detailPanel.innerHTML = `
    ${detailResizeHandle()}
    <div class="detail-content">
      <div class="detail-header">
        <div class="detail-title">
          ${avatar}
          <div>
            <h3>${escapeHtml(title)}</h3>
            <span class="muted">${escapeHtml(subtitle)}</span>
          </div>
        </div>
        <button class="secondary-button icon-button" data-action="clear-detail" type="button">${icon("x")}<span>Close</span></button>
      </div>
      ${body}
    </div>
  `;
}

function kv(label, value) {
  return `<div class="kv"><span>${escapeHtml(label)}</span><span>${escapeHtml(value || "-")}</span></div>`;
}

function renderDetail() {
  if (!state.selected) {
    els.appShell.classList.remove("detail-open");
    els.detailPanel.classList.remove("has-content");
    els.detailPanel.classList.add("hidden");
    els.detailPanel.innerHTML = `
      <div class="detail-empty">
        <strong>No item selected</strong>
        <span>Select a row to inspect details.</span>
      </div>
    `;
    return;
  }

  if (state.selected.type === "user") renderUserDetail(state.selected.id);
  if (state.selected.type === "room") renderRoomDetail(state.selected.id);
  if (state.selected.type === "sos") renderSosDetail(state.selected.roomId, state.selected.alertId);
  if (state.selected.type === "report") renderReportDetail(state.selected.id);
}

function renderUserDetail(uid) {
  const user = asRecord(state.users[uid]);
  if (!Object.keys(user).length) {
    state.selected = null;
    renderDetail();
    return;
  }
  const roomEntries = entries(userRoomsFor(uid));
  const contactEntries = contactsFor(user);
  const locations = getRooms()
    .map((room) => ({ room, member: room.members.find((item) => item.uid === uid) }))
    .filter((item) => item.member);

  const contactHtml = contactEntries.length ? contactEntries.map(([, contact]) => {
    const c = asRecord(contact);
    return `
      <div class="contact-row">
        <strong>${escapeHtml(c.name || "-")}</strong>
        <span class="cell-meta">${escapeHtml(c.relationship || "-")} - ${escapeHtml(c.phone || "-")}</span>
      </div>
    `;
  }).join("") : `<span class="muted">No emergency contacts.</span>`;

  const roomsHtml = roomEntries.length ? roomEntries.map(([roomCode, meta]) => {
    const m = asRecord(meta);
    return `
      <div class="member-row">
        <div class="member-row-head">
          <strong>${escapeHtml(m.label || roomCode)}</strong>
          <button class="small-button" data-action="view-room" data-room="${escapeHtml(roomCode)}" type="button">${icon("external-link")}<span>Open</span></button>
        </div>
        <span class="cell-meta">${escapeHtml(roomCode)} - ${escapeHtml(m.role || "member")}</span>
      </div>
    `;
  }).join("") : `<span class="muted">No rooms.</span>`;

  const locationsHtml = locations.length ? locations.map(({ room, member }) => {
    const hasMap = Number.isFinite(member.lat) && Number.isFinite(member.lng) && member.lat !== 0 && member.lng !== 0;
    return `
      <div class="member-row">
        <div class="member-row-head">
          <strong>${escapeHtml(room.code)}</strong>
        ${hasMap ? `<a class="small-button" href="https://www.google.com/maps?q=${member.lat},${member.lng}" target="_blank" rel="noreferrer">${icon("map-pin")}<span>Map</span></a>` : ""}
        </div>
        <span class="cell-meta">${escapeHtml(ageLabel(member.updatedAt))} - Battery ${member.batteryPct === null ? "-" : `${member.batteryPct}%`}</span>
      </div>
    `;
  }).join("") : `<span class="muted">No online room location.</span>`;

  const body = `
    <section class="detail-section">
      ${kv("UID", uid)}
      ${kv("Public ID", user.publicId)}
      ${kv("Email", user.email)}
      ${kv("Phone", user.phoneNumber)}
      ${kv("Last seen", ageLabel(getUserLastSeen(uid, user)))}
    </section>
    <section class="detail-section">
      ${kv("Blood type", user.bloodType)}
      ${kv("Allergies", user.allergies)}
      ${kv("Conditions", user.existingConditions)}
      ${kv("Date of birth", user.dateOfBirth)}
      ${kv("Address", user.address)}
    </section>
    <section class="detail-section">
      <h3>Emergency Contacts</h3>
      ${contactHtml}
    </section>
    <section class="detail-section">
      <h3>Rooms</h3>
      ${roomsHtml}
    </section>
    <section class="detail-section">
      <h3>Online Locations</h3>
      ${locationsHtml}
    </section>
  `;
  detailShell(user.name || user.email || "User", uid, body, avatarHtml(user, uid));
}

function renderRoomDetail(roomCode) {
  const room = asRecord(state.rooms[roomCode]);
  if (!Object.keys(room).length) {
    state.selected = null;
    renderDetail();
    return;
  }

  const roomModel = getRooms().find((item) => item.code === roomCode);
  const members = roomModel ? roomModel.members : [];
  const alerts = getSosAlerts().filter((alert) => alert.roomId === roomCode && alert.source === "room");
  const memberHtml = members.length ? members.map((member) => {
    const hasMap = Number.isFinite(member.lat) && Number.isFinite(member.lng) && member.lat !== 0 && member.lng !== 0;
    const battery = member.batteryPct === null ? "-" : `${member.batteryPct}%`;
    const batteryTone = member.batteryPct !== null && member.batteryPct <= LOW_BATTERY ? "warn" : "";
    return `
      <div class="member-row">
        <div class="member-row-head">
          <strong>${escapeHtml(member.data.name || userName(member.uid))}</strong>
          <div class="row-actions">
            ${pill(Date.now() - member.updatedAt <= ONLINE_MS ? "Online" : ageLabel(member.updatedAt), Date.now() - member.updatedAt <= ONLINE_MS ? "good" : "")}
            ${hasMap ? `<a class="small-button" href="https://www.google.com/maps?q=${member.lat},${member.lng}" target="_blank" rel="noreferrer">${icon("map-pin")}<span>Map</span></a>` : ""}
            <button class="small-button danger" data-action="remove-member" data-room="${escapeHtml(roomCode)}" data-uid="${escapeHtml(member.uid)}" type="button">${icon("user-minus")}<span>Remove</span></button>
          </div>
        </div>
        <span class="cell-meta">${escapeHtml(member.uid)} - Battery ${pill(battery, batteryTone)}</span>
      </div>
    `;
  }).join("") : `<span class="muted">No members in this room.</span>`;

  const alertHtml = alerts.length ? alerts.slice(0, 8).map((alert) => {
    const label = alert.active ? (alert.stale ? "Stale" : "Active") : "Cancelled";
    const tone = alert.active ? (alert.stale ? "warn" : "alert") : "good";
    return `
      <div class="member-row">
        <div class="member-row-head">
          <strong>${escapeHtml(alert.senderName || alert.senderUid || "Unknown")}</strong>
          <button class="small-button" data-action="view-sos" data-room="${escapeHtml(roomCode)}" data-alert="${escapeHtml(alert.key)}" type="button">${icon("external-link")}<span>Open</span></button>
        </div>
        <span class="cell-meta">${pill(label, tone)} ${escapeHtml(formatDate(alert.createdAt))}</span>
      </div>
    `;
  }).join("") : `<span class="muted">No SOS alerts in this room.</span>`;

  const body = `
    <section class="detail-section">
      ${kv("Code", roomCode)}
      ${kv("Name", room.name || roomCode)}
      ${kv("Creator", `${userName(room.creatorUid)} (${room.creatorUid || "-"})`)}
      ${kv("Created", formatDate(room.createdAt))}
      ${kv("Updated", ageLabel(roomModel ? roomModel.updatedAt : room.updatedAt))}
      ${kv("Members", String(members.length))}
    </section>
    <section class="detail-section">
      <h3>Members</h3>
      ${memberHtml}
    </section>
    <section class="detail-section">
      <h3>SOS Alerts</h3>
      ${alertHtml}
    </section>
    <section class="detail-section">
      <button class="secondary-button danger icon-button" data-action="delete-room" data-room="${escapeHtml(roomCode)}" type="button">${icon("trash-2")}<span>Delete room</span></button>
    </section>
  `;
  detailShell(room.name || roomCode, `Room ${roomCode}`, body);
}

function renderSosDetail(roomId, alertId) {
  const alert = getSosAlerts().find((item) => item.roomId === roomId && item.key === alertId);
  if (!alert) {
    state.selected = null;
    renderDetail();
    return;
  }

  const label = alert.active ? (alert.stale ? "Stale active" : "Active") : "Cancelled";
  const canCancel = alert.source === "room" && alert.active;
  const body = `
    <section class="detail-section">
      ${kv("Alert ID", alert.id)}
      ${kv("Room", alert.roomId)}
      ${kv("Source", alert.source)}
      ${kv("Status", label)}
      ${kv("Created", formatDate(alert.createdAt))}
      ${kv("Cancelled", alert.cancelledAt ? formatDate(alert.cancelledAt) : "-")}
      ${kv("Sender UID", alert.senderUid)}
      ${kv("Sender name", alert.senderName)}
    </section>
    <section class="detail-section">
      <button class="secondary-button icon-button" data-action="view-room" data-room="${escapeHtml(alert.roomId)}" type="button">${icon("external-link")}<span>Open room</span></button>
      ${alert.senderUid ? `<button class="secondary-button icon-button" data-action="view-user" data-uid="${escapeHtml(alert.senderUid)}" type="button">${icon("user-round")}<span>Open sender</span></button>` : ""}
      ${canCancel ? `<button class="secondary-button danger icon-button" data-action="cancel-sos" data-room="${escapeHtml(alert.roomId)}" data-alert="${escapeHtml(alert.key)}" type="button">${icon("circle-x")}<span>Cancel SOS</span></button>` : ""}
    </section>
  `;
  detailShell(alert.senderName || alert.senderUid || "SOS Alert", `${alert.roomId || "-"} - ${label}`, body);
}

function renderReportDetail(reportKey) {
  const report = getIncidentReports().find((item) => item.key === reportKey);
  if (!report) {
    state.selected = null;
    renderDetail();
    return;
  }

  const status = reportStatusMeta(report.status);
  const hasMap = Number.isFinite(report.latitude) && Number.isFinite(report.longitude);
  const mapUrl = hasMap ? `https://www.google.com/maps?q=${report.latitude},${report.longitude}` : "";
  const attachmentsHtml = report.attachments.length ? report.attachments.map((attachment, index) => {
    const name = text(attachment.name || "Attachment");
    const mime = text(attachment.mimeType).toLowerCase();
    const url = text(attachment.downloadUrl);
    const isImage = mime.startsWith("image/") || url.startsWith("data:image/");
    if (!url) {
      return `
        <div class="report-attachment-card">
          ${icon("paperclip")}
          <span>${escapeHtml(name)}</span>
        </div>
      `;
    }
    return `
      <button class="report-attachment-card report-attachment-button" data-action="view-report-attachment" data-report="${escapeHtml(report.key)}" data-attachment-index="${index}" type="button">
        ${isImage ? `<img class="report-attachment-preview" src="${escapeHtml(url)}" alt="">` : icon("paperclip")}
        <span>${escapeHtml(name)}</span>
        <em>View</em>
      </button>
    `;
  }).join("") : `<span class="muted">No attachments.</span>`;

  const body = `
    <section class="detail-section">
      ${kv("Report ID", report.id)}
      ${kv("Status", status.label)}
      ${kv("Category", report.categoryLabel)}
      ${kv("Created", formatDate(report.createdAt))}
      ${kv("Updated", report.updatedAt ? formatDate(report.updatedAt) : "-")}
      ${kv("Source", report.data.source || "android_app")}
    </section>
    <section class="detail-section">
      <h3>Reporter</h3>
      ${kv("Name", report.senderName)}
      ${kv("Email", report.senderEmail)}
      ${kv("UID", report.senderUid)}
      ${kv("Public ID", report.publicId)}
      ${report.senderUid ? `<button class="secondary-button icon-button" data-action="view-user" data-uid="${escapeHtml(report.senderUid)}" type="button">${icon("user-round")}<span>Open reporter</span></button>` : ""}
      ${report.senderUid ? `<button class="secondary-button icon-button" data-action="call-reporter" data-uid="${escapeHtml(report.senderUid)}" data-name="${escapeHtml(report.senderName || report.senderEmail || "Reporter")}" type="button">${icon("phone-call")}<span>Call Reporter</span></button>` : ""}
    </section>
    <section class="detail-section">
      <h3>Location</h3>
      ${kv("Address", report.address)}
      ${kv("Coordinates", hasMap ? `${report.latitude}, ${report.longitude}` : "-")}
      ${mapUrl ? `<a class="secondary-button icon-button" href="${escapeHtml(mapUrl)}" target="_blank" rel="noreferrer">${icon("map-pin")}<span>Open map</span></a>` : ""}
    </section>
    <section class="detail-section">
      <h3>Details</h3>
      <p class="detail-text">${escapeHtml(report.details || "No additional details.")}</p>
    </section>
    <section class="detail-section">
      <h3>Attachments</h3>
      <div class="report-attachment-grid">${attachmentsHtml}</div>
    </section>
    <section class="detail-section">
      <h3>Status Actions</h3>
      <div class="row-actions">
        ${REPORT_STATUSES.map((option) => `
          <button class="small-button${option.id === report.status ? " is-current" : ""}" data-action="set-report-status" data-report="${escapeHtml(report.key)}" data-status="${escapeHtml(option.id)}" type="button">
            ${icon(option.id === "resolved" ? "check-circle-2" : option.id === "rejected" ? "circle-x" : option.id === "reviewing" ? "clock" : "sparkles")}
            <span>${escapeHtml(option.label)}</span>
          </button>
        `).join("")}
        <button class="small-button danger" data-action="delete-report" data-report="${escapeHtml(report.key)}" type="button">${icon("trash-2")}<span>Delete</span></button>
      </div>
    </section>
  `;
  detailShell(report.categoryLabel || "Incident Report", `${status.label} - ${formatDate(report.createdAt)}`, body, avatarHtml(report.data, report.senderUid));
}

function attachmentViewerEls() {
  return {
    modal: document.getElementById("attachmentViewerModal"),
    title: document.getElementById("attachmentViewerTitle"),
    meta: document.getElementById("attachmentViewerMeta"),
    body: document.getElementById("attachmentViewerBody"),
    openLink: document.getElementById("attachmentViewerOpen")
  };
}

function closeAttachmentViewer() {
  const viewer = attachmentViewerEls();
  if (!viewer.modal || !viewer.body) return;
  viewer.modal.classList.add("hidden");
  viewer.body.innerHTML = "";
  if (viewer.openLink) viewer.openLink.removeAttribute("href");
}

function viewReportAttachment(reportKey, attachmentIndex) {
  const report = getIncidentReports().find((item) => item.key === reportKey);
  const attachment = report && report.attachments[Number(attachmentIndex)];
  const url = text(attachment && attachment.downloadUrl);
  if (!attachment || !url) {
    showToast("Attachment is not available.");
    return;
  }

  const name = text(attachment.name || "Attachment");
  const mime = text(attachment.mimeType).toLowerCase();
  const viewer = attachmentViewerEls();
  if (!viewer.modal || !viewer.body) {
    window.open(url, "_blank", "noopener,noreferrer");
    return;
  }

  viewer.title.textContent = name;
  viewer.meta.textContent = mime || "attachment";
  viewer.body.innerHTML = "";
  if (viewer.openLink) {
    viewer.openLink.href = url;
    viewer.openLink.download = name;
  }

  const isImage = mime.startsWith("image/") || url.startsWith("data:image/");
  const isVideo = mime.startsWith("video/");
  const isAudio = mime.startsWith("audio/");
  const isPdf = mime === "application/pdf" || name.toLowerCase().endsWith(".pdf");

  if (isImage) {
    const image = document.createElement("img");
    image.className = "attachment-viewer-image";
    image.alt = name;
    image.src = url;
    viewer.body.appendChild(image);
  } else if (isVideo) {
    const video = document.createElement("video");
    video.className = "attachment-viewer-media";
    video.controls = true;
    video.src = url;
    viewer.body.appendChild(video);
  } else if (isAudio) {
    const audio = document.createElement("audio");
    audio.className = "attachment-viewer-audio";
    audio.controls = true;
    audio.src = url;
    viewer.body.appendChild(audio);
  } else if (isPdf) {
    const frame = document.createElement("iframe");
    frame.className = "attachment-viewer-frame";
    frame.title = name;
    frame.src = url;
    viewer.body.appendChild(frame);
  } else {
    const empty = document.createElement("div");
    empty.className = "attachment-viewer-fallback";
    empty.innerHTML = `
      ${icon("paperclip")}
      <strong>${escapeHtml(name)}</strong>
      <span>Preview is not available for this file type. Open it in a new tab.</span>
    `;
    viewer.body.appendChild(empty);
    refreshIcons();
  }

  viewer.modal.classList.remove("hidden");
}

function setActiveView(view) {
  if (view !== "livechat") clearLivechatAdminTyping();

  const vcModal = document.getElementById("videoCallModal");
  if (vcModal && !vcModal.classList.contains("hidden") && !vcModal.classList.contains("floating")) {
    vcModal.classList.add("floating");
  }

  state.activeView = view;
  render();
}

function updateNotificationComposer() {
  const length = els.notificationMessageInput.value.length;
  els.notificationCharCount.textContent = `${length}/180`;
  els.sendNotificationButton.disabled = !els.notificationMessageInput.value.trim();
  els.clearNotificationButton.disabled = !els.notificationTitleInput.value.trim()
    && !els.notificationMessageInput.value.trim();
}

function clearNotificationComposer() {
  els.notificationAudienceInput.value = "all";
  els.notificationTitleInput.value = "";
  els.notificationMessageInput.value = "";
  updateNotificationComposer();
  els.notificationMessageInput.focus();
}

async function handleNoticeSubmit(event) {
  event.preventDefault();
  const message = els.notificationMessageInput.value.trim();
  const title = els.notificationTitleInput.value.trim();
  const audience = els.notificationAudienceInput.value || "all";
  if (!message) {
    showToast("Enter a notification message.");
    updateNotificationComposer();
    return;
  }

  els.sendNotificationButton.disabled = true;
  try {
    const noticeRef = push(ref(db, "admin_notifications"));
    const noticeId = noticeRef.key;
    if (!noticeId) throw new Error("Unable to create notice id.");

    const sentAt = serverTimestamp();
    const baseNotice = {
      audience,
      audienceLabel: audienceLabel(audience),
      title,
      message,
      sentBy: state.currentUser.uid,
      sentAt,
      status: "published",
      method: NOTICE_METHOD,
      source: NOTICE_SOURCE
    };
    const updates = {
      [`admin_notifications/${noticeId}`]: {
        ...baseNotice,
        successCount: 1,
        failureCount: 0
      },
      "broadcastNotifications/current": {
        ...baseNotice,
        id: noticeId,
        active: true
      },
      [`broadcastNotifications/history/${noticeId}`]: {
        ...baseNotice,
        id: noticeId,
        active: true
      }
    };

    await update(ref(db), updates);
    els.notificationTitleInput.value = "";
    els.notificationMessageInput.value = "";
    updateNotificationComposer();
    showToast("Notification published.");
  } catch (error) {
    console.error(error);
    showToast(error.message || "Unable to publish notice.");
  } finally {
    updateNotificationComposer();
  }
}

function currentAdminName() {
  return LIVECHAT_ADMIN_LABEL;
}

function setLivechatAttachMenu(open) {
  const shouldOpen = Boolean(open && state.selectedChatUid && !state.livechatSending);
  els.livechatAttachMenu.classList.toggle("hidden", !shouldOpen);
  els.livechatAttachButton.setAttribute("aria-expanded", shouldOpen ? "true" : "false");
}

function renderLivechatAttachment() {
  const attachment = state.livechatAttachment;
  els.livechatAttachmentLabel.classList.toggle("hidden", !attachment);
  if (!attachment) {
    els.livechatAttachmentLabel.innerHTML = "";
    return;
  }

  els.livechatAttachmentLabel.innerHTML = `
    <span>Attached: <strong>${escapeHtml(attachment.name)}</strong></span>
    <button data-livechat-clear-attachment type="button" aria-label="Remove attachment">&times;</button>
  `;
}

function clearLivechatAttachment() {
  state.livechatAttachment = null;
  state.livechatAttachmentKind = "";
  els.livechatAttachmentInput.value = "";
  renderLivechatAttachment();
  updateLivechatComposer();
}

function updateLivechatComposer() {
  const hasThread = Boolean(state.selectedChatUid);
  const hasText = Boolean(els.livechatReplyInput.value.trim());
  const hasAttachment = Boolean(state.livechatAttachment);
  els.livechatReplyInput.disabled = !hasThread || state.livechatSending;
  els.livechatAttachButton.disabled = !hasThread || state.livechatSending;
  els.livechatReplyButton.disabled = !hasThread || state.livechatSending || (!hasText && !hasAttachment);
  if (!hasThread || state.livechatSending) setLivechatAttachMenu(false);
  renderLivechatAttachment();
}

function clearLivechatTypingTimer() {
  if (state.livechatTypingTimer) {
    window.clearTimeout(state.livechatTypingTimer);
    state.livechatTypingTimer = null;
  }
}

async function writeLivechatTyping(active, uid = state.selectedChatUid) {
  if (!validPathSegment(uid)) return;
  const updates = {
    adminTyping: Boolean(active),
    adminTypingAt: serverTimestamp(),
    adminTypingBy: state.currentUser ? state.currentUser.uid : "",
    adminTypingName: LIVECHAT_ADMIN_LABEL
  };
  await update(ref(db, `supportChats/${uid}/meta`), updates);
}

function clearLivechatAdminTyping(uid = state.livechatTypingUid || state.selectedChatUid) {
  clearLivechatTypingTimer();
  if (!state.livechatTypingActive && !state.livechatTypingUid) return;
  const targetUid = uid;
  state.livechatTypingActive = false;
  state.livechatTypingUid = "";
  state.livechatTypingLastSentAt = 0;
  writeLivechatTyping(false, targetUid).catch((error) => console.warn(error));
}

function handleLivechatTypingInput() {
  updateLivechatComposer();
  const uid = state.selectedChatUid;
  const hasText = Boolean(els.livechatReplyInput.value);
  if (!validPathSegment(uid) || state.livechatSending || !hasText) {
    clearLivechatAdminTyping(uid);
    return;
  }

  const now = Date.now();
  state.livechatTypingUid = uid;
  if (!state.livechatTypingActive || now - state.livechatTypingLastSentAt >= LIVECHAT_TYPING_REFRESH_MS) {
    state.livechatTypingActive = true;
    state.livechatTypingLastSentAt = now;
    writeLivechatTyping(true, uid).catch((error) => console.warn(error));
  }

  clearLivechatTypingTimer();
  state.livechatTypingTimer = window.setTimeout(() => {
    clearLivechatAdminTyping(uid);
  }, LIVECHAT_TYPING_IDLE_MS);
}

function chooseLivechatAttachment(kind) {
  const option = LIVECHAT_ATTACHMENT_OPTIONS[kind];
  if (!option) return;

  state.livechatAttachmentKind = kind;
  els.livechatAttachmentInput.value = "";
  els.livechatAttachmentInput.accept = option.accept || "";
  if (option.capture) {
    els.livechatAttachmentInput.setAttribute("capture", option.capture);
  } else {
    els.livechatAttachmentInput.removeAttribute("capture");
  }
  els.livechatAttachmentInput.click();
}

function handleLivechatAttachmentSelected() {
  const file = els.livechatAttachmentInput.files && els.livechatAttachmentInput.files[0];
  if (!file) return;
  state.livechatAttachment = {
    file,
    kind: state.livechatAttachmentKind,
    name: file.name || "attachment",
    mimeType: file.type || "application/octet-stream",
    size: file.size || 0
  };
  setLivechatAttachMenu(false);
  updateLivechatComposer();
}

async function buildLivechatAttachmentPayload(uid, messageId) {
  const attachment = state.livechatAttachment;
  if (!attachment || !attachment.file) return null;

  if (canInlineImage(attachment.file)) {
    return {
      name: attachment.name,
      mimeType: "image/jpeg",
      size: attachment.size,
      downloadUrl: await imageFileToInlineDataUrl(attachment.file)
    };
  }

  const adminUid = state.currentUser && state.currentUser.uid;
  if (!validPathSegment(adminUid)) throw new Error("Admin account is not ready.");
  const safeName = safeStorageName(attachment.name);
  const target = storageRef(
    storage,
    `supportChatAdminUploads/${adminUid}/${uid}/${messageId}/${Date.now()}_${safeName}`
  );
  await uploadBytes(target, attachment.file, {
    contentType: attachment.mimeType || "application/octet-stream"
  });
  return {
    name: attachment.name,
    mimeType: attachment.mimeType || "application/octet-stream",
    size: attachment.size || 0,
    downloadUrl: await getDownloadURL(target)
  };
}

async function sendAdminLivechatReply(event) {
  event.preventDefault();
  const uid = state.selectedChatUid;
  const message = els.livechatReplyInput.value.trim();
  const attachment = state.livechatAttachment;
  if (!validPathSegment(uid)) {
    showToast("Select a livechat thread first.");
    return;
  }
  if (!message && !attachment) {
    showToast("Type a reply or attach a file first.");
    updateLivechatComposer();
    return;
  }

  state.livechatSending = true;
  clearLivechatAdminTyping(uid);
  updateLivechatComposer();
  try {
    const messageRef = push(ref(db, `supportChats/${uid}/messages`));
    const messageId = messageRef.key;
    if (!messageId) throw new Error("Unable to create message id.");

    const adminName = currentAdminName();
    const attachmentPayload = await buildLivechatAttachmentPayload(uid, messageId);
    const chatMessage = {
      text: message,
      sender: "admin",
      senderUid: state.currentUser.uid,
      senderName: adminName,
      createdAt: serverTimestamp(),
      source: "admin-panel"
    };
    if (attachmentPayload) chatMessage.attachment = attachmentPayload;

    const lastMessage = message || `Attachment: ${attachmentPayload.name || "file"}`;
    await update(ref(db), {
      [`supportChats/${uid}/messages/${messageId}`]: chatMessage,
      [`supportChats/${uid}/meta/status`]: "answered",
      [`supportChats/${uid}/meta/lastMessage`]: lastMessage,
      [`supportChats/${uid}/meta/lastSender`]: "admin",
      [`supportChats/${uid}/meta/lastAdminUid`]: state.currentUser.uid,
      [`supportChats/${uid}/meta/lastAdminName`]: adminName,
      [`supportChats/${uid}/meta/updatedAt`]: serverTimestamp()
    });
    els.livechatReplyInput.value = "";
    clearLivechatAttachment();
    updateLivechatComposer();
    showToast("Livechat reply sent.");
  } catch (error) {
    console.error(error);
    showToast(error.message || "Unable to send livechat reply.");
  } finally {
    state.livechatSending = false;
    updateLivechatComposer();
  }
}

async function resolveSelectedChat() {
  const uid = state.selectedChatUid;
  if (!validPathSegment(uid)) {
    showToast("Select a livechat thread first.");
    return;
  }

  await update(ref(db, `supportChats/${uid}/meta`), {
    status: "resolved",
    resolvedAt: serverTimestamp(),
    resolvedBy: state.currentUser.uid,
    updatedAt: serverTimestamp()
  });
  showToast("Livechat marked resolved.");
}

async function claimLivechatThread(uid) {
  if (!validPathSegment(uid)) return;
  const thread = asRecord(state.supportChats[uid]);
  const meta = asRecord(thread.meta);
  if (millis(meta.claimedAt)) return;

  await update(ref(db, `supportChats/${uid}/meta`), {
    claimedAt: serverTimestamp(),
    claimedBy: state.currentUser.uid,
    claimedByName: LIVECHAT_ADMIN_LABEL,
    claimNotice: LIVECHAT_CLAIM_NOTICE
  });
}

async function deleteSelectedResolvedChat() {
  const uid = state.selectedChatUid;
  if (!validPathSegment(uid)) {
    showToast("Select a livechat thread first.");
    return;
  }

  const thread = getSupportThreads().find((item) => item.uid === uid);
  if (!thread || text(thread.status).toLowerCase() !== "resolved") {
    showToast("Resolve the livechat before deleting it.");
    return;
  }

  if (!window.confirm(`Delete resolved livechat for ${thread.name}?`)) return;
  await remove(ref(db, `supportChats/${uid}`));
  state.selectedChatUid = "";
  showToast("Resolved livechat deleted.");
}

async function deleteSelectedAiChat() {
  const uid = state.selectedAiChatUid;
  if (!validPathSegment(uid)) {
    showToast("Select an AI chat thread first.");
    return;
  }

  const thread = getAiThreads().find((item) => item.uid === uid);
  if (!thread) {
    state.selectedAiChatUid = "";
    renderAiChat();
    return;
  }

  if (!window.confirm(`Reset AI chat for ${thread.name}?`)) return;
  await update(ref(db), {
    [`aiChats/${uid}`]: null,
    [`supportChats/${uid}/aiAssistant`]: null
  });
  state.selectedAiChatUid = "";
  showToast("AI chat reset.");
}

async function cleanupExpiredAiChats() {
  if (!state.currentUser || state.aiChatCleanupRunning) return;
  const expired = getAiThreads().filter((thread) => (
    thread.expiresAt > 0 && thread.expiresAt <= Date.now()
  ));
  if (!expired.length) return;

  state.aiChatCleanupRunning = true;
  try {
    const updates = {};
    expired.forEach((thread) => {
      if (!validPathSegment(thread.uid)) return;
      updates[`aiChats/${thread.uid}`] = null;
      updates[`supportChats/${thread.uid}/aiAssistant`] = null;
    });
    if (!Object.keys(updates).length) return;
    await update(ref(db), updates);
    if (expired.some((thread) => thread.uid === state.selectedAiChatUid)) {
      state.selectedAiChatUid = "";
    }
  } finally {
    state.aiChatCleanupRunning = false;
  }
}

async function cancelSos(roomId, alertId) {
  if (!validPathSegment(roomId) || !validPathSegment(alertId)) {
    showToast("Invalid SOS path.");
    return;
  }
  if (!window.confirm(`Cancel SOS alert ${alertId}?`)) return;
  await update(ref(db, `rooms/${roomId}/sosAlerts/${alertId}`), {
    status: "cancelled",
    cancelledAt: serverTimestamp(),
    cancelledByAdmin: state.currentUser.uid
  });
  showToast("SOS alert cancelled.");
}

async function deleteNotification(noticeId) {
  if (!validPathSegment(noticeId)) {
    showToast("Invalid notification path.");
    return;
  }
  if (!window.confirm(`Delete notification ${noticeId}?`)) return;

  const currentSnapshot = await get(ref(db, "broadcastNotifications/current/id"));
  const updates = {
    [`admin_notifications/${noticeId}`]: null,
    [`broadcastNotifications/history/${noticeId}`]: null
  };
  if (text(currentSnapshot.val()).trim() === noticeId) {
    updates["broadcastNotifications/current"] = null;
  }

  await update(ref(db), updates);
  showToast("Notification deleted.");
}

async function removeMember(roomId, uid) {
  if (!validPathSegment(roomId) || !validPathSegment(uid)) {
    showToast("Invalid member path.");
    return;
  }
  if (!window.confirm(`Remove ${userName(uid)} from room ${roomId}?`)) return;
  const updates = {};
  updates[`rooms/${roomId}/forceLeave/${uid}`] = {
    at: serverTimestamp(),
    by: state.currentUser.uid,
    source: "admin-panel"
  };
  updates[`rooms/${roomId}/members/${uid}`] = null;
  updates[`rooms/${roomId}/bells/${uid}`] = null;
  updates[`userRooms/${uid}/${roomId}`] = null;
  await update(ref(db), updates);
  showToast("Member removed.");
}

async function deleteUser(uid) {
  if (!validPathSegment(uid)) {
    showToast("Invalid user UID.");
    return;
  }

  const name = userName(uid);
  const self = uid === state.currentUser.uid;
  const message = self
    ? "Delete your own user record? You may lose admin panel access after refresh."
    : `Delete user ${name}? This will remove their profile and unlink them from rooms.`;
  if (!window.confirm(message)) return;

  const updates = {};
  updates[`users/${uid}`] = null;
  updates[`userRooms/${uid}`] = null;
  updates[`admins/${uid}`] = null;
  updates[`admin_user_deletions/${uid}`] = {
    deletedBy: state.currentUser.uid,
    deletedAt: serverTimestamp()
  };

  entries(state.rooms).forEach(([roomId, room]) => {
    if (!validPathSegment(roomId)) return;

    const roomRecord = asRecord(room);
    if (asRecord(roomRecord.members)[uid]) {
      updates[`rooms/${roomId}/forceLeave/${uid}`] = {
        at: serverTimestamp(),
        by: state.currentUser.uid,
        source: "admin-panel"
      };
      updates[`rooms/${roomId}/members/${uid}`] = null;
    }

    if (asRecord(roomRecord.bells)[uid]) {
      updates[`rooms/${roomId}/bells/${uid}`] = null;
    }

    entries(roomRecord.sosAlerts).forEach(([alertId, alertValue]) => {
      const alert = asRecord(alertValue);
      const senderUid = text(alert.senderUid || alert.fromUid).trim();
      if (validPathSegment(alertId) && senderUid === uid) {
        updates[`rooms/${roomId}/sosAlerts/${alertId}`] = null;
      }
    });
  });

  entries(state.userRooms).forEach(([otherUid, rooms]) => {
    if (otherUid !== uid && validPathSegment(otherUid) && asRecord(rooms)[uid]) {
      updates[`userRooms/${otherUid}/${uid}`] = null;
    }
  });

  entries(state.legacyAlerts).forEach(([alertId, alertValue]) => {
    const alert = asRecord(alertValue);
    const senderUid = text(alert.senderUid || alert.fromUid).trim();
    if (validPathSegment(alertId) && senderUid === uid) {
      updates[`sos_alerts/${alertId}`] = null;
    }
  });

  await update(ref(db), updates);
  if (state.selected && state.selected.type === "user" && state.selected.id === uid) {
    state.selected = null;
  }
  showToast("User deleted.");
}

async function deleteRoom(roomId) {
  if (!validPathSegment(roomId)) {
    showToast("Invalid room code.");
    return;
  }
  if (!window.confirm(`Delete room ${roomId} and unlink it from all members?`)) return;
  const room = asRecord(state.rooms[roomId]);
  const updates = {};
  updates[`roomTombstones/${roomId}`] = {
    deletedAt: serverTimestamp(),
    by: state.currentUser.uid,
    source: "admin-panel"
  };
  updates[`rooms/${roomId}`] = null;
  getRoomMembers(room).forEach((member) => {
    updates[`userRooms/${member.uid}/${roomId}`] = null;
  });
  entries(state.userRooms).forEach(([uid, rooms]) => {
    if (asRecord(rooms)[roomId]) updates[`userRooms/${uid}/${roomId}`] = null;
  });
  await update(ref(db), updates);
  state.selected = null;
  showToast("Room deleted.");
}

async function seedDefaultHighlights() {
  const updates = {
    "highlights/hl_people_first": {
      id: "hl_people_first",
      title: "People First Abilities Always",
      imageUrl: "assets/highlight_1.jpg",
      actionUrl: "https://www.jkm.gov.my",
      order: 1,
      active: true,
      createdAt: serverTimestamp()
    },
    "highlights/hl_ability_limits": {
      id: "hl_ability_limits",
      title: "Ability Has No Limits - Inclusion Is Everyone's Mission",
      imageUrl: "assets/highlight_2.jpg",
      actionUrl: "https://www.moh.gov.my",
      order: 2,
      active: true,
      createdAt: serverTimestamp()
    },
    "highlights/hl_different_abilities": {
      id: "hl_different_abilities",
      title: "Different Abilities One Community - Inclusion Today",
      imageUrl: "assets/highlight_3.jpg",
      actionUrl: "https://www.malaysia.gov.my",
      order: 3,
      active: true,
      createdAt: serverTimestamp()
    }
  };

  try {
    await update(ref(db), updates);
    showToast("3 Banner Kempen Inklusi OKU rasmi berjaya dimuatkan ke Firebase!");
  } catch (error) {
    console.error(error);
    showToast(error.message || "Gagal memuatkan banner default.");
  }
}

async function grantAdmin(uid) {
  if (!validPathSegment(uid)) {
    showToast("Enter a valid Firebase UID.");
    return;
  }
  await set(ref(db, `admins/${uid}`), {
    active: true,
    grantedAt: serverTimestamp(),
    grantedBy: state.currentUser.uid
  });
  els.adminUidInput.value = "";
  showToast("Admin access granted.");
}

async function revokeAdmin(uid) {
  if (!validPathSegment(uid)) {
    showToast("Invalid admin UID.");
    return;
  }
  const self = uid === state.currentUser.uid;
  const message = self
    ? "Revoke your own admin access? You will lose access after refresh."
    : `Revoke admin access for ${uid}?`;
  if (!window.confirm(message)) return;
  await remove(ref(db, `admins/${uid}`));
  showToast("Admin access revoked.");
}

async function setReportStatus(reportId, status) {
  if (!validPathSegment(reportId)) {
    showToast("Invalid report ID.");
    return;
  }
  const meta = reportStatusMeta(status);
  const updates = {
    status: meta.id,
    updatedAt: serverTimestamp(),
    reviewedBy: state.currentUser.uid
  };
  if (meta.id === "reviewing") updates.reviewedAt = serverTimestamp();
  if (meta.id === "resolved") updates.resolvedAt = serverTimestamp();
  if (meta.id === "rejected") updates.rejectedAt = serverTimestamp();
  await update(ref(db, `incidentReports/${reportId}`), updates);
  showToast(`Report marked ${meta.label.toLowerCase()}.`);
}

async function deleteReport(reportId) {
  if (!validPathSegment(reportId)) {
    showToast("Invalid report ID.");
    return;
  }
  if (!window.confirm(`Delete report ${reportId}?`)) return;
  await remove(ref(db, `incidentReports/${reportId}`));
  if (state.selected && state.selected.type === "report" && state.selected.id === reportId) {
    state.selected = null;
  }
  showToast("Report deleted.");
}

function exportJson() {
  const payload = {
    exportedAt: new Date().toISOString(),
    users: state.users,
    rooms: state.rooms,
    userRooms: state.userRooms,
    incidentReports: state.incidentReports,
    admin_notifications: state.adminNotifications,
    supportChats: state.supportChats,
    aiChats: state.aiChats,
    sos_alerts: state.legacyAlerts
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `ResQTap-admin-export-${Date.now()}.json`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

async function clearHistory() {
  const visibleHistoryCount = getNotificationHistory().length;
  if (!visibleHistoryCount) {
    showToast("No history logs to clear.");
    return;
  }

  if (!window.confirm(`Clear all ${visibleHistoryCount} history logs? This removes notification, bell, and SOS logs from Firebase.`)) return;

  const updates = {};
  entries(state.rooms).forEach(([roomId, room]) => {
    if (validPathSegment(roomId) && entries(asRecord(room).sosAlerts).length) {
      updates[`rooms/${roomId}/sosAlerts`] = null;
    }
    if (validPathSegment(roomId) && entries(asRecord(room).bells).length) {
      updates[`rooms/${roomId}/bells`] = null;
    }
  });
  if (entries(state.adminNotifications).length) {
    updates.admin_notifications = null;
    updates["broadcastNotifications/current"] = null;
  }
  updates["broadcastNotifications/history"] = null;
  if (entries(state.legacyAlerts).length) {
    updates.sos_alerts = null;
  }

  if (!Object.keys(updates).length) {
    showToast("No history logs to clear.");
    return;
  }

  await update(ref(db), updates);
  state.selected = null;
  state.notificationPage = 1;
  showToast("History cleared.");
}

function handleAction(button) {
  const action = button.dataset.action;
  const uid = button.dataset.uid || "";
  const chatUid = button.dataset.chatUid || "";
  const aiChatUid = button.dataset.aiChatUid || "";
  const room = button.dataset.room || "";
  const alert = button.dataset.alert || "";
  const notice = button.dataset.notice || "";
  const report = button.dataset.report || "";
  const status = button.dataset.status || "";
  const attachmentIndex = button.dataset.attachmentIndex || "";

  const run = async () => {
    if (action === "view-user") state.selected = { type: "user", id: uid };
    if (action === "view-room") state.selected = { type: "room", id: room };
    if (action === "view-sos") state.selected = { type: "sos", roomId: room, alertId: alert };
    if (action === "view-report") state.selected = { type: "report", id: report };
    if (action === "view-report-attachment") {
      viewReportAttachment(report, attachmentIndex);
      return;
    }
    if (action === "close-attachment-viewer") {
      closeAttachmentViewer();
      return;
    }
    if (action === "call-sos") {
      startAdminCall(uid, button.dataset.name || "", "video");
      return;
    }
    if (action === "call-reporter") {
      startAdminCall(uid, button.dataset.name || "Reporter", "voice");
      return;
    }
    if (action === "select-chat") {
      if (state.selectedChatUid && state.selectedChatUid !== chatUid) clearLivechatAdminTyping(state.selectedChatUid);
      if (state.selectedChatUid !== chatUid) clearLivechatAttachment();
      state.selectedChatUid = chatUid;
      state.activeView = "livechat";
      try {
        await claimLivechatThread(chatUid);
      } catch (error) {
        console.error(error);
        showToast(error.message || "Unable to claim livechat.");
      }
    }
    if (action === "select-ai-chat") {
      state.selectedAiChatUid = aiChatUid;
      state.activeView = "aichat";
    }
    if (action === "clear-detail") state.selected = null;
    if (action === "cancel-sos") await cancelSos(room, alert);
    if (action === "set-report-status") await setReportStatus(report, status);
    if (action === "delete-report") await deleteReport(report);
    if (action === "delete-notification") await deleteNotification(notice);
    if (action === "remove-member") await removeMember(room, uid);
    if (action === "delete-user") await deleteUser(uid);
    if (action === "delete-room") await deleteRoom(room);
    if (action === "revoke-admin") await revokeAdmin(uid);
    if (action === "toggle-highlight") {
      const hlId = button.dataset.highlightId;
      const curActive = button.dataset.active === "true";
      if (hlId) {
        await update(ref(db, `highlights/${hlId}`), { active: !curActive });
        showToast(curActive ? "Highlight hidden from mobile." : "Highlight active on mobile.");
      }
    }
    if (action === "delete-highlight") {
      const hlId = button.dataset.highlightId;
      if (hlId && window.confirm("Delete this highlight banner permanently?")) {
        await remove(ref(db, `highlights/${hlId}`));
        showToast("Highlight banner deleted.");
      }
    }
    if (action === "seed-default-highlights") {
      await seedDefaultHighlights();
    }
    render();
  };

  run().catch((error) => {
    console.error(error);
    showToast(error.message || "Action failed.");
  });
}

function canResizeDetailPanel() {
  return window.matchMedia("(min-width: 1321px)").matches;
}

function detailWidthFromPointer(clientX) {
  const shellRect = els.appShell.getBoundingClientRect();
  return shellRect.right - clientX;
}

function handleDetailResizeStart(event) {
  const handle = event.target.closest(".detail-resize-handle");
  if (!handle || !canResizeDetailPanel()) return;

  event.preventDefault();
  document.body.classList.add("detail-resizing");
  handle.classList.add("is-dragging");
  setDetailWidth(detailWidthFromPointer(event.clientX));

  try {
    handle.setPointerCapture(event.pointerId);
  } catch (error) {
    console.warn(error);
  }

  const resize = (moveEvent) => {
    setDetailWidth(detailWidthFromPointer(moveEvent.clientX));
  };

  const stopResize = () => {
    document.body.classList.remove("detail-resizing");
    handle.classList.remove("is-dragging");
    window.removeEventListener("pointermove", resize);
    window.removeEventListener("pointerup", stopResize);
    window.removeEventListener("pointercancel", stopResize);
    try {
      handle.releasePointerCapture(event.pointerId);
    } catch (error) {
      console.warn(error);
    }
  };

  window.addEventListener("pointermove", resize);
  window.addEventListener("pointerup", stopResize, { once: true });
  window.addEventListener("pointercancel", stopResize, { once: true });
}

function handleDetailResizeKeydown(event) {
  const handle = event.target.closest(".detail-resize-handle");
  if (!handle || !canResizeDetailPanel()) return;

  const step = event.shiftKey ? 72 : 32;
  if (event.key === "ArrowLeft") {
    event.preventDefault();
    setDetailWidth(state.detailWidth + step);
  }
  if (event.key === "ArrowRight") {
    event.preventDefault();
    setDetailWidth(state.detailWidth - step);
  }
  if (event.key === "Home") {
    event.preventDefault();
    setDetailWidth(DETAIL_PANEL_MIN_WIDTH);
  }
  if (event.key === "End") {
    event.preventDefault();
    setDetailWidth(DETAIL_PANEL_MAX_WIDTH);
  }
}

function bindEvents() {
  els.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    let email = els.emailInput.value.trim();
    const password = els.passwordInput.value;
    els.authMessage.textContent = "";
    els.loginButton.disabled = true;
    const btnText = els.loginButton.querySelector(".btn-text");
    const originalText = btnText ? btnText.textContent : "Login";
    if (btnText) btnText.textContent = "Logging in...";

    if (email && !email.includes("@")) {
      email = email + "@resqtap.com";
    }

    try {
      document.documentElement.classList.remove("admin-route-loading");
      await signInWithEmailAndPassword(auth, email, password);
    } catch (error) {
      console.error("Admin login error:", error);
      let msg = error.message || "Unable to sign in. Please verify your credentials.";
      if (error.code === "auth/invalid-credential" || error.code === "auth/user-not-found" || error.code === "auth/wrong-password") {
        msg = "Email atau kata laluan tidak sah / Invalid email or password.";
      } else if (error.code === "auth/invalid-email") {
        msg = "Format email tidak sah / Invalid email format.";
      } else if (error.code === "auth/too-many-requests") {
        msg = "Terlalu banyak percubaan gagal. Sila cuba sebentar lagi / Too many failed attempts.";
      }
      els.authMessage.textContent = msg;
    } finally {
      els.loginButton.disabled = false;
      if (btnText) btnText.textContent = originalText;
    }
  });

  const togglePasswordBtn = document.getElementById("togglePasswordBtn");
  if (togglePasswordBtn) {
    togglePasswordBtn.addEventListener("click", (e) => {
      e.preventDefault();
      const pwdInput = els.passwordInput;
      if (!pwdInput) return;
      const isPassword = pwdInput.getAttribute("type") === "password";
      pwdInput.setAttribute("type", isPassword ? "text" : "password");
      const iconEl = togglePasswordBtn.querySelector("[data-lucide]");
      if (iconEl) {
        iconEl.setAttribute("data-lucide", isPassword ? "eye-off" : "eye");
        refreshIcons();
      }
    });
  }

  if (els.signOutButton) {
    els.signOutButton.addEventListener("click", () => signOut(auth));
  }
  if (els.sidebarSignOutBtn && els.sidebarSignOutBtn !== els.signOutButton) {
    els.sidebarSignOutBtn.addEventListener("click", () => signOut(auth));
  }
  if (els.deniedSignOut) {
    els.deniedSignOut.addEventListener("click", () => signOut(auth));
  }

  // Dark Mode Toggle Logic
  function applyTheme(theme) {
    const isDark = theme === "dark";
    document.documentElement.classList.toggle("dark-mode", isDark);
    document.body.classList.toggle("dark-mode", isDark);
    try {
      localStorage.setItem("resqtap_theme", theme);
    } catch (e) {}

    const icon = document.getElementById("themeToggleIcon");
    const text = document.getElementById("themeToggleText");
    if (icon) {
      icon.setAttribute("data-lucide", isDark ? "sun" : "moon");
    }
    if (text) {
      text.textContent = isDark ? "Light Mode" : "Dark Mode";
    }
    refreshIcons();
  }

  const themeToggleBtn = document.getElementById("themeToggleBtn");
  if (themeToggleBtn) {
    themeToggleBtn.addEventListener("click", () => {
      const isDark = document.documentElement.classList.contains("dark-mode") || document.body.classList.contains("dark-mode");
      applyTheme(isDark ? "light" : "dark");
    });
  }

  // Initialize theme on start
  try {
    const currentSavedTheme = localStorage.getItem("resqtap_theme") || (document.documentElement.classList.contains("dark-mode") ? "dark" : "light");
    applyTheme(currentSavedTheme);
  } catch (e) {}

  els.globalSearch.addEventListener("input", () => {
    state.search = els.globalSearch.value;
    state.notificationPage = 1;
    render();
  });

  els.dashboardRangeSelect.addEventListener("change", () => {
    state.dashboardRange = els.dashboardRangeSelect.value || "today";
    render();
  });

  els.notificationMessageInput.addEventListener("input", updateNotificationComposer);
  els.notificationTitleInput.addEventListener("input", updateNotificationComposer);
  els.clearNotificationButton.addEventListener("click", clearNotificationComposer);
  els.noticeForm.addEventListener("submit", handleNoticeSubmit);
  els.notificationPageSizeSelect.addEventListener("change", () => {
    state.notificationPageSize = Number(els.notificationPageSizeSelect.value) || 10;
    state.notificationPage = 1;
    renderNotices();
    refreshIcons();
  });
  els.notificationCategoryTabs.addEventListener("click", (event) => {
    const tab = event.target.closest("[data-notification-category]");
    if (!tab) return;
    state.notificationCategory = tab.dataset.notificationCategory || "all";
    state.notificationPage = 1;
    renderNotices();
    refreshIcons();
  });
  els.notificationPrevPageButton.addEventListener("click", () => {
    state.notificationPage = Math.max(1, state.notificationPage - 1);
    renderNotices();
    refreshIcons();
  });
  els.notificationNextPageButton.addEventListener("click", () => {
    state.notificationPage += 1;
    renderNotices();
    refreshIcons();
  });
  els.livechatReplyInput.addEventListener("input", handleLivechatTypingInput);
  els.livechatReplyInput.addEventListener("blur", () => clearLivechatAdminTyping());
  els.livechatReplyForm.addEventListener("submit", sendAdminLivechatReply);
  els.livechatAttachButton.addEventListener("click", (event) => {
    event.stopPropagation();
    setLivechatAttachMenu(els.livechatAttachMenu.classList.contains("hidden"));
  });
  els.livechatAttachMenu.addEventListener("click", (event) => {
    event.stopPropagation();
    const option = event.target.closest("[data-livechat-attach]");
    if (!option) return;
    chooseLivechatAttachment(option.dataset.livechatAttach || "");
  });
  els.livechatAttachmentInput.addEventListener("change", handleLivechatAttachmentSelected);
  els.livechatAttachmentLabel.addEventListener("click", (event) => {
    const clear = event.target.closest("[data-livechat-clear-attachment]");
    if (clear) clearLivechatAttachment();
  });
  els.resolveChatButton.addEventListener("click", () => {
    resolveSelectedChat().catch((error) => {
      console.error(error);
      showToast(error.message || "Unable to resolve livechat.");
    });
  });
  els.deleteChatButton.addEventListener("click", () => {
    deleteSelectedResolvedChat().catch((error) => {
      console.error(error);
      showToast(error.message || "Unable to delete livechat.");
    });
  });
  els.deleteAiChatButton.addEventListener("click", () => {
    deleteSelectedAiChat().catch((error) => {
      console.error(error);
      showToast(error.message || "Unable to reset AI chat.");
    });
  });

  if (els.exportButton) {
    els.exportButton.addEventListener("click", exportJson);
  }
  if (els.clearHistoryButton) {
    els.clearHistoryButton.addEventListener("click", () => {
      clearHistory().catch((error) => {
        console.error(error);
        showToast(error.message || "Unable to clear history logs.");
      });
    });
  }

  if (els.grantAdminForm) {
    els.grantAdminForm.addEventListener("submit", (event) => {
      event.preventDefault();
      grantAdmin(els.adminUidInput.value.trim()).catch((error) => {
        console.error(error);
        showToast(error.message || "Unable to grant admin access.");
      });
    });
  }

  // Highlight Form Listeners
  const hlForm = document.getElementById("highlightForm");
  const hlFile = document.getElementById("hlImageFileInput");
  const hlUrl = document.getElementById("hlImageUrlInput");
  const hlPreview = document.getElementById("hlImagePreview");
  const hlNoPreview = document.getElementById("hlNoPreviewBox") || document.getElementById("hlNoPreviewText");

  if (hlFile) {
    hlFile.addEventListener("change", async () => {
      const file = hlFile.files && hlFile.files[0];
      if (file) {
        try {
          const dataUrl = await imageFileToInlineDataUrl(file);
          if (hlPreview) {
            hlPreview.src = dataUrl;
            hlPreview.style.display = "block";
            if (hlNoPreview) hlNoPreview.style.display = "none";
          }
        } catch (e) {
          console.warn(e);
        }
      }
    });
  }

  if (hlUrl) {
    hlUrl.addEventListener("input", () => {
      const val = hlUrl.value.trim();
      if (val && hlPreview) {
        hlPreview.src = val;
        hlPreview.style.display = "block";
        if (hlNoPreview) hlNoPreview.style.display = "none";
      }
    });
  }

  if (hlForm) {
    hlForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const title = document.getElementById("hlTitleInput").value.trim();
      const actionUrl = document.getElementById("hlActionUrlInput").value.trim();
      const order = Number(document.getElementById("hlOrderInput").value) || 1;
      let imageUrl = hlUrl ? hlUrl.value.trim() : "";

      if (hlFile && hlFile.files && hlFile.files[0]) {
        try {
          imageUrl = await imageFileToInlineDataUrl(hlFile.files[0]);
        } catch (err) {
          showToast("Error processing image file.");
          return;
        }
      }

      if (!imageUrl) {
        showToast("Please provide an image URL or upload an image file.");
        return;
      }

      try {
        const newRef = push(ref(db, "highlights"));
        await set(newRef, {
          id: newRef.key,
          title,
          imageUrl,
          actionUrl,
          order,
          active: true,
          createdAt: serverTimestamp()
        });
        hlForm.reset();
        if (hlPreview) hlPreview.style.display = "none";
        if (hlNoPreview) hlNoPreview.style.display = "block";
        showToast("Highlight banner added successfully!");
      } catch (err) {
        console.error(err);
        showToast(err.message || "Failed to save highlight banner.");
      }
    });
  }

  els.detailPanel.addEventListener("pointerdown", handleDetailResizeStart);
  els.detailPanel.addEventListener("keydown", handleDetailResizeKeydown);
  window.addEventListener("resize", () => {
    if (state.selected) setDetailWidth(state.detailWidth);
    setLivechatAttachMenu(false);
  });
  window.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      setLivechatAttachMenu(false);
      closeAttachmentViewer();
    }
  });

  try {
    localStorage.removeItem("resqtap_admin_sidebar_collapsed");
  } catch (e) {}
  if (els.appShell) {
    els.appShell.classList.remove("sidebar-collapsed");
  }

  function toggleMobileSidebar(force) {
    if (!els.appShell) return;
    const shouldOpen = typeof force === "boolean" ? force : !els.appShell.classList.contains("sidebar-open");
    els.appShell.classList.toggle("sidebar-open", shouldOpen);
    if (els.sidebarBackdrop) els.sidebarBackdrop.classList.toggle("is-open", shouldOpen);
  }

  function closeMobileSidebar() {
    toggleMobileSidebar(false);
  }

  if (els.sidebarToggleBtn) {
    els.sidebarToggleBtn.addEventListener("click", () => toggleMobileSidebar());
  }
  if (els.sidebarCloseBtn) {
    els.sidebarCloseBtn.addEventListener("click", () => closeMobileSidebar());
  }
  if (els.sidebarBackdrop) {
    els.sidebarBackdrop.addEventListener("click", () => closeMobileSidebar());
  }

  document.body.addEventListener("click", (event) => {
    if (!event.target.closest(".livechat-attach-wrap")) {
      setLivechatAttachMenu(false);
    }

    const nav = event.target.closest("[data-view]");
    if (nav) {
      setActiveView(nav.dataset.view);
      closeMobileSidebar();
      return;
    }

    const jump = event.target.closest("[data-view-jump]");
    if (jump) {
      setActiveView(jump.dataset.viewJump);
      closeMobileSidebar();
      return;
    }

    const action = event.target.closest("[data-action]");
    if (action) handleAction(action);
  });
}

window.addEventListener("load", refreshIcons);

onAuthStateChanged(auth, async (user) => {
  if (!isAdminRoute()) {
    showPublicSite();
    return;
  }

  enterAdminRoute();
  cleanupDataListeners();
  state.currentUser = user;
  state.selected = null;
  state.selectedChatUid = "";
  state.selectedAiChatUid = "";

  if (!user) {
    setScreen("auth");
    state.users = {};
    state.rooms = {};
    state.userRooms = {};
    state.admins = {};
    state.legacyAlerts = {};
    state.incidentReports = {};
    state.adminNotifications = {};
    state.supportChats = {};
    state.aiChats = {};
    state.selectedChatUid = "";
    state.selectedAiChatUid = "";
    state.dashboardSignature = "";
    state.dashboardMetricValues = {};
    return;
  }

  try {
    if (!await isCurrentUserAdmin(user.uid)) {
      els.deniedUid.textContent = `UID: ${user.uid}`;
      els.adminPathCode.textContent = `admins/${user.uid}/active = true`;
      setScreen("denied");
      return;
    }

    setScreen("app");
    startDataListeners();
  } catch (error) {
    els.deniedUid.textContent = `UID: ${user.uid}`;
    els.adminPathCode.textContent = `admins/${user.uid}/active = true`;
    setScreen("denied");
    showToast(error.message || "Unable to verify admin access.");
  }
});

bindEvents();

let vcPeerConnection = null;
let vcLocalStream = null;
let vcRemoteStream = null;
let vcCurrentCallId = null;
let vcTargetUid = null;
let vcCurrentCallType = "video";
let vcUnsubscribers = [];

const elsVc = {
  modal: document.getElementById("videoCallModal"),
  status: document.getElementById("videoCallStatus"),
  overlay: document.getElementById("videoCallOverlay"),
  localVideo: document.getElementById("localVideo"),
  remoteVideo: document.getElementById("remoteVideo"),
  endBtn: document.getElementById("vcEndBtn"),
  micBtn: document.getElementById("vcMicBtn"),
  camBtn: document.getElementById("vcCamBtn"),
  speakerBtn: document.getElementById("vcSpeakerBtn"),
  moreBtn: document.getElementById("vcMoreBtn"),
  bottomSheet: document.getElementById("vcBottomSheet"),
  shareScreenBtn: document.getElementById("vcShareScreenBtn"),
  sendMessageBtn: document.getElementById("vcSendMessageBtn"),
  localMuteIndicator: document.getElementById("localMuteIndicator"),
  remoteMuteIndicator: document.getElementById("remoteMuteIndicator"),
  localCamOffIndicator: document.getElementById("localCamOffIndicator"),
  remoteCamOffIndicator: document.getElementById("remoteCamOffIndicator"),
  minimizeBtn: document.getElementById("vcMinimizeBtn"),
  floatingAvatar: document.getElementById("vcFloatingAvatar"),
  floatingName: document.getElementById("vcFloatingName"),
  voicePanel: document.getElementById("voiceCallPanel"),
  voiceAvatar: document.getElementById("voiceCallAvatar"),
  voiceName: document.getElementById("voiceCallName"),
  voiceLabel: document.getElementById("voiceCallLabel")
};

let isVcMicEnabled = true;
let isVcCamEnabled = true;
async function startAdminCall(uid, name, callType = "video") {
  if (vcPeerConnection) endAdminCall();

  const isVoiceCall = callType === "voice";
  const displayName = name || "User";
  vcTargetUid = uid;
  vcCurrentCallType = isVoiceCall ? "voice" : "video";
  elsVc.status.textContent = "Calling " + displayName.toUpperCase() + "...";
  elsVc.floatingName.textContent = displayName;
  if (elsVc.voiceName) elsVc.voiceName.textContent = displayName;
  if (elsVc.voiceLabel) elsVc.voiceLabel.textContent = isVoiceCall ? "Voice call" : "Video call";

  const userPhoto = state.users[uid]?.photoUrl;
  if (userPhoto) {
    elsVc.floatingAvatar.style.backgroundImage = `url(${userPhoto})`;
    elsVc.floatingAvatar.textContent = "";
    if (elsVc.voiceAvatar) {
      elsVc.voiceAvatar.style.backgroundImage = `url(${userPhoto})`;
      elsVc.voiceAvatar.textContent = "";
    }
  } else {
    elsVc.floatingAvatar.style.backgroundImage = "none";
    elsVc.floatingAvatar.textContent = displayName.charAt(0).toUpperCase();
    if (elsVc.voiceAvatar) {
      elsVc.voiceAvatar.style.backgroundImage = "none";
      elsVc.voiceAvatar.textContent = displayName.charAt(0).toUpperCase();
    }
  }

  elsVc.remoteVideo.srcObject = null;
  elsVc.localVideo.srcObject = null;
  elsVc.overlay.classList.remove("hidden");
  elsVc.modal.classList.remove("hidden");
  elsVc.modal.classList.toggle("voice-mode", isVoiceCall);
  if (elsVc.voicePanel) elsVc.voicePanel.classList.toggle("hidden", !isVoiceCall);
  elsVc.bottomSheet.classList.remove("show");

  isVcMicEnabled = true;
  isVcCamEnabled = !isVoiceCall;

  const micOn = elsVc.micBtn.querySelector(".icon-on");
  const micOff = elsVc.micBtn.querySelector(".icon-off");
  if (micOn) micOn.classList.remove("hidden");
  if (micOff) micOff.classList.add("hidden");

  const camOn = elsVc.camBtn.querySelector(".icon-on");
  const camOff = elsVc.camBtn.querySelector(".icon-off");
  if (camOn) camOn.classList.toggle("hidden", isVoiceCall);
  if (camOff) camOff.classList.add("hidden");
  elsVc.localMuteIndicator.classList.add("hidden");
  if (elsVc.remoteMuteIndicator) elsVc.remoteMuteIndicator.classList.add("hidden");
  elsVc.localCamOffIndicator.classList.add("hidden");
  if (elsVc.remoteCamOffIndicator) elsVc.remoteCamOffIndicator.classList.add("hidden");

  try {
    vcLocalStream = await navigator.mediaDevices.getUserMedia(
      isVoiceCall ? { audio: true, video: false } : { video: true, audio: true }
    );
    if (!isVoiceCall) {
      elsVc.localVideo.srcObject = vcLocalStream;
    }

    vcPeerConnection = new RTCPeerConnection({
      iceServers: [{ urls: "stun:stun.l.google.com:19302" }]
    });

    vcLocalStream.getTracks().forEach(track => {
      vcPeerConnection.addTrack(track, vcLocalStream);
    });

    vcPeerConnection.ontrack = (event) => {
      elsVc.overlay.classList.add("hidden");
      if (!vcRemoteStream) {
        vcRemoteStream = new MediaStream();
        elsVc.remoteVideo.srcObject = vcRemoteStream;
      }
      vcRemoteStream.addTrack(event.track);
      elsVc.remoteVideo.play().catch(() => {});
    };

    const callRef = push(ref(db, "calls"));
    vcCurrentCallId = callRef.key;

    vcPeerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        push(ref(db, "calls/" + vcCurrentCallId + "/callerCandidates"), {
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex
        });
      }
    };

    const offer = await vcPeerConnection.createOffer();
    await vcPeerConnection.setLocalDescription(offer);

    await update(ref(db, "calls/" + vcCurrentCallId), {
      offer: { type: offer.type, sdp: offer.sdp },
      callerUid: state.currentUser.uid,
      callerName: "ResQTap",
      callerPhotoUrl: "",
      callType: vcCurrentCallType,
      status: "calling",
      timestamp: serverTimestamp()
    });

    await update(ref(db, "userCalls/" + vcTargetUid + "/currentCall"), {
      callId: vcCurrentCallId,
      status: "ringing",
      callerName: "ResQTap",
      callType: vcCurrentCallType,
      timestamp: serverTimestamp()
    });

    const answerUnsub = onValue(ref(db, "calls/" + vcCurrentCallId + "/answer"), (snapshot) => {
      const data = snapshot.val();
      if (data && !vcPeerConnection.currentRemoteDescription) {
        const rtcDescription = new RTCSessionDescription(data);
        vcPeerConnection.setRemoteDescription(rtcDescription);
      }
    });
    vcUnsubscribers.push(answerUnsub);

    const cameraStateRef = ref(db, `calls/${vcCurrentCallId}/cameraState`);
    set(child(cameraStateRef, 'caller'), !isVoiceCall);
    if (!isVoiceCall) {
      const cameraUnsub = onValue(child(cameraStateRef, 'callee'), (snapshot) => {
        if (snapshot.exists()) {
          const isCamEnabled = snapshot.val();
          if (elsVc.remoteCamOffIndicator) {
            if (isCamEnabled) {
              elsVc.remoteCamOffIndicator.classList.add("hidden");
            } else {
              elsVc.remoteCamOffIndicator.classList.remove("hidden");
            }
          }
        }
      });
      vcUnsubscribers.push(cameraUnsub);
    }

    const micStateRef = ref(db, `calls/${vcCurrentCallId}/micState`);
    set(child(micStateRef, 'caller'), true);

    const micUnsub = onValue(child(micStateRef, 'callee'), (snapshot) => {
      if (snapshot.exists()) {
        const isMicEnabled = snapshot.val();
        if (elsVc.remoteMuteIndicator) {
          if (isMicEnabled) {
            elsVc.remoteMuteIndicator.classList.add("hidden");
          } else {
            elsVc.remoteMuteIndicator.classList.remove("hidden");
          }
        }
      }
    });
    vcUnsubscribers.push(micUnsub);

    const iceUnsub = onValue(ref(db, "calls/" + vcCurrentCallId + "/calleeCandidates"), (snapshot) => {
      snapshot.forEach(childSnapshot => {
        const data = childSnapshot.val();
        const candidate = new RTCIceCandidate(data);
        vcPeerConnection.addIceCandidate(candidate);
      });
    });
    vcUnsubscribers.push(iceUnsub);

    const statusUnsub = onValue(ref(db, "userCalls/" + uid + "/currentCall/status"), (snapshot) => {
      const status = snapshot.val();
      if (status === "rejected" || status === "ended" || status === null) {
        showToast(status === null ? "Call declined" : "Call " + status);
        endAdminCall();
      } else if (status === "accepted") {
        elsVc.status.textContent = "Connecting...";
      }
    });
    vcUnsubscribers.push(statusUnsub);

  } catch (error) {
    console.error("Call error:", error);
    showToast("Error starting call: " + error.message);
    endAdminCall();
  }
}

function endAdminCall() {
  vcUnsubscribers.forEach(unsub => unsub());
  vcUnsubscribers = [];

  if (vcTargetUid && vcCurrentCallId) {
    update(ref(db, "userCalls/" + vcTargetUid + "/currentCall"), { status: "ended" });
    remove(ref(db, "calls/" + vcCurrentCallId));
  }

  if (vcPeerConnection) {
    vcPeerConnection.close();
    vcPeerConnection = null;
  }
  if (vcLocalStream) {
    vcLocalStream.getTracks().forEach(track => track.stop());
    vcLocalStream = null;
  }

  elsVc.localVideo.srcObject = null;
  elsVc.remoteVideo.srcObject = null;
  vcRemoteStream = null;
  vcCurrentCallId = null;
  vcTargetUid = null;
  vcCurrentCallType = "video";

  elsVc.modal.classList.remove("floating");
  elsVc.modal.classList.remove("voice-mode");
  if (elsVc.voicePanel) elsVc.voicePanel.classList.add("hidden");
  elsVc.modal.style.left = "";
  elsVc.modal.style.top = "";
  elsVc.modal.style.right = "";
  elsVc.modal.style.bottom = "";
  elsVc.modal.classList.add("hidden");
  elsVc.bottomSheet.classList.remove("show");
}

elsVc.minimizeBtn.addEventListener("click", (e) => {
  e.stopPropagation();
  elsVc.modal.classList.add("floating");
});

elsVc.endBtn.addEventListener("click", () => {
  endAdminCall();
});

elsVc.micBtn.addEventListener("click", () => {
  if (!vcLocalStream) return;
  const audioTrack = vcLocalStream.getAudioTracks()[0];
  if (!audioTrack) return;
  isVcMicEnabled = !isVcMicEnabled;
  audioTrack.enabled = isVcMicEnabled;

  const micOn = elsVc.micBtn.querySelector(".icon-on");
  const micOff = elsVc.micBtn.querySelector(".icon-off");
  if (isVcMicEnabled) {
    if (micOn) micOn.classList.remove("hidden");
    if (micOff) micOff.classList.add("hidden");
    elsVc.localMuteIndicator.classList.add("hidden");
    if (vcCurrentCallId) {
      set(ref(db, `calls/${vcCurrentCallId}/micState/caller`), true);
    }
  } else {
    if (micOn) micOn.classList.add("hidden");
    if (micOff) micOff.classList.remove("hidden");
    elsVc.localMuteIndicator.classList.remove("hidden");
    if (vcCurrentCallId) {
      set(ref(db, `calls/${vcCurrentCallId}/micState/caller`), false);
    }
  }
});

elsVc.camBtn.addEventListener("click", () => {
  if (!vcLocalStream || vcCurrentCallType === "voice") return;
  const videoTrack = vcLocalStream.getVideoTracks()[0];
  if (!videoTrack) return;
  isVcCamEnabled = !isVcCamEnabled;
  videoTrack.enabled = isVcCamEnabled;

  const camOn = elsVc.camBtn.querySelector(".icon-on");
  const camOff = elsVc.camBtn.querySelector(".icon-off");
  if (isVcCamEnabled) {
    if (camOn) camOn.classList.remove("hidden");
    if (camOff) camOff.classList.add("hidden");
    elsVc.localCamOffIndicator.classList.add("hidden");
    if (vcCurrentCallId) {
      set(ref(db, `calls/${vcCurrentCallId}/cameraState/caller`), true);
    }
  } else {
    if (camOn) camOn.classList.add("hidden");
    if (camOff) camOff.classList.remove("hidden");
    elsVc.localCamOffIndicator.classList.remove("hidden");
    if (vcCurrentCallId) {
      set(ref(db, `calls/${vcCurrentCallId}/cameraState/caller`), false);
    }
  }
});

elsVc.moreBtn.addEventListener("click", () => {
  elsVc.bottomSheet.classList.toggle("show");
});

let isDraggingVc = false;
let vcDragStartX = 0;
let vcDragStartY = 0;
let vcInitialLeft = 0;
let vcInitialTop = 0;
let vcHasDragged = false;

elsVc.modal.addEventListener("mousedown", (e) => {
  if (!elsVc.modal.classList.contains("floating")) return;
  if (e.target.closest("button") || e.target.closest(".vc-bottom-sheet")) return;

  isDraggingVc = true;
  vcHasDragged = false;
  vcDragStartX = e.clientX;
  vcDragStartY = e.clientY;
  const rect = elsVc.modal.getBoundingClientRect();
  vcInitialLeft = rect.left;
  vcInitialTop = rect.top;

  elsVc.modal.style.transition = "none";
  elsVc.modal.style.right = "auto";
  elsVc.modal.style.bottom = "auto";
  elsVc.modal.style.left = vcInitialLeft + "px";
  elsVc.modal.style.top = vcInitialTop + "px";
});

window.addEventListener("mousemove", (e) => {
  if (!isDraggingVc) return;
  const dx = e.clientX - vcDragStartX;
  const dy = e.clientY - vcDragStartY;

  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) vcHasDragged = true;

  let newLeft = vcInitialLeft + dx;
  let newTop = vcInitialTop + dy;

  const w = elsVc.modal.offsetWidth;
  const h = elsVc.modal.offsetHeight;
  if (newLeft < 0) newLeft = 0;
  if (newTop < 0) newTop = 0;
  if (newLeft + w > window.innerWidth) newLeft = window.innerWidth - w;
  if (newTop + h > window.innerHeight) newTop = window.innerHeight - h;

  elsVc.modal.style.left = newLeft + "px";
  elsVc.modal.style.top = newTop + "px";
});

window.addEventListener("mouseup", (e) => {
  if (!isDraggingVc) return;
  isDraggingVc = false;

  elsVc.modal.style.transition = "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)";

  const w = elsVc.modal.offsetWidth;
  const h = elsVc.modal.offsetHeight;
  const rect = elsVc.modal.getBoundingClientRect();
  const centerX = rect.left + w / 2;

  if (centerX < window.innerWidth / 2) {
    elsVc.modal.style.left = "24px";
  } else {
    elsVc.modal.style.left = (window.innerWidth - w - 24) + "px";
  }

  if (rect.top < 24) elsVc.modal.style.top = "24px";
  if (rect.top + h > window.innerHeight - 24) elsVc.modal.style.top = (window.innerHeight - h - 24) + "px";
});

elsVc.modal.addEventListener("click", (e) => {
  if (elsVc.modal.classList.contains("floating")) {
    if (vcHasDragged) {
      vcHasDragged = false;
      return;
    }
    elsVc.modal.classList.remove("floating");
    elsVc.modal.style.left = "";
    elsVc.modal.style.top = "";
    elsVc.modal.style.right = "";
    elsVc.modal.style.bottom = "";
    return;
  }
  if (!e.target.closest(".vc-bottom-sheet") && !e.target.closest("#vcMoreBtn") && !e.target.closest("#vcMinimizeBtn")) {
    elsVc.bottomSheet.classList.remove("show");
  }
});

elsVc.shareScreenBtn.addEventListener("click", async (e) => {
  e.stopPropagation();
  elsVc.bottomSheet.classList.remove("show");
  if (!vcPeerConnection || vcCurrentCallType === "voice") return;

  try {
    const displayStream = await navigator.mediaDevices.getDisplayMedia({ video: true });
    const screenTrack = displayStream.getVideoTracks()[0];

    const senders = vcPeerConnection.getSenders();
    const videoSender = senders.find(s => s.track && s.track.kind === 'video');
    if (videoSender) {
      videoSender.replaceTrack(screenTrack);
    }
    elsVc.localVideo.srcObject = displayStream;

    screenTrack.onended = () => {

      const cameraTrack = vcLocalStream ? vcLocalStream.getVideoTracks()[0] : null;
      if (cameraTrack && videoSender) {
        videoSender.replaceTrack(cameraTrack);
        elsVc.localVideo.srcObject = vcLocalStream;
      }
    };
  } catch (error) {
    console.error("Screen sharing failed", error);
  }
});

elsVc.sendMessageBtn.addEventListener("click", (e) => {
  e.stopPropagation();
  elsVc.bottomSheet.classList.remove("show");
  const uid = vcTargetUid;

  elsVc.modal.classList.add("floating");

  state.selectedChatUid = uid;
  setActiveView("livechat");
});

