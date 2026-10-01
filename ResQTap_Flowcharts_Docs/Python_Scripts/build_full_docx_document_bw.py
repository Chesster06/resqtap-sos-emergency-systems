import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
import generate_docx_report_bw as gdr_bw

DOCS_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts_bw")
OUTPUT_DOCX = os.path.join(os.path.dirname(__file__), "ResQTap_System_Flowcharts_Specification_BW.docx")

def build_monochrome_document():
    doc = docx.Document()
    
    # Page setup - A4 with 1 inch (72pt) margins
    for section in doc.sections:
        section.page_width = Inches(8.27)
        section.page_height = Inches(11.69)
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        
    print("Building Monochrome (Black & White) Word documentation...")

    # =========================================================================
    # COVER / EXECUTIVE HEADER (MONOCHROME)
    # =========================================================================
    p_badge = doc.add_paragraph()
    gdr_bw.format_paragraph(p_badge, space_before=10, space_after=6)
    r_badge = p_badge.add_run("SOFTWARE ENGINEERING SPECIFICATION • FINAL YEAR PROJECT (FYP) • MONOCHROME EDITION")
    r_badge.font.name = gdr_bw.FONT_FAMILY
    r_badge.font.size = Pt(9.5)
    r_badge.font.bold = True
    r_badge.font.color.rgb = gdr_bw.COLOR_BLACK

    p_title = doc.add_paragraph()
    gdr_bw.format_paragraph(p_title, space_before=0, space_after=6)
    r_title = p_title.add_run("SYSTEM FLOWCHARTS SPECIFICATION DOCUMENT\nRESQTAP (ONETAPSOS) ECOSYSTEM")
    r_title.font.name = gdr_bw.FONT_FAMILY
    r_title.font.size = Pt(21)
    r_title.font.bold = True
    r_title.font.color.rgb = gdr_bw.COLOR_BLACK

    p_subtitle = doc.add_paragraph()
    gdr_bw.format_paragraph(p_subtitle, space_before=0, space_after=14)
    r_subtitle = p_subtitle.add_run("Personal Emergency SOS, Rapid Response Dispatch, Real-Time Geolocation & WebRTC Intercom System")
    r_subtitle.font.name = gdr_bw.FONT_FAMILY
    r_subtitle.font.size = Pt(12.5)
    r_subtitle.font.bold = True
    r_subtitle.font.color.rgb = gdr_bw.COLOR_DARK_GRAY

    # Metadata Table (Black & White)
    meta_headers = ["Specification Parameter", "System Details"]
    meta_rows = [
        ["Project Title", "ResQTap (OneTapSOS) Emergency Response System"],
        ["Mobile Client Platform", "Android Native (Java & Kotlin, Min SDK 24, Target SDK 36)"],
        ["Cloud & Backend Infrastructure", "Firebase Platform (Realtime Database, Authentication, Cloud Storage, Cloud Messaging)"],
        ["Real-Time Media Communications", "Google WebRTC P2P Intercom (Voice & Video Calling) & FusedLocationProvider"],
        ["Web Portal & Administration", "Node.js HTTP Server, REST APIs, & Real-Time Incident Management Console"],
        ["Flowchart Standard", "ISO 5807:1985 Information Processing - Flowchart Symbols and Conventions"],
        ["Color Palette Format", "Monochrome / Grayscale (Strictly Black & White for Print & Academic Submission)"],
        ["Document Version", "Version 2.0 (Official Stable Release)"],
        ["Release Date", "September 2026"]
    ]
    gdr_bw.create_styled_table_bw(doc, meta_headers, meta_rows, col_widths=[2.4, 3.8])

    gdr_bw.add_callout_bw(doc, 
        "This specification document and all embedded flowchart diagrams have been formatted exclusively in high-contrast "
        "Black and White (Monochrome/Grayscale). This format is optimized for academic thesis submissions, formal engineering reports, "
        "and standard black-and-white printing while preserving full ISO 5807 structural fidelity.",
        title="MONOCHROME SPECIFICATION COMPLIANCE")

    doc.add_page_break()

    # =========================================================================
    # SECTION 1: INTRODUCTION & SYSTEM ARCHITECTURE
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "1. INTRODUCTION & SYSTEM ARCHITECTURE OVERVIEW")
    
    gdr_bw.add_body_p_bw(doc, 
        "ResQTap (OneTapSOS) is an emergency response and personal safety platform engineered to bridge the critical first five "
        "minutes of life-threatening events (The Golden 5-Minute Window). The ecosystem seamlessly connects an Android mobile application, "
        "Google Play Services location APIs, Firebase Cloud services (Authentication, Realtime Database, Cloud Functions, and Firebase Cloud Messaging), "
        "a WebRTC peer-to-peer audio/video calling engine, and a web-based command-and-control administration portal.")

    gdr_bw.add_heading_2_bw(doc, "1.1 Core System Modules")
    gdr_bw.add_bullet_p_bw(doc, "Governs user onboarding, Firebase Auth sessions, 4-digit passkey app lock, native fingerprint biometric authentication, and a covert Duress PIN security safeguard.", bold_prefix="Module 1 - Authentication & Security Gateway: ")
    gdr_bw.add_bullet_p_bw(doc, "Handles single-tap SOS triggering with an interruptible 3-second countdown, hardware alert blasting (sirens, camera strobe, haptic pulses), multi-room cloud broadcasting, and an interactive 4-stage response timeline.", bold_prefix="Module 2 - OneTap SOS Emergency Dispatch: ")
    gdr_bw.add_bullet_p_bw(doc, "Manages trusted group circles using unique 6-character room codes and QR codes, member battery and presence telemetry, instant haptic Bell pinging, an Android Foreground Tracking Service, and Google Maps SDK marker synchronization.", bold_prefix="Module 3 - Emergency Rooms & Live GPS Tracking: ")
    gdr_bw.add_bullet_p_bw(doc, "Provides ultra-low-latency two-way voice and video intercom via WebRTC P2P mesh architecture, utilizing Firebase RTDB as a signaling conduit and Android foreground services for camera and microphone access.", bold_prefix="Module 4 - WebRTC Voice & Video Intercom: ")
    gdr_bw.add_bullet_p_bw(doc, "Enables citizen incident reporting with compressed photographic evidence uploaded to Firebase Storage, geospatial nearest-hospital discovery via Haversine calculations, emergency hotline dialing, and turn-by-turn navigation.", bold_prefix="Module 5 - Community Reporting & Hospital Finder: ")
    gdr_bw.add_bullet_p_bw(doc, "Web-based management console for live incident surveillance, operator incident reservation, targeted broadcast notifications via Cloud Functions, and complete user data purging.", bold_prefix="Module 6 - Web Admin Portal & Cloud Broadcasting: ")

    gdr_bw.add_heading_2_bw(doc, "1.2 ISO 5807 Standard Flowchart Symbols (Monochrome)")
    gdr_bw.add_body_p_bw(doc, 
        "To ensure engineering clarity and academic rigor, all diagrams in this document strictly adhere to the international standard "
        "ISO 5807:1985 (Information Processing - Documentation Symbols and Conventions for Data, Program and System Flowcharts):")

    iso_headers = ["Symbol Shape", "Symbol Name (ISO)", "Standard Meaning", "Implementation in ResQTap"]
    iso_rows = [
        ["Pill / Oval", "Terminal (Start / End)", "Marks the entry or exit point of an execution flow or program cycle.", "START (solid black fill) and END (double-line border)."],
        ["Rectangle", "Process", "Standard computation, data transformation, program instruction, or UI update.", "Hardware driver initialization, UI state updates, siren/strobe activation."],
        ["Diamond", "Decision", "Conditional branch evaluating a logical condition (Boolean or Multi-branch).", "Session checks, PIN credential matching, Duress PIN detection, action choices."],
        ["Parallelogram", "Input / Output (I/O)", "Data entry from user / hardware sensors or output presentation to screen.", "PIN keypad entry, QR code camera scanning, hospital directory listing."],
        ["Double-Stripe Rectangle", "Predefined Process (Subroutine)", "An external module, library call, cloud function, or background service.", "Firebase Cloud Functions, LiveRoomTrackingService, WebRTC handshake."],
        ["Arrow / Flowline", "Flowline & Connectors", "Indicates sequence of execution and direction of control between nodes.", "Sequential directional paths with decision labels (Yes / No / Accept)."]
    ]
    gdr_bw.create_styled_table_bw(doc, iso_headers, iso_rows, col_widths=[1.3, 1.4, 1.7, 1.8])

    doc.add_page_break()

    # =========================================================================
    # SECTION 2: FLOWCHART 1 - OVERALL SYSTEM ARCHITECTURE (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "2. SECTION 1: FLOWCHART OF OVERALL SYSTEM ARCHITECTURE")
    
    gdr_bw.add_body_p_bw(doc, 
        "Flowchart 1 captures the holistic operational workflow of the ResQTap ecosystem in pure monochrome format. It visualizes the "
        "end-to-end execution path starting from the initial SplashActivity launch, session verification via Firebase Auth, the App Lock "
        "security perimeter, and user dispatch into the five core functional modules available from the Main Dashboard (MainActivity).")

    # Embed B&W Flowchart Image 1
    fc1_path = os.path.join(DOCS_DIR, "flowchart_1_overall_architecture_bw.png")
    if os.path.exists(fc1_path):
        doc.add_picture(fc1_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 2.1: Flowchart of Overall System Navigation and Logical Architecture (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "2.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "The application boots via SplashActivity. The system applies the user's saved theme mode, checks for required runtime permissions (Location, Notifications, Camera), and initializes the Firebase App environment.", bold_prefix="Step 1.0 - Application Launch & Splash Screen: ")
    gdr_bw.add_bullet_p_bw(doc, "FirebaseAuth.getInstance().getCurrentUser() is evaluated. If null, execution transfers to GetStartedActivity for onboarding, login, or registration. If a cached authentication token exists, the user proceeds to the security verification stage.", bold_prefix="Step 2.0 - User Session Validation: ")
    gdr_bw.add_bullet_p_bw(doc, "The AppLockManager checks UserPrefs.isAppLockEnabled() and UserPrefs.isFingerprintEnabled(). If active and the session is locked, AppLockActivity is forced to the front. The user must provide a valid 4-digit PIN or fingerprint scan. If the Duress PIN is detected, a Silent SOS is dispatched covertly.", bold_prefix="Step 3.0 - App Lock & Security Gateway: ")
    gdr_bw.add_bullet_p_bw(doc, "Upon clearing the security barrier, the user reaches MainActivity. The dashboard binds the user's critical medical ID card (blood group, allergies, conditions), live battery percentage, administrative announcements, and safety highlights.", bold_prefix="Step 4.0 - Main Dashboard (MainActivity): ")
    gdr_bw.add_bullet_p_bw(doc, "Users can navigate to five core subsystems: (1) OneTap SOS emergency triggering, (2) Emergency Rooms & Live GPS tracking, (3) WebRTC Voice & Video Intercom, (4) Nearby Hospitals & Navigation, or (5) Incident Reporting & News.", bold_prefix="Step 5.0 - Subsystem Dispatch: ")
    gdr_bw.add_bullet_p_bw(doc, "When the app enters the background or the user logs out, background listeners are safely detached, session state is cleared, and device tokens are synchronized.", bold_prefix="Step 6.0 - Session Lifecycle & Termination: ")

    gdr_bw.add_heading_2_bw(doc, "2.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t1_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t1_rows = [
        ["1.0 Launch", "SplashActivity", "System launch intent", "Theme application, permission probe, Firebase init", "Configured environment, transition to Auth check"],
        ["2.0 Auth Check", "FirebaseAuth", "Cached session token", "Evaluation of getCurrentUser() != null", "Route to AppLock or GetStartedActivity"],
        ["3.0 App Lock", "AppLockManager", "4-digit PIN / Biometric scan", "Credential comparison against local hash / Duress PIN", "App unlocked / Silent SOS triggered / Lockout"],
        ["4.0 Dashboard", "MainActivity", "User profile & RTDB state", "Render medical profile, battery level, broadcast cards", "Interactive dashboard ready for user action"],
        ["5.0 Dispatch", "Intent Dispatcher", "User navigation touch event", "Activity intent resolution & parameter bundling", "Transition into requested functional module"],
        ["6.0 Lifecycle", "BaseActivity & UserPrefs", "onDestroy() / Exit intent", "Detach database listeners & stop foreground services", "Clean shutdown or secure background idle state"]
    ]
    gdr_bw.create_styled_table_bw(doc, t1_headers, t1_rows, col_widths=[1.0, 1.2, 1.3, 1.4, 1.3])

    gdr_bw.add_heading_2_bw(doc, "2.3 Offline Resilience & Network Degradation")
    gdr_bw.add_body_p_bw(doc, 
        "If network connectivity is lost during launch, Firebase Realtime Database disk persistence ensures the user's cached medical profile "
        "remains immediately accessible. Emergency telephone numbers and local emergency contacts remain fully operational offline, "
        "allowing immediate cellular dialing even in total network dead zones.")

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.app.SplashActivity, com.example.resqtap.home.MainActivity, "
        "com.example.resqtap.app.BaseActivity, com.example.resqtap.security.AppLockManager, com.example.resqtap.utils.UserPrefs.", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 3: FLOWCHART 2 - AUTH, APP LOCK & DURESS PIN (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "3. SECTION 2: FLOWCHART OF AUTHENTICATION, APP LOCK & DURESS PIN SAFEGUARD")
    
    gdr_bw.add_body_p_bw(doc, 
        "The security subsystem of ResQTap provides defense-in-depth against unauthorized device access while introducing an "
        "innovative covert protection mechanism: the Duress PIN. In situations where a victim is coerced by an aggressor to unlock their "
        "device, entering this designated passkey triggers an invisible, high-priority emergency protocol while outwardly displaying a normal app interface.")

    # Embed B&W Flowchart Image 2
    fc2_path = os.path.join(DOCS_DIR, "flowchart_2_auth_security_bw.png")
    if os.path.exists(fc2_path):
        doc.add_picture(fc2_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 3.1: Flowchart of User Authentication, App Lock Gateway and Duress PIN Safeguard (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "3.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "If no active session exists, GetStartedActivity provides options for Login (email and password), Registration (full name, email, phone, blood group, allergies, chronic conditions), or Forgot Password (password reset link dispatch via Firebase Auth).", bold_prefix="Step 1.0 - Onboarding & Authentication Flow: ")
    gdr_bw.add_bullet_p_bw(doc, "Upon successful Firebase Auth verification, user profile data, vital medical records, and the device FCM token are persisted to the /users/{uid} node in Firebase Realtime Database.", bold_prefix="Step 2.0 - Profile & Medical Record Storage: ")
    gdr_bw.add_bullet_p_bw(doc, "Whenever the app transitions from background to foreground, AppLockManager intercepts the lifecycle event and queries UserPrefs. If App Lock or Biometrics are enabled, AppLockActivity is displayed immediately over the current view.", bold_prefix="Step 3.0 - AppLockManager Lifecycle Interception: ")
    gdr_bw.add_bullet_p_bw(doc, "The lock screen presents a 4-digit numeric keypad and a fingerprint scan option. When credentials are submitted, the system performs a three-way cryptographic comparison.", bold_prefix="Step 4.0 - Credential Evaluation: ")
    gdr_bw.add_bullet_p_bw(doc, "If the PIN matches UserPrefs.getAppLockPin() or a valid fingerprint is scanned, AppLockManager.setUnlocked(true) is invoked, granting standard access to MainActivity.", bold_prefix="Step 4.1 - Standard Access Granted: ")
    gdr_bw.add_bullet_p_bw(doc, "If the entered PIN matches UserPrefs.getDuressPin(), the covert Duress Protocol is engaged: A Silent SOS alert is immediately written to Firebase RTDB with user coordinates. ZERO sirens sound, ZERO strobe lights flash, and NO on-screen warnings appear. The app unlocks to a normal view to conceal the alarm from the assailant.", bold_prefix="Step 4.2 - Duress PIN Protocol (Silent SOS): ")
    gdr_bw.add_bullet_p_bw(doc, "If an incorrect PIN is entered, haptic error feedback is generated and a failed attempt counter increments. If failed attempts exceed 5 consecutive times, a 30-second cooldown timer locks the keypad to prevent brute-force attacks.", bold_prefix="Step 4.3 - Anti-Brute-Force Lockout: ")

    gdr_bw.add_heading_2_bw(doc, "3.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t2_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t2_rows = [
        ["1.0 Auth Flow", "LoginActivity / RegisterActivity", "Email, password, medical attributes", "Firebase Authentication credentials validation", "User authenticated; token issued"],
        ["2.0 Profile Store", "Firebase RTDB", "Demographics & emergency notes", "Write payload to /users/{uid} in RTDB", "User record synchronized across devices"],
        ["3.0 Intercept", "AppLockManager", "onActivityStarted lifecycle event", "Inspect lock flags and session unlocked state", "Display AppLockActivity if locked"],
        ["4.1 Normal PIN", "AppLockActivity", "Correct 4-digit PIN / Biometrics", "Match against local passkey hash", "Session unlocked; navigate to dashboard"],
        ["4.2 Duress PIN", "AppLockActivity & RTDB", "Coerced 4-digit Duress PIN", "Detect match -> Write Silent SOS alert to RTDB", "Covert SOS transmitted; camouflage app unlocked"],
        ["4.3 Lockout", "AppLockActivity", "Incorrect 4-digit PIN", "Increment failure counter; if >= 5 initiate 30s timer", "Keypad locked; countdown displayed"]
    ]
    gdr_bw.create_styled_table_bw(doc, t2_headers, t2_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.auth.LoginActivity, com.example.resqtap.auth.RegisterActivity, "
        "com.example.resqtap.security.AppLockActivity, com.example.resqtap.security.AppLockManager, "
        "com.example.resqtap.security.SetPinActivity, com.example.resqtap.utils.UserPrefs.", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 4: FLOWCHART 3 - ONETAP SOS DISPATCH (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "4. SECTION 3: FLOWCHART OF ONETAP SOS EMERGENCY TRIGGER & INCIDENT DISPATCH")
    
    gdr_bw.add_body_p_bw(doc, 
        "The OneTap SOS module is the core emergency feature of ResQTap. Engineered for high-stress situations, it triggers a "
        "multi-channel alert with a single tap while providing an interruptible 3-second countdown window to prevent false alarms. "
        "Once verified, it initiates physical hardware alarms and broadcasts telemetric emergency packets across trusted rooms, "
        "emergency responders, and the web administration console.")

    # Embed B&W Flowchart Image 3
    fc3_path = os.path.join(DOCS_DIR, "flowchart_3_onetap_sos_bw.png")
    if os.path.exists(fc3_path):
        doc.add_picture(fc3_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 4.1: Flowchart of OneTap SOS Emergency Trigger, Cloud Blasting and Live Response Timeline (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "4.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "The user taps the prominent circular SOS button on MainActivity. The system immediately displays SosBottomSheetController containing a visual countdown and a swipeable slider.", bold_prefix="Step 1.0 - OneTap SOS Button Press: ")
    gdr_bw.add_bullet_p_bw(doc, "A 3-second CountDownTimer starts. If pressed by accident, the user can slide the cancel bar (Progress >= 95%). The timer halts immediately, the sheet dismisses, and no alert is transmitted to the cloud.", bold_prefix="Step 2.0 - 3-Second Countdown & Cancellation: ")
    gdr_bw.add_bullet_p_bw(doc, "If the timer elapses to 0 seconds, emergency state is confirmed! Three physical hardware alert systems activate simultaneously: (1) SosAudioManager plays a high-decibel siren at maximum volume, (2) the camera flash rapidly strobes to attract physical attention, and (3) VibrateManager executes a continuous rhythmic vibration pattern.", bold_prefix="Step 3.0 - Local Hardware Alarms Activated: ")
    gdr_bw.add_bullet_p_bw(doc, "FusedLocationProviderClient captures high-accuracy GPS coordinates (Latitude, Longitude), current battery level, and user identity, querying active room memberships from /userRooms/{uid}.", bold_prefix="Step 4.0 - Telemetry & Room Identification: ")
    gdr_bw.add_bullet_p_bw(doc, "If the user belongs to active rooms, an alert payload is dispatched to /rooms/{roomCode}/sosAlerts for each room. If the user has no rooms, a fallback mechanism transmits the alert to /rooms/DIRECT/sosAlerts and the global /sos_alerts queue.", bold_prefix="Step 5.0 - Multi-Room & Direct Channel Blasting: ")
    gdr_bw.add_bullet_p_bw(doc, "Writing to RTDB triggers Firebase Cloud Functions (onRoomSosCreated and onSosAlertCreated). The server extracts FCM device tokens for all room members and administrators, dispatching High-Priority FCM Push Notifications.", bold_prefix="Step 6.0 - Cloud Functions Automated Blasting: ")
    gdr_bw.add_bullet_p_bw(doc, "Receiving devices intercept the FCM payload and launch SosAlarmActivity over the lock screen (USE_FULL_SCREEN_INTENT). Members hear the alarm and can choose to: Snooze the Siren, Open RoomMapActivity to view live tracking, or Initiate a Call.", bold_prefix="Step 7.1 - Responder / Member Reaction: ")
    gdr_bw.add_bullet_p_bw(doc, "Simultaneously, the incident appears on the Web Admin Console. An operator reserves the incident (served = true, servedBy = adminUid) and advances the response stage (Dispatched -> En Route -> On Scene -> Resolved).", bold_prefix="Step 7.2 - Admin Console Reservation: ")
    gdr_bw.add_bullet_p_bw(doc, "The victim's device, monitoring the alert via watchSosCaseReservation(), detects the admin reservation and automatically transitions from the SOS trigger sheet to SosProgressActivity, displaying responder details and the 4-step progress timeline.", bold_prefix="Step 8.0 - Live Response Timeline (SosProgressActivity): ")
    gdr_bw.add_bullet_p_bw(doc, "When the incident is resolved or cancelled, onRoomSosUpdated transmits an SOS_CANCELLED broadcast to all devices. Sirens silence, hardware strobes extinguish, and tracking services reset.", bold_prefix="Step 9.0 - Incident Resolution: ")

    gdr_bw.add_heading_2_bw(doc, "4.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t3_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t3_rows = [
        ["1.0-2.0 Countdown", "SosBottomSheetController", "SOS button tap / Slider drag", "CountDownTimer 3s & progress >= 95% evaluation", "SOS confirmed or cancelled"],
        ["3.0 Hardware Alarms", "SosAudioManager & VibrateManager", "SOS confirmation event", "Activate max audio stream, camera strobe, vibration", "High-intensity physical alarm output"],
        ["4.0 Telemetry", "FusedLocationProviderClient", "GPS satellite signal & device battery", "Fetch Lat/Lng coordinates & query /userRooms", "Assembled emergency telemetry payload"],
        ["5.0 Cloud Publish", "FirebaseRoomClient", "Emergency JSON payload", "Write to /rooms/{code}/sosAlerts or DIRECT channel", "Persistent active alert records in RTDB"],
        ["6.0 Cloud Functions", "onRoomSosCreated", "RTDB onCreate trigger", "Aggregate FCM device tokens & dispatch push messages", "High-priority push notifications dispatched"],
        ["7.1 Responders", "SosAlarmActivity", "High-priority FCM alert payload", "Display full-screen over lock screen; sound alarm", "Responders alerted; interactive response options"],
        ["7.2 & 8.0 Timeline", "Admin Console & SosProgressActivity", "Admin reservation (served = true)", "Advance progressStep (1 to 4) in RTDB", "Victim views live responder progress on screen"],
        ["9.0 Resolution", "onRoomSosUpdated", "Resolution action (status: resolved)", "Broadcast cancellation event; terminate alarms", "Incident formally closed; hardware released"]
    ]
    gdr_bw.create_styled_table_bw(doc, t3_headers, t3_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.sos.SosBottomSheetController, com.example.resqtap.sos.SosAlarmActivity, "
        "com.example.resqtap.sos.SosAudioManager, com.example.resqtap.sos.VibrateManager, com.example.resqtap.sos.SosProgressActivity, "
        "com.example.resqtap.room.FirebaseRoomClient, firebase-functions/index.js (onRoomSosCreated, onRoomSosUpdated).", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 5: FLOWCHART 4 - EMERGENCY ROOMS & GPS (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "5. SECTION 4: FLOWCHART OF EMERGENCY ROOM HUB & LIVE GPS TRACKING SERVICE")
    
    gdr_bw.add_body_p_bw(doc, 
        "The Emergency Room Hub enables users to form trusted circles of family, friends, or specialized response teams. "
        "It provides continuous background location broadcasting through an Android Foreground Service and real-time "
        "visual marker synchronization on Google Maps.")

    # Embed B&W Flowchart Image 4
    fc4_path = os.path.join(DOCS_DIR, "flowchart_4_emergency_room_map_bw.png")
    if os.path.exists(fc4_path):
        doc.add_picture(fc4_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 5.1: Flowchart of Emergency Room Hub, Code/QR Sharing and Live GPS Tracking (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "5.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "Users access RoomListActivity to view their active emergency groups or initiate actions to create or join a circle.", bold_prefix="Step 1.0 - Room Hub Access: ")
    gdr_bw.add_bullet_p_bw(doc, "When creating a room, the user provides a room name. The system generates a unique 6-character alphanumeric code (e.g. RT9482) via RoomCodeUtils, registers it under /rooms/{code}, and assigns the user as room owner.", bold_prefix="Step 2.1 - Room Creation: ")
    gdr_bw.add_bullet_p_bw(doc, "When joining an existing room, users can enter the 6-character code manually or scan the room's QR code using ScanQrActivity. The code is verified in RTDB; if valid, the user UID is registered under /rooms/{code}/members/{uid}.", bold_prefix="Step 2.2 - Joining a Room (Code / QR): ")
    gdr_bw.add_bullet_p_bw(doc, "Inside RoomHubActivity, the user sees member avatars, live presence (Online/Offline), battery percentage, and the latest location timestamp for each member.", bold_prefix="Step 3.0 - Room Dashboard (RoomHubActivity): ")
    gdr_bw.add_bullet_p_bw(doc, "Users can tap 'Send Bell' on any member's profile. This writes a record to /rooms/{code}/bells/{toUid}, triggering the onBellCreated Cloud Function to send an instant haptic FCM ping to that specific device.", bold_prefix="Step 4.1 - Instant Bell / Ping Alert: ")
    gdr_bw.add_bullet_p_bw(doc, "RoomChatActivity provides an encrypted in-room chat channel where members can exchange text messages and photo attachments during emergency coordination.", bold_prefix="Step 4.2 - Group Room Chat: ")
    gdr_bw.add_bullet_p_bw(doc, "Opening RoomMapActivity starts LiveRoomTrackingService as an Android Foreground Service (location|dataSync). The service holds a partial WakeLock and queries FusedLocationProviderClient continuously, publishing Latitude, Longitude, speed, and battery to RTDB.", bold_prefix="Step 5.0 - Foreground Background Tracking Service: ")
    gdr_bw.add_bullet_p_bw(doc, "The Google Maps SDK renders custom avatar markers for all room members. Positions update smoothly on screen as members move. Tapping a member marker displays their profile card, distance (km), and a navigation shortcut.", bold_prefix="Step 6.0 - Interactive Google Maps SDK Interface: ")

    gdr_bw.add_heading_2_bw(doc, "5.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t4_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t4_rows = [
        ["1.0 Room List", "RoomListActivity", "Query /userRooms/{uid}", "Fetch user room subscriptions from RTDB", "List of active rooms displayed in RecyclerView"],
        ["2.1 Create Room", "RoomCodeUtils & RTDB", "Room name & owner UID", "Generate 6-char code; create /rooms/{code}", "New room created; user enrolled as owner"],
        ["2.2 Join Room", "ScanQrActivity", "QR camera scan / 6-char input", "Verify room existence; register member in RTDB", "User joined to room member list"],
        ["3.0 Hub Roster", "RoomHubActivity", "Member data from /rooms/{code}", "Listen for live presence & battery changes", "Live roster with telemetry status displayed"],
        ["4.1 Bell Ping", "onBellCreated Cloud Function", "Tap 'Send Bell' on member card", "Trigger FCM notification with high-priority ping", "Target device buzzes with instant haptic alert"],
        ["4.2 Room Chat", "RoomChatActivity", "Text input / camera photos", "Write message object to /rooms/{code}/messages", "Synchronized chat messages displayed"],
        ["5.0 Tracking", "LiveRoomTrackingService", "LocationRequest sensor updates", "Publish Lat/Lng/Battery to RTDB periodically", "Live coordinates broadcast from foreground"],
        ["6.0 Map Display", "RoomMapActivity", "Member coordinates from RTDB", "Render custom avatar markers & polyline tracks", "Live interactive map showing all circle members"]
    ]
    gdr_bw.create_styled_table_bw(doc, t4_headers, t4_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.room.RoomListActivity, com.example.resqtap.room.RoomHubActivity, "
        "com.example.resqtap.room.RoomMapActivity, com.example.resqtap.room.LiveRoomTrackingService, "
        "com.example.resqtap.room.RoomChatActivity, com.example.resqtap.room.FirebaseRoomClient, "
        "com.example.resqtap.friend.ScanQrActivity, com.example.resqtap.friend.QrCodeUtils.", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 6: FLOWCHART 5 - WEBRTC CALLS (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "6. SECTION 5: FLOWCHART OF WEBRTC VOICE & VIDEO EMERGENCY INTERCOM")
    
    gdr_bw.add_body_p_bw(doc, 
        "ResQTap integrates WebRTC (Web Real-Time Communication) to provide direct, low-latency, two-way audio and video intercom "
        "between victims, emergency room members, and first responders. Firebase Realtime Database serves as the signaling plane "
        "to exchange Session Description Protocol (SDP) offers, answers, and ICE network candidates.")

    # Embed B&W Flowchart Image 5
    fc5_path = os.path.join(DOCS_DIR, "flowchart_5_webrtc_call_bw.png")
    if os.path.exists(fc5_path):
        doc.add_picture(fc5_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 6.1: Flowchart of WebRTC Signaling Protocol and Peer-to-Peer Intercom (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "6.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "The caller initiates a call from SosProgressActivity, RoomHubActivity, or ChatActivity by selecting Voice Call (Audio) or Video Call (Camera). The system creates a unique Call ID record under /calls/{callId} with status 'ringing'.", bold_prefix="Step 1.0 - Call Initiation: ")
    gdr_bw.add_bullet_p_bw(doc, "CallService launches as a Foreground Service (microphone|camera permissions). WebRTCClient creates a local SDP Offer containing media capabilities and uploads it to /calls/{callId}/offer in RTDB.", bold_prefix="Step 2.0 - Signaling Client & SDP Offer: ")
    gdr_bw.add_bullet_p_bw(doc, "The recipient detects the incoming call via an FCM push notification or RTDB event listener. IncomingCallManager displays IncomingCallActivity over the lock screen with a loud ringtone and vibration.", bold_prefix="Step 3.0 - Incoming Call Detection: ")
    gdr_bw.add_bullet_p_bw(doc, "The recipient has 3 choices: (1) Decline, setting status to 'declined'; (2) Allow the call to ring out for 30 seconds, setting status to 'timeout'; or (3) Accept, setting status to 'connected'.", bold_prefix="Step 4.0 - Recipient Decision: ")
    gdr_bw.add_bullet_p_bw(doc, "Upon acceptance, the recipient generates an SDP Answer and writes it to /calls/{callId}/answer. Both devices then generate and exchange ICE Candidates via /calls/{callId}/iceCandidates. A direct P2P mesh connection is established.", bold_prefix="Step 5.0 - WebRTC Handshake (SDP & ICE): ")
    gdr_bw.add_bullet_p_bw(doc, "Direct two-way audio and video streaming begins without server media relay. The user can toggle Microphone Mute, Speakerphone On/Off, and Front/Rear Camera switch.", bold_prefix="Step 6.0 - Active Media Streaming: ")
    gdr_bw.add_bullet_p_bw(doc, "When either party taps 'End Call', status is set to 'ended' in RTDB. CallService stops, camera and microphone hardware are released, and the PeerConnection closes cleanly.", bold_prefix="Step 7.0 - Call Teardown & Resource Disposal: ")

    gdr_bw.add_heading_2_bw(doc, "6.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t5_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t5_rows = [
        ["1.0 Initiation", "CallSignalingClient", "Call mode (voice/video)", "Create record /calls/{callId} status 'ringing'", "New active call node initialized in RTDB"],
        ["2.0 SDP Offer", "WebRTCClient & CallService", "Local audio/video stream", "Generate SDP Offer & upload to RTDB", "Connection offer published for recipient"],
        ["3.0 Detection", "IncomingCallActivity", "FCM push event / RTDB listener", "Display incoming call view over lock screen", "Ringtone and vibration alerting recipient"],
        ["4.0 Response", "IncomingCallManager", "Accept / Decline button tap", "Update call status in RTDB (connected/declined)", "Route to active call or terminate"],
        ["5.0 Handshake", "WebRTC PeerConnection", "SDP Answer & ICE Candidates", "P2P hole punching & candidate negotiation", "Direct peer-to-peer media pipeline established"],
        ["6.0 Streaming", "VoiceCall / VideoCallActivity", "Live microphone & camera frames", "Low-latency streaming & interactive controls", "Real-time emergency audio/video communication"],
        ["7.0 Teardown", "CallService & WebRTCClient", "End Call button tap", "Close PeerConnection; terminate foreground service", "Hardware drivers safely released"]
    ]
    gdr_bw.create_styled_table_bw(doc, t5_headers, t5_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.call.CallSignalingClient, com.example.resqtap.call.WebRTCClient, "
        "com.example.resqtap.call.CallService, com.example.resqtap.call.IncomingCallActivity, "
        "com.example.resqtap.call.IncomingCallManager, com.example.resqtap.call.VoiceCallActivity, "
        "com.example.resqtap.call.VideoCallActivity.", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 7: FLOWCHART 6 - INCIDENT REPORTING & HOSPITALS (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "7. SECTION 6: FLOWCHART OF COMMUNITY INCIDENT REPORTING & NEARBY HOSPITAL FINDER")
    
    gdr_bw.add_body_p_bw(doc, 
        "This section encompasses two essential citizen services: (1) Community Incident Reporting (ReportActivity), allowing "
        "users to file emergency reports with photographic evidence and automatic geocoding; and (2) Nearby Hospital Finder (NearbyHospitalActivity), "
        "which calculates distances to healthcare facilities and provides instant emergency hotline calling and navigation.")

    # Embed B&W Flowchart Image 6
    fc6_path = os.path.join(DOCS_DIR, "flowchart_6_report_hospital_bw.png")
    if os.path.exists(fc6_path):
        doc.add_picture(fc6_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 7.1: Flowchart of Community Incident Reporting and Nearby Hospital Finder (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "7.1 Community Incident Reporting Flow (ReportActivity)")
    gdr_bw.add_bullet_p_bw(doc, "The user opens ReportActivity. The incident submission form loads, verifying Camera and Location permissions.", bold_prefix="Step L1.0 - Form Initialization: ")
    gdr_bw.add_bullet_p_bw(doc, "The user selects an incident category: Road Accident, Violent Crime, Medical Emergency, Fire Hazard, Natural Disaster, or Other.", bold_prefix="Step L2.0 - Category Selection: ")
    gdr_bw.add_bullet_p_bw(doc, "The user enters a detailed description. Current GPS coordinates are acquired automatically as the incident geocode.", bold_prefix="Step L3.0 - Details & Location Capture: ")
    gdr_bw.add_bullet_p_bw(doc, "Users may attach a photo via the device camera or gallery. The image is compressed and uploaded to Firebase Cloud Storage under /incident_reports/{reportId}.jpg, returning a secure public download URL.", bold_prefix="Step L4.0 - Photo Compression & Upload: ")
    gdr_bw.add_bullet_p_bw(doc, "The complete incident payload is written to /reports/{reportId} in RTDB. A submission receipt and tracking ticket are generated for the user.", bold_prefix="Step L5.0 & L6.0 - Record Persistence & Ticket Generation: ")

    gdr_bw.add_heading_2_bw(doc, "7.2 Nearby Hospital Finder Flow (NearbyHospitalActivity)")
    gdr_bw.add_bullet_p_bw(doc, "The user launches NearbyHospitalActivity. The system queries FusedLocationProviderClient to determine current user Latitude and Longitude.", bold_prefix="Step H1.0 & H2.0 - GPS Location Scan: ")
    gdr_bw.add_bullet_p_bw(doc, "A geospatial query is submitted to medical facility APIs (Overpass API / Google Places API) to retrieve all hospitals, clinics, and trauma centers within radius.", bold_prefix="Step H3.0 - Geospatial Facility Query: ")
    gdr_bw.add_bullet_p_bw(doc, "The system calculates distances between user coordinates and each facility using the Haversine formula, sorting the directory in ascending order of proximity.", bold_prefix="Step H4.0 - Haversine Distance Calculation & Sorting: ")
    gdr_bw.add_bullet_p_bw(doc, "The directory renders facility cards displaying name, address, distance (km), open status, and emergency telephone number.", bold_prefix="Step H5.0 - Hospital Directory Presentation: ")
    gdr_bw.add_bullet_p_bw(doc, "The user can: (1) Tap 'Call' to launch the Android Dialer with the hospital hotline, or (2) Tap 'Navigate' to launch Google Maps with turn-by-turn driving directions.", bold_prefix="Step H6.0 - Direct Dialing or Map Navigation: ")

    gdr_bw.add_heading_2_bw(doc, "7.3 Entity, Input, Processing, and Output (IPO) Matrix")
    t6_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t6_rows = [
        ["L1.0-L3.0 Form", "ReportActivity", "Category, description, GPS", "Form validation & automatic geocode acquisition", "Validated incident data bundle"],
        ["L4.0 Storage", "Firebase Cloud Storage", "Camera photo / Gallery image", "Image compression & upload to cloud bucket", "Secure image download URL returned"],
        ["L5.0-L6.0 Ticket", "Firebase RTDB (/reports)", "Complete incident JSON object", "Record persistence & notification to admin console", "Incident ticket generated & confirmed"],
        ["H1.0-H2.0 GPS", "FusedLocationProviderClient", "GPS satellite signal / network", "Acquire precise user Lat/Lng coordinates", "Current user coordinates ready for query"],
        ["H3.0-H4.0 Query", "Overpass / Places API", "Search radius & user Lat/Lng", "Haversine distance calculation & ascending sort", "Ranked list of nearest healthcare facilities"],
        ["H5.0-H6.0 Action", "Dialer & Google Maps", "User selection of hospital card", "Trigger ACTION_DIAL intent or ACTION_VIEW geo-URI", "Emergency phone call or turn-by-turn navigation"]
    ]
    gdr_bw.create_styled_table_bw(doc, t6_headers, t6_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: com.example.resqtap.report.ReportActivity, com.example.resqtap.report.SupportActivity, "
        "com.example.resqtap.map.NearbyHospitalActivity, com.example.resqtap.map.GpsUtils, "
        "com.example.resqtap.news.MedicalNewsActivity, com.example.resqtap.news.MedicalNewsFetcher.", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 8: FLOWCHART 7 - ADMIN PORTAL & BROADCASTING (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "8. SECTION 7: FLOWCHART OF WEB ADMIN PORTAL & CLOUD BROADCAST SYSTEM")
    
    gdr_bw.add_body_p_bw(doc, 
        "The Web Administration Portal serves as the centralized command-and-control dashboard for emergency dispatchers and system operators. "
        "Hosted via a Node.js server (server.js) and frontend dashboard (admin.html), it enables live incident monitoring, operator reservation, "
        "targeted FCM broadcast dispatches, and account security management.")

    # Embed B&W Flowchart Image 7
    fc7_path = os.path.join(DOCS_DIR, "flowchart_7_admin_portal_cloud_bw.png")
    if os.path.exists(fc7_path):
        doc.add_picture(fc7_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr_bw.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Figure 8.1: Flowchart of Admin Authentication, Live SOS Incident Control and Targeted Cloud Broadcasts (Monochrome)")
        r_cap.font.name = gdr_bw.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr_bw.COLOR_MID_GRAY

    gdr_bw.add_heading_2_bw(doc, "8.1 Step-by-Step Logic Explanation")
    gdr_bw.add_bullet_p_bw(doc, "Administrators access the web console via browser (http://localhost:5000/admin or production URL), authenticating with Firebase Auth credentials.", bold_prefix="Step 1.0 - Portal Access & Login: ")
    gdr_bw.add_bullet_p_bw(doc, "The system verifies admin privileges. Accounts with official @resqtap.com domains receive automatic admin promotion via the onUserCreated Cloud Function. The system also verifies the /admins/{uid} node in RTDB. If unauthorized, access is denied (403 Forbidden).", bold_prefix="Step 2.0 & 3.0 - Privilege Verification: ")
    gdr_bw.add_bullet_p_bw(doc, "The main dashboard (admin.html) presents live telemetry: Total Users, Active Emergency Rooms, Active SOS Alerts, and server health metrics.", bold_prefix="Step 4.0 - Live Surveillance Dashboard: ")
    gdr_bw.add_bullet_p_bw(doc, "The console listens to /rooms/*/sosAlerts. When an alert arrives, flashing indicators and audio chimes activate. The operator clicks 'Reserve Case' (served = true, servedBy = adminUid) and advances the progress timeline (1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved).", bold_prefix="Step 4.1 - Live SOS Incident Management: ")
    gdr_bw.add_bullet_p_bw(doc, "Operators can broadcast emergency announcements. The operator selects the audience ('all' for all registered users, 'live' for active room members within the past 2 minutes, or 'admins'), inputs title and message, and invokes the sendAdminNotification Callable Cloud Function.", bold_prefix="Step 4.2 - Targeted Broadcast System: ")
    gdr_bw.add_bullet_p_bw(doc, "The cloud function aggregates target device FCM tokens, simultaneously broadcasts high-priority push notifications, and records delivery metrics (sent count, failures, timestamps) under /admin_notifications/{historyId}.", bold_prefix="Step 4.2b - FCM Cloud Blasting Engine: ")
    gdr_bw.add_bullet_p_bw(doc, "Operators maintain security by deleting compromised accounts via /admin_user_deletions/{uid}, triggering onAdminUserDeletionRequest to purge Auth and RTDB records completely, or triggering database account reset via /api/admin/clear-database.", bold_prefix="Step 4.3 - Account Purging & Database Maintenance: ")

    gdr_bw.add_heading_2_bw(doc, "8.2 Entity, Input, Processing, and Output (IPO) Matrix")
    t7_headers = ["Step", "Entity / Component", "Input Data", "Processing Operations", "Output / Result"]
    t7_rows = [
        ["1.0-2.0 Auth", "server.js & admin.html", "Admin email and password", "Firebase Auth verify & check @resqtap.com domain", "Admin dashboard access granted or 403 denied"],
        ["3.0 Telemetry", "Admin Dashboard (app.js)", "RTDB snapshot /users, /rooms, /sosAlerts", "Compute live metrics & update statistical cards", "Real-time system health and alert count rendered"],
        ["4.1 SOS Control", "Firebase RTDB & Console", "Click 'Reserve Case' & update stage", "Write served=true & advance progressStep (1-4)", "Status changes synced to victim's screen in real time"],
        ["4.2 Broadcast", "sendAdminNotification", "Audience (all/live/admins), title & message", "Callable Cloud Function queries tokens & blasts FCM", "Official announcement delivered to target devices"],
        ["4.3 Purge", "onAdminUserDeletionRequest", "Target UID deletion request", "Purge user from Firebase Auth & RTDB paths", "Target account completely wiped from system"],
        ["5.0 Maintenance", "Database Maintenance API", "Database maintenance / reset request", "cleanup_rtdb.js executes cleanup logic", "Database maintained clean, performant, and secure"]
    ]
    gdr_bw.create_styled_table_bw(doc, t7_headers, t7_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr_bw.add_body_p_bw(doc, 
        "• Source Code References: ResQTap-Website/server.js, ResQTap-Website/public/admin.html, "
        "ResQTap-Website/public/app.js, cleanup_rtdb.js, firebase-functions/index.js "
        "(sendAdminNotification, onUserCreated, onAdminUserDeletionRequest, cleanupExpiredAiChats).", 
        bold_prefix="Source Code References: ")

    doc.add_page_break()

    # =========================================================================
    # SECTION 9: ARCHITECTURAL CONCLUSION & SUMMARY (B&W)
    # =========================================================================
    gdr_bw.add_heading_1_bw(doc, "9. ARCHITECTURAL CONCLUSION & SUMMARY MATRIX")
    
    gdr_bw.add_body_p_bw(doc, 
        "This specification document demonstrates that the ResQTap (OneTapSOS) architecture is engineered for exceptional reliability, "
        "fault-tolerance, and responsiveness during life-critical emergencies. By leveraging Firebase Realtime Database and Cloud Functions "
        "as a reactive event bus, combined with native Android foreground tracking and WebRTC peer-to-peer media streaming, the platform "
        "delivers enterprise-grade emergency response capabilities suitable for personal, family, and professional first responder applications.")

    sum_headers = ["Flowchart Section", "Module Focus", "Core Technical Components", "Key Safety & Performance Innovations"]
    sum_rows = [
        ["Section 1: Overall Flow", "Application lifecycle & navigation", "SplashActivity, MainActivity", "Automated session recovery & offline caching"],
        ["Section 2: Security & Auth", "Authentication, PIN & Biometrics", "AppLockManager, BiometricPrompt", "Covert Silent SOS via Duress PIN & brute-force lockout"],
        ["Section 3: OneTap SOS", "Emergency trigger & live timeline", "SosBottomSheet, Cloud Functions, FCM", "3s countdown, hardware alarms & admin reservation"],
        ["Section 4: Rooms & Tracking", "Emergency groups & live map", "LiveRoomTrackingService, Google Maps", "Foreground Service, QR sharing & instant Bell pings"],
        ["Section 5: WebRTC Calls", "P2P voice and video intercom", "CallSignalingClient, WebRTCClient", "Ultra-low latency, P2P mesh & lock screen ringing"],
        ["Section 6: Reports & Hospitals", "Incident reporting & hospital finder", "ReportActivity, FusedLocationClient", "Cloud image storage, Haversine formula & navigation"],
        ["Section 7: Admin Portal", "Command console & cloud broadcasting", "server.js, admin.html, FCM Blaster", "@resqtap.com auto-admin & full account purging"]
    ]
    gdr_bw.create_styled_table_bw(doc, sum_headers, sum_rows, col_widths=[1.4, 1.6, 1.6, 1.6])

    gdr_bw.add_callout_bw(doc, 
        "All 7 monochrome flowchart diagrams and engineering matrices in this document reflect the active production codebase of ResQTap. "
        "All visual diagrams have been generated in high resolution, strict Black and White, and permanently embedded in this document.",
        title="DOCUMENT VERIFICATION & SIGNOFF")

    # Save document
    doc.save(OUTPUT_DOCX)
    print(f"Monochrome document successfully created and saved to: {OUTPUT_DOCX}")
    print(f"Document file size: {os.path.getsize(OUTPUT_DOCX) / 1024:.1f} KB")

if __name__ == "__main__":
    build_monochrome_document()
