import os
import win32com.client
from word_flowchart_builder import WordFlowchartBuilder

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))

def draw_fc1(b):
    b.add_header_banner(1680, 
        "FLOWCHART 1: OVERALL SYSTEM WORKFLOW & ARCHITECTURE", 
        "End-to-End System Navigation, Security Verification Gateways & Core Subsystems")
    
    b.add_terminal([740, 115, 940, 160], "START", is_start=True)
    b.add_process([670, 190, 1010, 270], "1.0 App Launch & Splash Screen", [
        "User launches ResQTap application",
        "SplashActivity checks permissions & Firebase config"
    ])
    b.add_decision(840, 345, 290, 85, "Is User Session", ["Active & Authenticated?"])
    
    b.add_io_box([160, 305, 510, 385], "2.0 Onboarding & Authentication", [
        "Login, Register, or Forgot Password flows",
        "Firebase Auth credential validation"
    ])
    
    b.add_decision(840, 480, 300, 85, "Is App Lock or", ["Biometrics Enabled?"])
    
    b.add_process([1180, 440, 1540, 520], "3.0 App Lock Gateway", [
        "Enter 4-Digit Security PIN or Fingerprint Scan",
        "Duress PIN: Covertly triggers Silent SOS"
    ])
    
    b.add_process([660, 585, 1020, 665], "4.0 Main Dashboard (MainActivity)", [
        "Displays Medical Profile, Battery Status & Alerts",
        "User selects emergency or community service"
    ])
    
    b.add_decision(840, 740, 280, 80, "User Action", ["Selection?"])
    
    # 5 Functional Modules
    b.add_process([70, 835, 340, 935], "ONETAP SOS", [
        "Instant SOS Trigger",
        "3-sec countdown & alarm",
        "Broadcasts to RTDB & FCM"
    ])
    b.add_process([375, 835, 655, 935], "EMERGENCY ROOMS", [
        "Create / Join Room (Code / QR)",
        "LiveRoomTrackingService (GPS)",
        "Google Maps & Room Chat"
    ])
    b.add_process([690, 835, 990, 935], "WEBRTC CALLS", [
        "Voice & Video Intercom",
        "WebRTC Signaling via RTDB",
        "Low-latency P2P Media Stream"
    ])
    b.add_process([1025, 835, 1315, 935], "NEARBY HOSPITALS", [
        "GPS location scan",
        "Geospatial medical facility query",
        "Emergency dial & navigation"
    ])
    b.add_process([1350, 835, 1610, 935], "REPORTS & NEWS", [
        "Submit incident report + photo",
        "Upload to Cloud Storage",
        "Verified medical news feeds"
    ])
    
    b.add_process([680, 1010, 1000, 1085], "5.0 Session Lifecycle & Logout", [
        "User completes activity or signs out",
        "Background services & session tokens synchronized"
    ])
    b.add_terminal([750, 1130, 930, 1175], "END", is_start=False)
    
    # Connectors
    b.add_arrow((840, 160), (840, 190))
    b.add_arrow((840, 270), (840, 302))
    
    b.add_arrow((695, 345), (510, 345), label="No", label_side="top")
    b.add_poly_arrow([(335, 305), (335, 230), (670, 230)], label="Auth Success", label_idx=1, label_side="top")
    b.add_arrow((840, 387), (840, 437), label="Yes", label_side="right")
    
    b.add_arrow((990, 480), (1180, 480), label="Yes", label_side="top")
    b.add_poly_arrow([(1360, 520), (1360, 625), (1020, 625)], label="Unlocked", label_idx=1, label_side="top")
    b.add_arrow((840, 522), (840, 585), label="No", label_side="right")
    
    b.add_arrow((840, 665), (840, 700))
    
    b.add_poly_arrow([(700, 740), (205, 740), (205, 835)])
    b.add_poly_arrow([(788, 765), (515, 765), (515, 835)])
    b.add_arrow((840, 780), (840, 835))
    b.add_poly_arrow([(892, 765), (1170, 765), (1170, 835)])
    b.add_poly_arrow([(980, 740), (1480, 740), (1480, 835)])
    
    # 5 Paths
    b.add_poly_arrow([(205, 935), (205, 1060), (670, 1060)])
    b.add_poly_arrow([(515, 935), (515, 1030), (670, 1030)])
    b.add_arrow((840, 935), (840, 1010))
    b.add_poly_arrow([(1170, 935), (1170, 1030), (1010, 1030)])
    b.add_poly_arrow([(1480, 935), (1480, 1060), (1010, 1060)])
    
    b.add_arrow((840, 1085), (840, 1130))


def draw_fc2(b):
    b.add_header_banner(1750, 
        "FLOWCHART 2: AUTHENTICATION, APP LOCK & DURESS PIN SAFEGUARD", 
        "Session Verification, 4-Digit Passkey / Biometric Authentication & Covert Silent SOS Protocol")
        
    b.add_terminal([775, 115, 975, 160], "START", is_start=True)
    b.add_process([705, 190, 1045, 270], "1.0 Launch SplashActivity", [
        "Initialize Firebase App & Check Auth State",
        "FirebaseAuth.getInstance().getCurrentUser()"
    ])
    b.add_decision(875, 345, 290, 80, "Is User Session", ["Valid & Active?"])
    
    b.add_io_box([180, 305, 540, 385], "2.0 GetStarted & Auth Options", [
        "Login, Register New Account, or Reset Password",
        "Email & Password verification via Firebase"
    ])
    b.add_process([180, 440, 540, 520], "2.1 Save User Profile Data", [
        "Save Demographics & Medical Card to RTDB",
        "Store FCM Device Token under /users/{uid}"
    ])
    
    b.add_decision(875, 480, 300, 80, "Is App Lock or", ["Fingerprint Enabled?"])
    
    b.add_process([705, 585, 1045, 665], "3.0 Display AppLockActivity", [
        "Presents secure 4-digit PIN keypad screen",
        "Provides BiometricPrompt fingerprint option"
    ])
    
    b.add_decision(875, 750, 290, 90, "Credential Evaluation", ["Input Match Type?"])
    
    b.add_process([735, 875, 1015, 955], "4.1 Access Granted (Normal)", [
        "AppLockManager.setUnlocked(true)",
        "App session unlocked for user"
    ])
    
    b.add_process([1180, 700, 1640, 830], "4.2 DURESS PIN PROTOCOL (SILENT SOS)", [
        "Matches UserPrefs.getDuressPin()",
        "Immediately writes Silent SOS Alert to RTDB",
        "NO sirens, NO strobe lights, NO on-screen alerts",
        "Unlocks standard app camouflage to protect user"
    ])
    
    b.add_process([150, 705, 530, 785], "4.3 Invalid PIN / Failed", [
        "Increment failed attempts counter",
        "Display haptic error feedback"
    ])
    
    b.add_decision(340, 850, 260, 80, "Failed Attempts", ["> 5 Consecutive?"])
    
    b.add_process([150, 945, 530, 1040], "4.4 Temporary Cooldown Lockout", [
        "Locks app keypad for 30 seconds",
        "Mitigates brute-force passkey guessing"
    ])
    
    b.add_process([705, 1060, 1045, 1140], "5.0 Navigate to MainActivity", [
        "Loads ResQTap Main Dashboard",
        "Emergency services ready in foreground"
    ])
    b.add_terminal([785, 1195, 965, 1240], "END", is_start=False)
    
    # Arrows
    b.add_arrow((875, 160), (875, 190))
    b.add_arrow((875, 270), (875, 305))
    
    b.add_arrow((730, 345), (540, 345), label="No", label_side="top")
    b.add_arrow((360, 385), (360, 440))
    b.add_arrow((540, 480), (725, 480), label="Auth Success", label_side="top")
    b.add_arrow((875, 385), (875, 440), label="Yes", label_side="right")
    
    b.add_arrow((875, 520), (875, 585), label="Yes", label_side="right")
    b.add_poly_arrow([(1025, 480), (1690, 480), (1690, 1115), (1045, 1115)], label="Disabled", label_side="top")
    
    b.add_arrow((875, 665), (875, 705))
    
    # Branches
    b.add_arrow((875, 795), (875, 875), label="Valid PIN / Bio", label_side="right")
    b.add_arrow((875, 955), (875, 1060))
    
    b.add_arrow((1020, 750), (1180, 750), label="Duress Match", label_side="top")
    b.add_poly_arrow([(1350, 830), (1350, 1075), (1045, 1075)], label="Camouflage App", label_side="right")
    
    b.add_arrow((730, 750), (530, 750), label="Wrong PIN", label_side="top")
    b.add_arrow((340, 785), (340, 810))
    b.add_arrow((340, 890), (340, 945), label="Yes (>= 5)", label_side="right")
    
    b.add_poly_arrow([(210, 850), (110, 850), (110, 645), (705, 645)], label="No (Tries left)", label_side="top")
    b.add_poly_arrow([(150, 992), (50, 992), (50, 615), (705, 615)], label="Cooldown Expired", label_idx=1, label_side="right")
    
    b.add_arrow((875, 1140), (875, 1195))


def draw_fc3(b):
    b.add_header_banner(1700, 
        "FLOWCHART 3: ONETAP SOS EMERGENCY TRIGGER & INCIDENT DISPATCH", 
        "Rapid Response Workflow: 3-Second Countdown, Hardware Alarms, Firebase RTDB/FCM Blasting & Live Dispatch Timeline")
        
    b.add_terminal([750, 115, 950, 160], "START", is_start=True)
    b.add_process([660, 190, 1040, 270], "1.0 Tap OneTap SOS Button", [
        "User taps central SOS button on MainActivity",
        "Triggers modal SosBottomSheetController"
    ])
    b.add_decision(850, 345, 320, 85, "3-Second Countdown", ["(CountDownTimer Active)"])
    
    b.add_process([1240, 310, 1580, 385], "2.0 Slide to Cancel", [
        "User slides cancellation bar (Progress >= 95%)",
        "Timer stopped & no alert dispatched to cloud"
    ])
    b.add_terminal([1360, 425, 1470, 465], "END", is_start=False)
    
    b.add_process([650, 450, 1050, 550], "3.0 Local Hardware Alarms Activated", [
        "Blasts Maximum Decibel Siren (SosAudioManager)",
        "Pulses Camera Flashlight Strobe continuously",
        "Triggers Rhythmic Haptic Vibration (VibrateManager)"
    ])
    
    b.add_io_box([650, 585, 1050, 670], "4.0 Fetch GPS Location & User Rooms", [
        "fusedLocationClient.getLastLocation() -> Lat, Lng",
        "Query user's active rooms from /userRooms/{uid}"
    ])
    
    b.add_decision(850, 745, 290, 80, "Does User Have", ["Active Rooms?"])
    
    b.add_subroutine([370, 835, 770, 920], "5.1 Group Room Blasting", [
        "Publish alert to each /rooms/{code}/sosAlerts",
        "Include GPS coordinates, battery level & timestamp"
    ])
    b.add_subroutine([930, 835, 1330, 920], "5.2 DIRECT Fallback Channel", [
        "Publish to fallback /rooms/DIRECT/sosAlerts",
        "Register alert in global /sos_alerts queue"
    ])
    
    b.add_subroutine([620, 960, 1080, 1060], "6.0 Cloud Functions Automated Dispatch", [
        "Triggers onRoomSosCreated & onSosAlertCreated",
        "Collects FCM device tokens of all members & admins",
        "Blasts High-Priority FCM Push Notifications"
    ])
    
    b.add_process([120, 1105, 580, 1205], "7.1 Room Members / Responders", [
        "Receive FCM -> Launch SosAlarmActivity over Lockscreen",
        "Full-screen siren, strobe & vibration alert",
        "Options: Snooze Alarm, View Live Map, Call Victim"
    ])
    
    b.add_process([1120, 1105, 1580, 1205], "7.2 Web Admin Dashboard", [
        "Flashing emergency alert banner on console",
        "Admin clicks 'Reserve Case' (served = true, servedBy)",
        "Updates Timeline: Dispatched -> En Route -> On Scene"
    ])
    
    b.add_process([620, 1245, 1080, 1345], "8.0 Live Response Timeline (SosProgressActivity)", [
        "watchSosCaseReservation() detects admin reservation",
        "Auto-launches 4-step interactive progress screen:",
        "1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved"
    ])
    
    b.add_decision(850, 1415, 290, 75, "Incident Resolved /", ["Cancelled?"])
    b.add_terminal([760, 1485, 940, 1525], "END", is_start=False)
    
    # Arrows
    b.add_arrow((850, 160), (850, 190))
    b.add_arrow((850, 270), (850, 302))
    
    b.add_arrow((1010, 345), (1240, 345), label="Slide Cancel", label_side="top")
    b.add_arrow((1410, 385), (1410, 425))
    b.add_arrow((850, 387), (850, 450), label="Countdown Expired (0s)", label_side="right")
    
    b.add_arrow((850, 550), (850, 585))
    b.add_arrow((850, 670), (850, 705))
    
    b.add_poly_arrow([(705, 745), (570, 745), (570, 835)], label="Yes (Has Rooms)", label_side="top")
    b.add_poly_arrow([(995, 745), (1130, 745), (1130, 835)], label="No (No Rooms)", label_side="top")
    
    b.add_poly_arrow([(570, 920), (570, 940), (760, 940), (760, 960)])
    b.add_poly_arrow([(1130, 920), (1130, 940), (940, 940), (940, 960)])
    
    b.add_poly_arrow([(620, 1010), (350, 1010), (350, 1105)], label="FCM to Members", label_side="top")
    b.add_poly_arrow([(1080, 1010), (1350, 1010), (1350, 1105)], label="RTDB Sync to Admin", label_side="top")
    
    b.add_poly_arrow([(350, 1205), (350, 1295), (620, 1295)])
    b.add_poly_arrow([(1350, 1205), (1350, 1295), (1080, 1295)], label="Admin Reserved", label_idx=1, label_side="top")
    
    b.add_arrow((850, 1345), (850, 1377))
    b.add_arrow((850, 1452), (850, 1485), label="Yes (Resolved)", label_side="right")


def draw_fc4(b):
    b.add_header_banner(1680, 
        "FLOWCHART 4: EMERGENCY ROOM HUB & LIVE GPS TRACKING SERVICE", 
        "Room Coordination, 6-Char Code / QR Code Sharing, Foreground Background Tracking & Google Maps SDK")
        
    b.add_terminal([740, 115, 940, 160], "START", is_start=True)
    b.add_process([650, 190, 1030, 270], "1.0 Open Emergency Room Hub", [
        "User navigates to RoomListActivity",
        "Lists all active emergency groups the user belongs to"
    ])
    b.add_decision(840, 345, 270, 80, "Room Action", ["Selection?"])
    
    b.add_process([180, 425, 560, 525], "2.1 Create New Room", [
        "User specifies Room Name",
        "System generates unique 6-char code (e.g. RT9482)",
        "Registers current user as Room Owner"
    ])
    
    b.add_process([1110, 425, 1500, 520], "2.2 Join Existing Room", [
        "Option A: Manually enter 6-character room code",
        "Option B: Scan room QR Code (ScanQrActivity)"
    ])
    b.add_decision(1305, 585, 260, 80, "Verify Code", ["in Firebase RTDB?"])
    
    b.add_process([650, 615, 1030, 700], "3.0 Access RoomHubActivity", [
        "Displays member roster, live battery levels & online status",
        "Options: Send Bell ping, Open Room Chat, or View Live Map"
    ])
    
    b.add_decision(840, 770, 270, 80, "Member Interaction", ["Option?"])
    
    b.add_subroutine([180, 855, 560, 960], "4.1 Instant Bell / Ping Alert", [
        "Writes record to /rooms/{code}/bells/{toUid}",
        "Cloud Function onBellCreated triggers FCM vibration",
        "Target member receives high-priority haptic ping"
    ])
    
    b.add_process([650, 855, 1030, 960], "4.2 Group Chat (RoomChatActivity)", [
        "Secure end-to-end incident communication channel",
        "Exchange text messages and incident photos in real time",
        "Real-time database synchronization"
    ])
    
    b.add_process([1110, 855, 1500, 960], "4.3 Live Map (RoomMapActivity)", [
        "Launches interactive Google Maps SDK interface",
        "Requests ACCESS_FINE_LOCATION permissions"
    ])
    
    b.add_subroutine([1110, 1005, 1500, 1105], "5.0 LiveRoomTrackingService (Foreground)", [
        "Android Foreground Service (location | dataSync)",
        "FusedLocationProviderClient captures GPS updates",
        "Publishes Lat/Lng, speed, accuracy & battery to RTDB"
    ])
    
    b.add_process([1110, 1145, 1500, 1245], "6.0 Interactive Google Maps Interface", [
        "Renders custom avatar markers for all room members",
        "Tracks real-time movement and location history",
        "Click member to calculate distance & start navigation"
    ])
    
    b.add_terminal([750, 1325, 930, 1370], "END", is_start=False)
    
    # Arrows
    b.add_arrow((840, 160), (840, 190))
    b.add_arrow((840, 270), (840, 305))
    
    b.add_poly_arrow([(705, 345), (370, 345), (370, 425)], label="Create Room", label_side="top")
    b.add_poly_arrow([(975, 345), (1305, 345), (1305, 425)], label="Join Room", label_side="top")
    
    b.add_poly_arrow([(370, 525), (370, 655), (650, 655)], label="Room Created", label_idx=1, label_side="top")
    
    b.add_arrow((1305, 520), (1305, 545))
    b.add_poly_arrow([(1305, 625), (1305, 655), (1030, 655)], label="Valid (Found)", label_idx=1, label_side="top")
    b.add_poly_arrow([(1435, 585), (1550, 585), (1550, 472), (1500, 472)], label="Invalid Code", label_idx=1, label_side="right")
    
    b.add_arrow((840, 700), (840, 730))
    
    # 3-column balanced branches (100% straight Room Chat!)
    b.add_poly_arrow([(705, 770), (370, 770), (370, 855)], label="Bell Ping", label_side="top")
    b.add_arrow((840, 810), (840, 855), label="Room Chat", label_side="right")
    b.add_poly_arrow([(975, 770), (1305, 770), (1305, 855)], label="Live Map", label_side="top")
    
    b.add_arrow((1305, 960), (1305, 1005))
    b.add_arrow((1305, 1105), (1305, 1145))
    
    # Symmetrical termination
    b.add_poly_arrow([(370, 960), (370, 1347), (750, 1347)])
    b.add_arrow((840, 960), (840, 1325))
    b.add_poly_arrow([(1305, 1245), (1305, 1347), (930, 1347)])


def draw_fc5(b):
    b.add_header_banner(1700, 
        "FLOWCHART 5: WEBRTC VOICE & VIDEO EMERGENCY INTERCOM", 
        "Firebase RTDB Signaling Pipeline, Peer-to-Peer (P2P) Handshake & Bi-directional Media Stream Management")
        
    b.add_terminal([750, 115, 950, 160], "START", is_start=True)
    b.add_process([650, 190, 1050, 270], "1.0 Caller Initiates Call", [
        "Selects mode: Voice Call (Audio) or Video Call (Camera)",
        "Generates unique Call ID (/calls/{callId}) with status 'ringing'"
    ])
    b.add_subroutine([640, 305, 1060, 410], "2.0 Signaling Client & SDP Offer Creation", [
        "Starts CallService (Foreground: microphone | camera)",
        "WebRTCClient generates local SDP Offer",
        "Uploads SDP Offer to /calls/{callId}/offer"
    ])
    b.add_process([640, 440, 1060, 545], "3.0 Receiver Detects Incoming Call", [
        "IncomingCallManager intercepts FCM push / RTDB event",
        "Launches IncomingCallActivity over Lockscreen",
        "Plays full-screen emergency ringtone & vibration"
    ])
    
    b.add_decision(850, 620, 290, 85, "Receiver Decision?", ["Action Selected"])
    
    b.add_process([150, 580, 500, 655], "4.1 Call Declined", [
        "Receiver taps 'Decline'",
        "Updates status to 'declined' in RTDB"
    ])
    
    b.add_process([1200, 580, 1550, 655], "4.2 Call Timeout (30s)", [
        "No response after 30 seconds of ringing",
        "Updates status to 'timeout' in RTDB"
    ])
    
    b.add_process([660, 715, 1040, 795], "4.3 Call Accepted", [
        "Receiver taps 'Accept'",
        "Updates status to 'connected' in RTDB"
    ])
    
    b.add_subroutine([620, 835, 1080, 940], "5.0 WebRTC Signaling Handshake (SDP Answer & ICE)", [
        "Receiver generates SDP Answer & writes to /calls/{callId}/answer",
        "Both endpoints exchange ICE Candidates via /calls/{callId}/iceCandidates",
        "Establishes direct Peer-to-Peer (P2P Mesh) connection"
    ])
    
    b.add_process([640, 975, 1060, 1080], "6.0 Active Low-Latency Media Session", [
        "Real-time bi-directional Audio & Video streaming",
        "User Controls: Mute Microphone, Speakerphone Toggle,",
        "and Front / Rear Camera Switch (video mode)"
    ])
    
    b.add_decision(850, 1165, 290, 75, "Call Terminated?", ["(Either Party)"])
    
    b.add_process([650, 1250, 1050, 1325], "7.0 Teardown & Resource Disposal", [
        "Updates status to 'ended' in Firebase RTDB",
        "Stops CallService & releases camera / microphone drivers"
    ])
    b.add_terminal([760, 1355, 940, 1390], "END", is_start=False)
    
    # Arrows
    b.add_arrow((850, 160), (850, 190))
    b.add_arrow((850, 270), (850, 305))
    b.add_arrow((850, 410), (850, 440))
    b.add_arrow((850, 545), (850, 577))
    
    b.add_arrow((705, 620), (500, 620), label="Decline", label_side="top")
    b.add_arrow((995, 620), (1200, 620), label="Timeout", label_side="top")
    b.add_arrow((850, 663), (850, 715), label="Accept", label_side="right")
    
    b.add_arrow((850, 795), (850, 835))
    b.add_arrow((850, 940), (850, 975))
    b.add_arrow((850, 1080), (850, 1127))
    
    b.add_arrow((850, 1202), (850, 1250), label="Yes (End)", label_side="right")
    b.add_arrow((850, 1325), (850, 1355))
    
    b.add_poly_arrow([(325, 655), (325, 1287), (650, 1287)])
    b.add_poly_arrow([(1375, 655), (1375, 1287), (1050, 1287)])


def draw_fc6(b):
    b.add_header_banner(1700, 
        "FLOWCHART 6: COMMUNITY INCIDENT REPORTING & NEARBY HOSPITAL FINDER", 
        "Incident Submission with Photo Upload to Firebase Storage & Haversine Distance Geospatial Navigation")
        
    b.add_line((850, 115), (850, 1230), color=0xB4B4B4, weight=1.5)
    
    # Left: Incident Report
    b.add_terminal([270, 115, 470, 160], "START (REPORT)", is_start=True)
    b.add_process([160, 190, 580, 270], "L1.0 Open ReportActivity", [
        "User accesses community incident form",
        "Camera & Location permissions validated"
    ])
    b.add_io_box([160, 300, 580, 380], "L2.0 Select Incident Category", [
        "Road Accident / Violent Crime / Medical Emergency",
        "Fire Hazard / Natural Disaster / Other"
    ])
    b.add_io_box([160, 410, 580, 490], "L3.0 Incident Details & Location", [
        "User enters detailed description of incident",
        "Current GPS coordinates captured automatically"
    ])
    b.add_decision(370, 570, 270, 75, "Is Photo Attachment", ["Included?"])
    
    b.add_subroutine([140, 650, 600, 735], "L4.1 Upload to Firebase Storage", [
        "Compress image & upload to /incident_reports/{id}.jpg",
        "Retrieve secure public download URL"
    ])
    
    b.add_subroutine([140, 775, 600, 860], "L5.0 Save Record to Firebase RTDB", [
        "Write complete JSON payload to /reports/{reportId}",
        "Include user UID, timestamp, category, GPS & photo URL"
    ])
    b.add_process([160, 900, 580, 975], "L6.0 Confirmation & Ticket Generation", [
        "Display submission receipt & ticket ID to user",
        "Notify emergency dispatch admin console"
    ])
    b.add_terminal([280, 1030, 460, 1070], "END", is_start=False)
    
    b.add_arrow((370, 160), (370, 190))
    b.add_arrow((370, 270), (370, 300))
    b.add_arrow((370, 380), (370, 410))
    b.add_arrow((370, 490), (370, 532))
    
    b.add_arrow((370, 607), (370, 650), label="Yes (With Photo)", label_side="right")
    b.add_poly_arrow([(505, 570), (640, 570), (640, 815), (600, 815)], label="No Photo", label_side="top")
    
    b.add_arrow((370, 735), (370, 775))
    b.add_arrow((370, 860), (370, 900))
    b.add_arrow((370, 975), (370, 1030))
    
    # Right: Nearby Hospitals
    b.add_terminal([1210, 115, 1410, 160], "START (HOSPITAL)", is_start=True)
    b.add_process([1100, 190, 1520, 270], "H1.0 Open NearbyHospitalActivity", [
        "User chooses 'Nearby Hospitals & Clinics' from menu",
        "Validates GPS hardware status & permissions"
    ])
    b.add_subroutine([1100, 305, 1520, 390], "H2.0 Acquire User GPS Coordinates", [
        "fusedLocationClient.getCurrentLocation()",
        "Retrieves accurate user Latitude & Longitude"
    ])
    b.add_subroutine([1100, 425, 1520, 510], "H3.0 Query Medical Geospatial API", [
        "Submits geospatial query to medical facilities API",
        "Fetches hospitals, trauma centers & clinics within radius"
    ])
    b.add_process([1100, 545, 1520, 630], "H4.0 Calculate Distance & Sort", [
        "Computes direct distance using Haversine formula",
        "Sorts facilities ascending from nearest to farthest"
    ])
    b.add_io_box([1100, 665, 1520, 755], "H5.0 Render Hospital Directory", [
        "Displays facility name, address, distance (km),",
        "operational status, and emergency telephone number"
    ])
    b.add_decision(1310, 830, 270, 75, "User Action", ["Selection?"])
    
    b.add_process([980, 915, 1270, 995], "H6.1 Emergency Phone Call", [
        "Launches Android Dialer with",
        "hospital emergency hotline"
    ])
    
    b.add_process([1350, 915, 1640, 995], "H6.2 Google Maps Navigation", [
        "Launches Google Maps Intent with",
        "turn-by-turn driving route"
    ])
    
    b.add_terminal([1220, 1045, 1400, 1085], "END", is_start=False)
    
    b.add_arrow((1310, 160), (1310, 190))
    b.add_arrow((1310, 270), (1310, 305))
    b.add_arrow((1310, 390), (1310, 425))
    b.add_arrow((1310, 510), (1310, 545))
    b.add_arrow((1310, 630), (1310, 665))
    b.add_arrow((1310, 755), (1310, 792))
    
    b.add_poly_arrow([(1175, 830), (1125, 830), (1125, 915)], label="Phone Call", label_side="top")
    b.add_poly_arrow([(1445, 830), (1495, 830), (1495, 915)], label="Navigation", label_side="top")
    
    b.add_poly_arrow([(1125, 995), (1125, 1065), (1220, 1065)])
    b.add_poly_arrow([(1495, 995), (1495, 1065), (1400, 1065)])


def draw_fc7(b):
    b.add_header_banner(1700, 
        "FLOWCHART 7: WEB ADMIN PORTAL & CLOUD BROADCAST SYSTEM", 
        "Admin Authentication, Real-Time SOS Incident Control, Targeted FCM Broadcasts & Database Maintenance")
        
    b.add_terminal([750, 115, 950, 160], "START", is_start=True)
    b.add_process([640, 190, 1060, 270], "1.0 Access Web Admin Portal", [
        "Admin navigates to web portal (server.js /admin)",
        "Authentication handled via Firebase Auth"
    ])
    b.add_decision(850, 345, 310, 85, "Does User Have", ["Admin Privileges?"])
    
    b.add_process([150, 305, 500, 385], "2.0 Access Denied (403)", [
        "Email domain is not @resqtap.com and no /admins/{uid} entry",
        "System rejects access to administration console"
    ])
    b.add_terminal([270, 430, 380, 470], "END", is_start=False)
    
    b.add_process([640, 460, 1060, 545], "3.0 Admin Dashboard (admin.html)", [
        "Live system statistics: Total Users, Active Rooms & Active Alerts",
        "Operator selects management action"
    ])
    
    b.add_decision(850, 635, 300, 80, "Admin Operation", ["Selection?"])
    
    b.add_process([80, 730, 500, 855], "4.1 Live SOS Incident Control", [
        "Listens to incoming alerts on /rooms/*/sosAlerts",
        "Click 'Reserve Case' (served = true, servedBy)",
        "Update progress timeline (Dispatched -> Resolved)",
        "Direct emergency support chat with victim"
    ])
    
    b.add_process([610, 730, 1090, 840], "4.2 Targeted Broadcast System", [
        "Select target audience: All / Live Room Members / Admins",
        "Input alert title and notification message",
        "Invoke Callable Cloud Function: sendAdminNotification"
    ])
    
    b.add_subroutine([610, 880, 1090, 975], "4.2b Cloud Function FCM Blaster", [
        "Collects target user device FCM tokens",
        "Simultaneously dispatches High-Priority notifications",
        "Logs delivery audit to /admin_notifications/{id}"
    ])
    
    b.add_process([1180, 730, 1620, 840], "4.3 Maintenance & User Purge", [
        "Trigger user purge via /admin_user_deletions/{uid}",
        "Cloud Function purges user from Auth & RTDB completely",
        "Trigger DB account reset via /api/admin/clear-database"
    ])
    
    b.add_process([650, 1050, 1050, 1125], "5.0 Database Synchronized", [
        "Operation completed and synced to Firebase",
        "Dashboard statistics refreshed"
    ])
    b.add_terminal([760, 1170, 940, 1210], "END", is_start=False)
    
    # Arrows
    b.add_arrow((850, 160), (850, 190))
    b.add_arrow((850, 270), (850, 302))
    
    b.add_arrow((695, 345), (500, 345), label="No (Non-Admin)", label_side="top")
    b.add_arrow((325, 385), (325, 430))
    b.add_arrow((850, 387), (850, 460), label="Yes (Admin Granted)", label_side="right")
    
    b.add_arrow((850, 545), (850, 595))
    
    b.add_poly_arrow([(700, 635), (290, 635), (290, 730)], label="Manage SOS", label_side="top")
    b.add_arrow((850, 675), (850, 730), label="Broadcast", label_side="right")
    b.add_poly_arrow([(1000, 635), (1400, 635), (1400, 730)], label="Maintenance", label_side="top")
    
    b.add_arrow((850, 840), (850, 880))
    b.add_arrow((850, 975), (850, 1050))
    
    b.add_poly_arrow([(290, 855), (290, 1087), (650, 1087)])
    b.add_poly_arrow([(1400, 840), (1400, 1087), (1050, 1087)])
    
    b.add_arrow((850, 1125), (850, 1170))


FLOWCHARTS = [
    ("Flowchart_1_Overall_Architecture", 1680, 1260, draw_fc1),
    ("Flowchart_2_Authentication_Security", 1750, 1320, draw_fc2),
    ("Flowchart_3_OneTap_SOS", 1700, 1620, draw_fc3),
    ("Flowchart_4_Emergency_Room_Hub", 1680, 1420, draw_fc4),
    ("Flowchart_5_WebRTC_Intercom", 1700, 1400, draw_fc5),
    ("Flowchart_6_Reporting_Hospital", 1700, 1280, draw_fc6),
    ("Flowchart_7_Admin_Portal", 1700, 1280, draw_fc7),
]

def generate_individual_files(word, page_size="A4"):
    print(f"\n--- Generating 7 Individual Word Flowchart Documents ({page_size}) ---")
    for name, w, h, fn in FLOWCHARTS:
        doc = word.Documents.Add()
        sec = doc.Sections(1)
        builder = WordFlowchartBuilder(doc, sec, w, h, page_size=page_size)
        fn(builder)
        
        docx_path = os.path.join(OUTPUT_DIR, f"{name}.docx")
        doc.SaveAs(docx_path)
        doc.Close(False)
        print(f"Saved: {docx_path}")

def generate_master_document(word, filename, page_size="A4"):
    print(f"\n--- Generating Master Flowcharts Document: {filename} ({page_size}) ---")
    doc = word.Documents.Add()
    
    for idx, (name, w, h, fn) in enumerate(FLOWCHARTS):
        if idx > 0:
            rng = doc.Content
            rng.Collapse(0) # wdCollapseEnd
            rng.InsertBreak(2) # wdSectionBreakNextPage
            
        sec = doc.Sections(idx + 1)
        p = sec.Range.Paragraphs.Add()
        builder = WordFlowchartBuilder(doc, sec, w, h, page_size=page_size, anchor=p.Range)
        fn(builder)
        print(f"  Page {idx + 1}: {name} drawn successfully")
        
    docx_path = os.path.join(OUTPUT_DIR, filename)
    doc.SaveAs(docx_path)
    
    # Also export PDF to visually verify
    pdf_path = os.path.join(OUTPUT_DIR, filename.replace(".docx", ".pdf"))
    doc.ExportAsFixedFormat(pdf_path, 17) # wdExportFormatPDF
    doc.Close(False)
    print(f"Saved Master DOCX: {docx_path}")
    print(f"Exported PDF: {pdf_path}")
    return pdf_path

if __name__ == "__main__":
    word = win32com.client.Dispatch("Word.Application")
    word.Visible = False
    try:
        # 1. Generate Individual Files for each flowchart
        generate_individual_files(word, page_size="A4")
        
        # 2. Generate Master Document in A4 Landscape (Standard Print & Thesis)
        pdf_a4 = generate_master_document(word, "ResQTap_Flowcharts_Master_A4.docx", page_size="A4")
        
        # 3. Generate Master Document in A3 Landscape (High-Definition Engineering Sheet)
        pdf_a3 = generate_master_document(word, "ResQTap_Flowcharts_Master_A3.docx", page_size="A3")
        
        print("\nAll Word Flowchart files created successfully!")
    finally:
        word.Quit()
