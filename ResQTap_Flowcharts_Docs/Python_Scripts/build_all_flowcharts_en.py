import os
from PIL import Image, ImageDraw
import generate_flowcharts as gf

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts_en")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ==============================================================================
# FLOWCHART 1: OVERALL SYSTEM WORKFLOW & ARCHITECTURE
# ==============================================================================
def draw_flowchart_1_en():
    W, H = 1600, 1220
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 1: OVERALL SYSTEM WORKFLOW & ARCHITECTURE", 
        "Comprehensive End-to-End Navigation, Application Security Gateways & Core Functional Modules")
    
    # Nodes
    gf.draw_pill(d, [700, 110, 900, 155], "START", is_start=True)
    gf.draw_process(d, [630, 185, 970, 260], "1.0 App Launch & Splash Screen", [
        "User launches ResQTap application",
        "SplashActivity checks permissions & Firebase config"
    ])
    gf.draw_decision(d, 800, 335, 290, 85, "Is User Session", ["Active & Authenticated?"])
    
    # Inactive Session -> Auth
    gf.draw_io_box(d, [150, 295, 490, 375], "2.0 Onboarding & Authentication", [
        "Login, Register, or Forgot Password flows",
        "Firebase Auth credential validation"
    ])
    
    # Active Session -> App Lock
    gf.draw_decision(d, 800, 465, 300, 85, "Is App Lock or", ["Biometrics Enabled?"])
    
    # App Lock Active
    gf.draw_process(d, [1140, 430, 1480, 500], "3.0 App Lock Gateway", [
        "Enter 4-Digit Security PIN or Fingerprint Scan",
        "Duress PIN: Covertly triggers Silent SOS"
    ])
    
    # Main Dashboard
    gf.draw_process(d, [620, 570, 980, 650], "4.0 Main Dashboard (MainActivity)", [
        "Displays Medical Profile, Battery Status & Alerts",
        "User selects emergency or community service"
    ])
    
    gf.draw_decision(d, 800, 725, 280, 80, "User Action", ["Selection?"])
    
    # 5 Functional Modules
    gf.draw_process(d, [60, 820, 320, 915], "ONETAP SOS", [
        "Instant SOS Trigger",
        "3-sec countdown & alarm",
        "Broadcasts to RTDB & FCM"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_process(d, [350, 820, 620, 915], "EMERGENCY ROOMS", [
        "Create / Join Room (Code / QR)",
        "LiveRoomTrackingService (GPS)",
        "Google Maps & Room Chat"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_process(d, [650, 820, 950, 915], "WEBRTC CALLS", [
        "Voice & Video Intercom",
        "WebRTC Signaling via RTDB",
        "Low-latency P2P Media Stream"
    ], border_color=(99, 102, 241), accent_color=(238, 242, 255))
    
    gf.draw_process(d, [980, 820, 1260, 915], "NEARBY HOSPITALS", [
        "GPS location scan",
        "Geospatial medical facility query",
        "Emergency dial & navigation"
    ], border_color=(14, 165, 233), accent_color=(240, 249, 255))
    
    gf.draw_process(d, [1290, 820, 1550, 915], "REPORTS & NEWS", [
        "Submit incident report + photo",
        "Upload to Cloud Storage",
        "Verified medical news feeds"
    ], border_color=(168, 85, 247), accent_color=(250, 245, 255))
    
    # Lifecycle / Exit
    gf.draw_process(d, [640, 990, 960, 1060], "5.0 Session Lifecycle & Logout", [
        "User completes activity or signs out",
        "Background services & session tokens synchronized"
    ])
    gf.draw_pill(d, [710, 1110, 890, 1155], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (800, 155), (800, 185))
    gf.draw_arrow(d, (800, 260), (800, 292))
    
    # Dec 2.0 (Active Session?)
    gf.draw_arrow(d, (655, 335), (490, 335), label="No", label_side="top")
    gf.draw_poly_arrow(d, [(320, 295), (320, 222), (630, 222)], label="Auth Success", label_side="top")
    gf.draw_arrow(d, (800, 377), (800, 422), label="Yes", label_side="right")
    
    # Dec 3.0 (App Lock?)
    gf.draw_arrow(d, (950, 465), (1140, 465), label="Yes", label_side="top")
    gf.draw_poly_arrow(d, [(1310, 500), (1310, 610), (980, 610)], label="Unlocked", label_side="bottom")
    gf.draw_arrow(d, (800, 507), (800, 570), label="No", label_side="right")
    
    gf.draw_arrow(d, (800, 650), (800, 685))
    
    # Dec 5.0 to 5 modules
    gf.draw_poly_arrow(d, [(660, 725), (190, 725), (190, 820)])
    gf.draw_poly_arrow(d, [(710, 750), (485, 750), (485, 820)])
    gf.draw_arrow(d, (800, 765), (800, 820))
    gf.draw_poly_arrow(d, [(890, 750), (1120, 750), (1120, 820)])
    gf.draw_poly_arrow(d, [(940, 725), (1420, 725), (1420, 820)])
    
    # From 5 modules to Lifecycle
    gf.draw_poly_arrow(d, [(190, 915), (190, 960), (700, 960), (700, 990)])
    gf.draw_poly_arrow(d, [(485, 915), (485, 960), (750, 960), (750, 990)])
    gf.draw_arrow(d, (800, 915), (800, 990))
    gf.draw_poly_arrow(d, [(1120, 915), (1120, 960), (850, 960), (850, 990)])
    gf.draw_poly_arrow(d, [(1420, 915), (1420, 960), (900, 960), (900, 990)])
    
    gf.draw_arrow(d, (800, 1060), (800, 1110))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_1_overall_architecture_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 2: AUTHENTICATION, APP LOCK & DURESS PIN
# ==============================================================================
def draw_flowchart_2_en():
    W, H = 1500, 1260
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 2: AUTHENTICATION, APP LOCK & DURESS PIN SAFEGUARD", 
        "Session Lifecycle, 4-Digit Passkey / Biometric Verification & Covert Silent SOS Protocol")
        
    gf.draw_pill(d, [650, 110, 850, 155], "START", is_start=True)
    gf.draw_process(d, [580, 185, 920, 260], "1.0 Launch SplashActivity", [
        "Initialize Firebase App & Check Auth State",
        "FirebaseAuth.getInstance().getCurrentUser()"
    ])
    gf.draw_decision(d, 750, 335, 280, 85, "Is User Session", ["Valid & Active?"])
    
    # Onboarding Flow
    gf.draw_io_box(d, [120, 295, 460, 375], "2.0 GetStarted & Auth Options", [
        "Login, Register New Account, or Reset Password",
        "Email & Password verification via Firebase"
    ])
    gf.draw_process(d, [120, 420, 460, 495], "2.1 Save User Profile Data", [
        "Save Demographics & Medical Card to RTDB",
        "Store FCM Device Token under /users/{uid}"
    ])
    
    # Active Session -> Check App Lock
    gf.draw_decision(d, 750, 465, 300, 85, "Is App Lock or", ["Fingerprint Enabled?"])
    
    gf.draw_process(d, [580, 570, 920, 645], "3.0 Display AppLockActivity", [
        "Presents secure 4-digit PIN keypad screen",
        "Provides BiometricPrompt fingerprint scanner option"
    ])
    
    gf.draw_decision(d, 750, 735, 290, 90, "Credential Evaluation", ["Input Match Type?"])
    
    # 1. Normal PIN / Biometrics OK (Center)
    gf.draw_process(d, [610, 850, 890, 925], "4.1 Access Granted (Normal)", [
        "AppLockManager.setUnlocked(true)",
        "App session unlocked for user"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    
    # 2. DURESS PIN (Right)
    gf.draw_subroutine(d, [990, 695, 1420, 795], "4.2 DURESS PIN PROTOCOL (SILENT SOS)", [
        "Matches UserPrefs.getDuressPin()",
        "Immediately writes Silent SOS Alert to RTDB",
        "NO sirens, NO strobe lights, NO on-screen alerts",
        "Unlocks standard app camouflage to protect user"
    ])
    
    # 3. Invalid PIN (Left)
    gf.draw_process(d, [80, 700, 440, 770], "4.3 Invalid PIN / Failed", [
        "Increment failed attempts counter",
        "Display haptic error feedback"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_decision(d, 260, 840, 240, 75, "Failed Attempts", ["> 5 Consecutive?"])
    gf.draw_process(d, [80, 930, 440, 995], "4.4 Temporary Cooldown Lockout", [
        "Locks app keypad for 30 seconds",
        "Mitigates brute-force passkey guessing"
    ], border_color=gf.C_STOP_BD)
    
    # Proceed to MainActivity
    gf.draw_process(d, [580, 1020, 920, 1095], "5.0 Navigate to MainActivity", [
        "Loads ResQTap Main Dashboard",
        "Emergency services ready in foreground"
    ])
    gf.draw_pill(d, [660, 1150, 840, 1195], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (750, 155), (750, 185))
    gf.draw_arrow(d, (750, 260), (750, 292))
    
    # Dec 2.0
    gf.draw_arrow(d, (610, 335), (460, 335), label="No", label_side="top")
    gf.draw_arrow(d, (290, 375), (290, 420))
    gf.draw_poly_arrow(d, [(460, 460), (510, 460), (510, 222), (580, 222)], label="Auth Success", label_side="top")
    gf.draw_arrow(d, (750, 377), (750, 422), label="Yes", label_side="right")
    
    # Dec 3.0
    gf.draw_poly_arrow(d, [(900, 465), (960, 465), (960, 1060), (920, 1060)], label="Disabled", label_side="top")
    gf.draw_arrow(d, (750, 507), (750, 570), label="Yes", label_side="right")
    gf.draw_arrow(d, (750, 645), (750, 690))
    
    # Dec 4.0
    gf.draw_arrow(d, (750, 780), (750, 850), label="Valid PIN / Bio", label_side="right")
    gf.draw_arrow(d, (750, 925), (750, 1020))
    
    # Duress
    gf.draw_arrow(d, (895, 735), (990, 735), label="Duress Match", label_side="top")
    gf.draw_poly_arrow(d, [(1205, 795), (1205, 1040), (920, 1040)], label="Camouflage App", label_side="right")
    
    # Invalid
    gf.draw_arrow(d, (605, 735), (440, 735), label="Wrong PIN", label_side="top")
    gf.draw_arrow(d, (260, 770), (260, 802))
    gf.draw_arrow(d, (260, 877), (260, 930), label="Yes", label_side="right")
    gf.draw_poly_arrow(d, [(80, 965), (40, 965), (40, 610), (580, 610)], label="Cooldown Expired", label_side="left")
    gf.draw_poly_arrow(d, [(380, 840), (520, 840), (520, 630), (580, 630)], label="No (Tries left)", label_side="top")
    
    gf.draw_arrow(d, (750, 1095), (750, 1150))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_2_auth_security_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 3: ONETAP SOS DISPATCH & MANAGEMENT
# ==============================================================================
def draw_flowchart_3_en():
    W, H = 1650, 1500
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 3: ONETAP SOS EMERGENCY TRIGGER & INCIDENT DISPATCH", 
        "Rapid Response Workflow: 3-Second Countdown, Hardware Alarms, Firebase RTDB/FCM Blasting & Live Dispatch Timeline")
        
    gf.draw_pill(d, [720, 110, 920, 155], "START", is_start=True)
    gf.draw_process(d, [630, 185, 1010, 260], "1.0 Tap OneTap SOS Button", [
        "User taps central SOS button on MainActivity",
        "Triggers modal SosBottomSheetController"
    ])
    gf.draw_decision(d, 820, 340, 320, 85, "3-Second Countdown", ["(CountDownTimer Active)"])
    
    # Slide to Cancel
    gf.draw_process(d, [1200, 305, 1530, 380], "2.0 Slide to Cancel", [
        "User slides cancellation bar (Progress >= 95%)",
        "Timer stopped & no alert dispatched to cloud"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    gf.draw_pill(d, [1310, 420, 1420, 460], "END", is_start=False)
    
    # Countdown Finished -> Alarm Activated
    gf.draw_process(d, [620, 440, 1020, 535], "3.0 Local Hardware Alarms Activated", [
        "Blasts Maximum Decibel Siren (SosAudioManager)",
        "Pulses Camera Flashlight Strobe continuously",
        "Triggers Rhythmic Haptic Vibration (VibrateManager)"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_io_box(d, [620, 570, 1020, 650], "4.0 Fetch GPS Location & User Rooms", [
        "fusedLocationClient.getLastLocation() -> Lat, Lng",
        "Query user's active rooms from /userRooms/{uid}"
    ])
    
    gf.draw_decision(d, 820, 725, 290, 80, "Does User Have", ["Active Rooms?"])
    
    # Dispatch branches
    gf.draw_subroutine(d, [350, 810, 740, 890], "5.1 Group Room Blasting", [
        "Publish alert to each /rooms/{code}/sosAlerts",
        "Include GPS coordinates, battery level & timestamp"
    ])
    gf.draw_subroutine(d, [900, 810, 1290, 890], "5.2 DIRECT Fallback Channel", [
        "Publish to fallback /rooms/DIRECT/sosAlerts",
        "Register alert in global /sos_alerts queue"
    ])
    
    # Cloud Functions Trigger
    gf.draw_subroutine(d, [590, 930, 1050, 1025], "6.0 Cloud Functions Automated Dispatch", [
        "Triggers onRoomSosCreated & onSosAlertCreated",
        "Collects FCM device tokens of all members & admins",
        "Blasts High-Priority FCM Push Notifications"
    ], border_color=gf.C_SUB_BD, bg_color=gf.C_SUB_BG)
    
    # Receivers and Admin
    gf.draw_process(d, [120, 1070, 560, 1165], "7.1 Room Members / Responders", [
        "Receive FCM -> Launch SosAlarmActivity over Lockscreen",
        "Full-screen siren, strobe & vibration alert",
        "Options: Snooze Alarm, View Live Map, Call Victim"
    ], border_color=(217, 119, 6), accent_color=(255, 251, 235))
    
    gf.draw_process(d, [1080, 1070, 1520, 1165], "7.2 Web Admin Dashboard", [
        "Flashing emergency alert banner on console",
        "Admin clicks 'Reserve Case' (served = true, servedBy)",
        "Updates Timeline: Dispatched -> En Route -> On Scene"
    ], border_color=gf.C_PROC_BD, accent_color=gf.C_PROC_ACCENT)
    
    # Victim progress tracking
    gf.draw_process(d, [600, 1205, 1040, 1300], "8.0 Live Response Timeline (SosProgressActivity)", [
        "watchSosCaseReservation() detects admin reservation",
        "Auto-launches 4-step interactive progress screen:",
        "1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved"
    ], border_color=(99, 102, 241), accent_color=(238, 242, 255))
    
    gf.draw_decision(d, 820, 1375, 290, 75, "Incident Resolved /", ["Cancelled?"])
    gf.draw_pill(d, [730, 1450, 910, 1490], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 297))
    
    # Dec 2.0
    gf.draw_arrow(d, (980, 340), (1200, 340), label="Slide Cancel", label_side="top")
    gf.draw_arrow(d, (1365, 380), (1365, 420))
    gf.draw_arrow(d, (820, 382), (820, 440), label="Countdown Expired (0s)", label_side="right")
    
    gf.draw_arrow(d, (820, 535), (820, 570))
    gf.draw_arrow(d, (820, 650), (820, 685))
    
    # Dec 5.0
    gf.draw_poly_arrow(d, [(675, 725), (545, 725), (545, 810)], label="Yes (Has Rooms)", label_side="top")
    gf.draw_poly_arrow(d, [(965, 725), (1095, 725), (1095, 810)], label="No (No Rooms)", label_side="top")
    
    gf.draw_poly_arrow(d, [(545, 890), (545, 910), (740, 910), (740, 930)])
    gf.draw_poly_arrow(d, [(1095, 890), (1095, 910), (900, 910), (900, 930)])
    
    # Cloud Functions to Receiver & Admin
    gf.draw_poly_arrow(d, [(660, 1025), (340, 1025), (340, 1070)], label="FCM to Members", label_side="top")
    gf.draw_poly_arrow(d, [(980, 1025), (1300, 1025), (1300, 1070)], label="RTDB Sync to Admin", label_side="top")
    
    # Both lead to Progress
    gf.draw_poly_arrow(d, [(340, 1165), (340, 1250), (600, 1250)])
    gf.draw_poly_arrow(d, [(1300, 1165), (1300, 1250), (1040, 1250)], label="Admin Reserved", label_side="top")
    
    gf.draw_arrow(d, (820, 1300), (820, 1337))
    gf.draw_arrow(d, (820, 1412), (820, 1450), label="Yes (Resolved)", label_side="right")
    
    path = os.path.join(OUTPUT_DIR, "flowchart_3_onetap_sos_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 4: EMERGENCY ROOMS & LIVE GPS TRACKING
# ==============================================================================
def draw_flowchart_4_en():
    W, H = 1600, 1300
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 4: EMERGENCY ROOM HUB & LIVE GPS TRACKING SERVICE", 
        "Room Coordination, 6-Char Code / QR Code Sharing, Foreground Background Tracking & Google Maps SDK")
        
    gf.draw_pill(d, [700, 110, 900, 155], "START", is_start=True)
    gf.draw_process(d, [610, 185, 990, 260], "1.0 Open Emergency Room Hub", [
        "User navigates to RoomListActivity",
        "Lists all active emergency groups the user belongs to"
    ])
    gf.draw_decision(d, 800, 335, 270, 80, "Room Action", ["Selection?"])
    
    # Create Room (Left)
    gf.draw_process(d, [180, 415, 540, 495], "2.1 Create New Room", [
        "User specifies Room Name",
        "System generates unique 6-char code (e.g. RT9482)",
        "Registers current user as Room Owner"
    ])
    
    # Join Room (Right)
    gf.draw_process(d, [1060, 415, 1420, 495], "2.2 Join Existing Room", [
        "Option A: Manually enter 6-character room code",
        "Option B: Scan room QR Code (ScanQrActivity)"
    ])
    gf.draw_decision(d, 1240, 570, 260, 80, "Verify Code", ["in Firebase RTDB?"])
    
    # RoomHub
    gf.draw_process(d, [620, 600, 980, 680], "3.0 Access RoomHubActivity", [
        "Displays member roster, live battery levels & online status",
        "Options: Send Bell ping, Open Room Chat, or View Live Map"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_decision(d, 800, 755, 270, 80, "Member Interaction", ["Option?"])
    
    # 3 Interaction Options
    gf.draw_subroutine(d, [80, 840, 440, 925], "4.1 Instant Bell / Ping Alert", [
        "Writes record to /rooms/{code}/bells/{toUid}",
        "Cloud Function onBellCreated triggers FCM vibration",
        "Target member receives high-priority haptic ping"
    ])
    
    gf.draw_process(d, [480, 840, 820, 925], "4.2 Group Chat (RoomChatActivity)", [
        "Secure end-to-end incident communication channel",
        "Exchange text messages and incident photos in real time",
        "Real-time database synchronization"
    ])
    
    gf.draw_process(d, [860, 840, 1240, 925], "4.3 Live Map (RoomMapActivity)", [
        "Launches interactive Google Maps SDK interface",
        "Requests ACCESS_FINE_LOCATION permissions"
    ], border_color=gf.C_PROC_BD)
    
    # Tracking Service
    gf.draw_subroutine(d, [840, 980, 1260, 1075], "5.0 LiveRoomTrackingService (Foreground)", [
        "Android Foreground Service (location | dataSync)",
        "FusedLocationProviderClient captures GPS updates",
        "Publishes Lat/Lng, speed, accuracy & battery to RTDB"
    ])
    
    gf.draw_process(d, [840, 1120, 1260, 1205], "6.0 Interactive Google Maps Interface", [
        "Renders custom avatar markers for all room members",
        "Tracks real-time movement and location history",
        "Click member to calculate distance & start navigation"
    ], border_color=(14, 165, 233), accent_color=(240, 249, 255))
    
    gf.draw_pill(d, [710, 1220, 890, 1260], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (800, 155), (800, 185))
    gf.draw_arrow(d, (800, 260), (800, 295))
    
    # Dec 2.0
    gf.draw_poly_arrow(d, [(665, 335), (360, 335), (360, 415)], label="Create Room", label_side="top")
    gf.draw_poly_arrow(d, [(935, 335), (1240, 335), (1240, 415)], label="Join Room", label_side="top")
    
    gf.draw_poly_arrow(d, [(360, 495), (360, 640), (620, 640)], label="Room Created", label_side="bottom")
    
    gf.draw_arrow(d, (1240, 495), (1240, 530))
    gf.draw_arrow(d, (1110, 570), (980, 640), label="Valid (Found)", label_side="top")
    gf.draw_poly_arrow(d, [(1370, 570), (1480, 570), (1480, 455), (1420, 455)], label="Invalid Code", label_side="right")
    
    gf.draw_arrow(d, (800, 680), (800, 715))
    
    # Dec 4.0
    gf.draw_poly_arrow(d, [(665, 755), (260, 755), (260, 840)], label="Bell Ping", label_side="top")
    gf.draw_poly_arrow(d, [(730, 785), (650, 785), (650, 840)], label="Room Chat", label_side="right")
    gf.draw_poly_arrow(d, [(870, 785), (1050, 785), (1050, 840)], label="Live Map", label_side="left")
    
    # Map leads to Service and Maps
    gf.draw_arrow(d, (1050, 925), (1050, 980))
    gf.draw_arrow(d, (1050, 1075), (1050, 1120))
    
    # Endings
    gf.draw_poly_arrow(d, [(260, 925), (260, 1240), (710, 1240)])
    gf.draw_poly_arrow(d, [(650, 925), (650, 1240), (710, 1240)])
    gf.draw_poly_arrow(d, [(840, 1160), (800, 1160), (800, 1220)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_4_emergency_room_map_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 5: WEBRTC VOICE & VIDEO EMERGENCY CALLS
# ==============================================================================
def draw_flowchart_5_en():
    W, H = 1650, 1380
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 5: WEBRTC VOICE & VIDEO EMERGENCY INTERCOM", 
        "Firebase RTDB Signaling Pipeline, Peer-to-Peer (P2P) Handshake & Bi-directional Media Stream Management")
        
    gf.draw_pill(d, [720, 110, 920, 155], "START", is_start=True)
    gf.draw_process(d, [620, 185, 1020, 260], "1.0 Caller Initiates Call", [
        "Selects mode: Voice Call (Audio) or Video Call (Camera)",
        "Generates unique Call ID (/calls/{callId}) with status 'ringing'"
    ])
    gf.draw_subroutine(d, [610, 295, 1030, 385], "2.0 Signaling Client & SDP Offer Creation", [
        "Starts CallService (Foreground: microphone | camera)",
        "WebRTCClient generates local SDP Offer",
        "Uploads SDP Offer to /calls/{callId}/offer"
    ])
    gf.draw_process(d, [610, 420, 1030, 505], "3.0 Receiver Detects Incoming Call", [
        "IncomingCallManager intercepts FCM push / RTDB event",
        "Launches IncomingCallActivity over Lockscreen",
        "Plays full-screen emergency ringtone & vibration"
    ], border_color=(217, 119, 6), accent_color=(255, 251, 235))
    
    gf.draw_decision(d, 820, 595, 290, 85, "Receiver Decision?", ["Action Selected"])
    
    # 3 Decisions: Decline, Timeout, Accept
    gf.draw_process(d, [150, 560, 480, 630], "4.1 Call Declined", [
        "Receiver taps 'Decline'",
        "Updates status to 'declined' in RTDB"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_process(d, [1170, 560, 1500, 630], "4.2 Call Timeout (30s)", [
        "No response after 30 seconds of ringing",
        "Updates status to 'timeout' in RTDB"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    
    gf.draw_process(d, [630, 700, 1010, 775], "4.3 Call Accepted", [
        "Receiver taps 'Accept'",
        "Updates status to 'connected' in RTDB"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    
    # Handshake
    gf.draw_subroutine(d, [590, 815, 1050, 915], "5.0 WebRTC Signaling Handshake (SDP Answer & ICE)", [
        "Receiver generates SDP Answer & writes to /calls/{callId}/answer",
        "Both endpoints exchange ICE Candidates via /calls/{callId}/iceCandidates",
        "Establishes direct Peer-to-Peer (P2P Mesh) connection"
    ], border_color=(99, 102, 241), bg_color=(238, 242, 255))
    
    gf.draw_process(d, [610, 950, 1030, 1050], "6.0 Active Low-Latency Media Session", [
        "Real-time bi-directional Audio & Video streaming",
        "User Controls: Mute Microphone, Speakerphone Toggle,",
        "and Front / Rear Camera Switch (video mode)"
    ], border_color=gf.C_PROC_BD)
    
    gf.draw_decision(d, 820, 1140, 290, 75, "Call Terminated?", ["(Either Party)"])
    
    gf.draw_process(d, [620, 1225, 1020, 1300], "7.0 Teardown & Resource Disposal", [
        "Updates status to 'ended' in Firebase RTDB",
        "Stops CallService & releases camera / microphone drivers"
    ])
    gf.draw_pill(d, [730, 1335, 910, 1370], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 295))
    gf.draw_arrow(d, (820, 385), (820, 420))
    gf.draw_arrow(d, (820, 505), (820, 552))
    
    # Dec 4.0
    gf.draw_arrow(d, (675, 595), (480, 595), label="Decline", label_side="top")
    gf.draw_arrow(d, (965, 595), (1170, 595), label="Timeout", label_side="top")
    gf.draw_arrow(d, (820, 637), (820, 700), label="Accept", label_side="right")
    
    gf.draw_arrow(d, (820, 775), (820, 815))
    gf.draw_arrow(d, (820, 915), (820, 950))
    gf.draw_arrow(d, (820, 1050), (820, 1102))
    
    gf.draw_arrow(d, (820, 1177), (820, 1225), label="Yes (End)", label_side="right")
    gf.draw_arrow(d, (820, 1300), (820, 1335))
    
    # Decline / Timeout to cleanup
    gf.draw_poly_arrow(d, [(315, 630), (315, 1262), (620, 1262)])
    gf.draw_poly_arrow(d, [(1335, 630), (1335, 1262), (1020, 1262)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_5_webrtc_call_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 6: INCIDENT REPORTING & NEARBY HOSPITALS
# ==============================================================================
def draw_flowchart_6_en():
    W, H = 1650, 1250
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 6: COMMUNITY INCIDENT REPORTING & NEARBY HOSPITAL FINDER", 
        "Incident Submission with Photo Upload to Firebase Storage & Haversine Distance Geospatial Navigation")
        
    # Divider line
    d.line([(825, 110), (825, 1200)], fill=(226, 232, 240), width=2)
    
    # ================= LEFT: INCIDENT REPORTING =================
    gf.draw_pill(d, [260, 110, 460, 155], "START (REPORT)", is_start=True)
    gf.draw_process(d, [160, 185, 560, 260], "L1.0 Open ReportActivity", [
        "User accesses community incident form",
        "Camera & Location permissions validated"
    ])
    gf.draw_io_box(d, [160, 290, 560, 365], "L2.0 Select Incident Category", [
        "Road Accident / Violent Crime / Medical Emergency",
        "Fire Hazard / Natural Disaster / Other"
    ])
    gf.draw_io_box(d, [160, 395, 560, 470], "L3.0 Incident Details & Location", [
        "User enters detailed description of incident",
        "Current GPS coordinates captured automatically"
    ])
    gf.draw_decision(d, 360, 550, 270, 75, "Is Photo Attachment", ["Included?"])
    
    # Upload Photo
    gf.draw_subroutine(d, [140, 630, 580, 715], "L4.1 Upload to Firebase Storage", [
        "Compress image & upload to /incident_reports/{id}.jpg",
        "Retrieve secure public download URL"
    ])
    
    gf.draw_subroutine(d, [140, 755, 580, 840], "L5.0 Save Record to Firebase RTDB", [
        "Write complete JSON payload to /reports/{reportId}",
        "Include user UID, timestamp, category, GPS & photo URL"
    ])
    gf.draw_process(d, [160, 880, 560, 955], "L6.0 Confirmation & Ticket Generation", [
        "Display submission receipt & ticket ID to user",
        "Notify emergency dispatch admin console"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    gf.draw_pill(d, [270, 1010, 450, 1050], "END", is_start=False)
    
    # Left Arrows
    gf.draw_arrow(d, (360, 155), (360, 185))
    gf.draw_arrow(d, (360, 260), (360, 290))
    gf.draw_arrow(d, (360, 365), (360, 395))
    gf.draw_arrow(d, (360, 470), (360, 512))
    
    gf.draw_arrow(d, (360, 587), (360, 630), label="Yes (With Photo)", label_side="right")
    gf.draw_poly_arrow(d, [(495, 550), (620, 550), (620, 795), (580, 795)], label="No Photo", label_side="top")
    
    gf.draw_arrow(d, (360, 715), (360, 755))
    gf.draw_arrow(d, (360, 840), (360, 880))
    gf.draw_arrow(d, (360, 955), (360, 1010))
    
    # ================= RIGHT: NEARBY HOSPITALS =================
    gf.draw_pill(d, [1180, 110, 1380, 155], "START (HOSPITAL)", is_start=True)
    gf.draw_process(d, [1080, 185, 1480, 260], "H1.0 Open NearbyHospitalActivity", [
        "User chooses 'Nearby Hospitals & Clinics' from menu",
        "Validates GPS hardware status & permissions"
    ])
    gf.draw_subroutine(d, [1080, 295, 1480, 375], "H2.0 Acquire User GPS Coordinates", [
        "fusedLocationClient.getCurrentLocation()",
        "Retrieves accurate user Latitude & Longitude"
    ])
    gf.draw_subroutine(d, [1080, 410, 1480, 495], "H3.0 Query Medical Geospatial API", [
        "Submits geospatial query to medical facilities API",
        "Fetches hospitals, trauma centers & clinics within radius"
    ])
    gf.draw_process(d, [1080, 530, 1480, 615], "H4.0 Calculate Distance & Sort", [
        "Computes direct distance using Haversine formula",
        "Sorts facilities ascending from nearest to farthest"
    ])
    gf.draw_io_box(d, [1080, 650, 1480, 735], "H5.0 Render Hospital Directory", [
        "Displays facility name, address, distance (km),",
        "operational status, and emergency telephone number"
    ])
    gf.draw_decision(d, 1280, 810, 270, 75, "User Action", ["Selection?"])
    
    gf.draw_process(d, [960, 895, 1240, 975], "H6.1 Emergency Phone Call", [
        "Launches Android Dialer with",
        "hospital emergency hotline"
    ], border_color=(217, 119, 6))
    
    gf.draw_process(d, [1310, 895, 1590, 975], "H6.2 Google Maps Navigation", [
        "Launches Google Maps Intent with",
        "turn-by-turn driving route"
    ], border_color=gf.C_PROC_BD)
    
    gf.draw_pill(d, [1190, 1020, 1370, 1060], "END", is_start=False)
    
    # Right Arrows
    gf.draw_arrow(d, (1280, 155), (1280, 185))
    gf.draw_arrow(d, (1280, 260), (1280, 295))
    gf.draw_arrow(d, (1280, 375), (1280, 410))
    gf.draw_arrow(d, (1280, 495), (1280, 530))
    gf.draw_arrow(d, (1280, 615), (1280, 650))
    gf.draw_arrow(d, (1280, 735), (1280, 772))
    
    gf.draw_poly_arrow(d, [(1145, 810), (1100, 810), (1100, 895)], label="Phone Call", label_side="left")
    gf.draw_poly_arrow(d, [(1415, 810), (1450, 810), (1450, 895)], label="Navigation", label_side="right")
    
    gf.draw_poly_arrow(d, [(1100, 975), (1100, 1040), (1190, 1040)])
    gf.draw_poly_arrow(d, [(1450, 975), (1450, 1040), (1370, 1040)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_6_report_hospital_en.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# FLOWCHART 7: ADMIN PORTAL & CLOUD BROADCASTING
# ==============================================================================
def draw_flowchart_7_en():
    W, H = 1650, 1260
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "FLOWCHART 7: WEB ADMIN PORTAL & CLOUD BROADCAST SYSTEM", 
        "Admin Authentication, Real-Time SOS Incident Control, Targeted FCM Broadcasts & Database Maintenance")
        
    gf.draw_pill(d, [720, 110, 920, 155], "START", is_start=True)
    gf.draw_process(d, [610, 185, 1030, 260], "1.0 Access Web Admin Portal", [
        "Admin navigates to web portal (server.js /admin)",
        "Authentication handled via Firebase Auth"
    ])
    gf.draw_decision(d, 820, 335, 310, 85, "Does User Have", ["Admin Privileges?"])
    
    # Access Denied
    gf.draw_process(d, [150, 295, 480, 375], "2.0 Access Denied (403)", [
        "Email domain is not @resqtap.com and no /admins/{uid} entry",
        "System rejects access to administration console"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    gf.draw_pill(d, [260, 420, 370, 460], "END", is_start=False)
    
    # Dashboard
    gf.draw_process(d, [610, 450, 1030, 535], "3.0 Admin Dashboard (admin.html)", [
        "Live system statistics: Total Users, Active Rooms & Active Alerts",
        "Operator selects management action"
    ], border_color=gf.C_PROC_BD, accent_color=gf.C_PROC_ACCENT)
    
    gf.draw_decision(d, 820, 625, 300, 80, "Admin Operation", ["Selection?"])
    
    # 3 Operations
    gf.draw_process(d, [80, 720, 480, 825], "4.1 Live SOS Incident Control", [
        "Listens to incoming alerts on /rooms/*/sosAlerts",
        "Click 'Reserve Case' (served = true, servedBy)",
        "Update progress timeline (Dispatched -> Resolved)",
        "Direct emergency support chat with victim"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_process(d, [580, 720, 1060, 825], "4.2 Targeted Broadcast System", [
        "Select target audience: All / Live Room Members / Admins",
        "Input alert title and notification message",
        "Invoke Callable Cloud Function: sendAdminNotification"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_subroutine(d, [580, 870, 1060, 965], "4.2b Cloud Function FCM Blaster", [
        "Collects target user device FCM tokens",
        "Simultaneously dispatches High-Priority notifications",
        "Logs delivery audit to /admin_notifications/{id}"
    ])
    
    gf.draw_process(d, [1140, 720, 1560, 825], "4.3 Maintenance & User Purge", [
        "Trigger user purge via /admin_user_deletions/{uid}",
        "Cloud Function purges user from Auth & RTDB completely",
        "Trigger DB account reset via /api/admin/clear-database"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    
    gf.draw_process(d, [630, 1040, 1010, 1115], "5.0 Database Synchronized", [
        "Operation completed and synced to Firebase",
        "Dashboard statistics refreshed"
    ])
    gf.draw_pill(d, [730, 1160, 910, 1200], "END", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 292))
    
    # Dec 2.0 (Admin Auth)
    gf.draw_arrow(d, (665, 335), (480, 335), label="No (Non-Admin)", label_side="top")
    gf.draw_arrow(d, (315, 375), (315, 420))
    gf.draw_arrow(d, (820, 377), (820, 450), label="Yes (Admin Granted)", label_side="right")
    
    gf.draw_arrow(d, (820, 535), (820, 585))
    
    # Dec 3.0
    gf.draw_poly_arrow(d, [(670, 625), (280, 625), (280, 720)], label="Manage SOS", label_side="top")
    gf.draw_arrow(d, (820, 665), (820, 720), label="Broadcast", label_side="right")
    gf.draw_poly_arrow(d, [(970, 625), (1350, 625), (1350, 720)], label="Maintenance", label_side="top")
    
    # Broadcast
    gf.draw_arrow(d, (820, 825), (820, 870))
    gf.draw_arrow(d, (820, 965), (820, 1040))
    
    # From 4.1 & 4.3 to 5.0
    gf.draw_poly_arrow(d, [(280, 825), (280, 1077), (630, 1077)])
    gf.draw_poly_arrow(d, [(1350, 825), (1350, 1077), (1010, 1077)])
    
    gf.draw_arrow(d, (820, 1115), (820, 1160))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_7_admin_portal_cloud_en.png")
    img.save(path)
    print("Saved:", path)

if __name__ == "__main__":
    print("Generating all 7 English flowchart images...")
    draw_flowchart_1_en()
    draw_flowchart_2_en()
    draw_flowchart_3_en()
    draw_flowchart_4_en()
    draw_flowchart_5_en()
    draw_flowchart_6_en()
    draw_flowchart_7_en()
    print("All 7 English flowcharts successfully generated in docs_flowcharts_en!")
