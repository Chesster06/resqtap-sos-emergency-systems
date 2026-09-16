const admin = require("firebase-admin");
const serviceAccount = require("C:\\Users\\Administrator\\Downloads\\resqtap-b9ff5-firebase-adminsdk-fbsvc-012665cfc9.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com"
});

const db = admin.database();

async function main() {
  console.log("==================================================");
  console.log("   RESET DATABASE ACCOUNT SELAIN @RESQTAP");
  console.log("==================================================");

  // 1. Get all users from Auth and RTDB
  const listResult = await admin.auth().listUsers(1000);
  const usersSnap = await db.ref("users").once("value");
  const rtdbUsers = usersSnap.val() || {};

  const resqtapUids = new Set();
  const deleteUids = new Set();
  const deleteEmails = new Set();
  const deletePublicIds = new Set();

  // Scan Auth users
  for (const user of listResult.users) {
    const email = (user.email || "").trim().toLowerCase();
    if (email.includes("@resqtap")) {
      resqtapUids.add(user.uid);
      console.log(`[AUTH] KEEP: ${user.uid} (${email})`);
    } else {
      deleteUids.add(user.uid);
      if (email) deleteEmails.add(email);
      console.log(`[AUTH] TO DELETE: ${user.uid} (${email})`);
    }
  }

  // Scan RTDB users
  for (const [uid, data] of Object.entries(rtdbUsers)) {
    const email = (data.email || "").trim().toLowerCase();
    if (email.includes("@resqtap")) {
      resqtapUids.add(uid);
      console.log(`[RTDB] KEEP: ${uid} (${email})`);
    } else {
      deleteUids.add(uid);
      if (email) deleteEmails.add(email);
      if (data.publicId) deletePublicIds.add(String(data.publicId).trim().toUpperCase());
      console.log(`[RTDB] TO DELETE: ${uid} (${email})`);
    }
  }

  console.log(`\nSummary: Keep ${resqtapUids.size} account(s), Delete ${deleteUids.size} account(s)\n`);

  if (deleteUids.size === 0 && deleteEmails.size === 0) {
    console.log("Tiada akaun selain @resqtap untuk dipadam.");
    process.exit(0);
  }

  const updates = {};

  // 2. Clear registeredEmails
  const emailsSnap = await db.ref("registeredEmails").once("value");
  if (emailsSnap.exists()) {
    for (const [key, val] of Object.entries(emailsSnap.val() || {})) {
      const decoded = key.replace(/_at_/g, "@").replace(/_/g, ".");
      if (!decoded.toLowerCase().includes("@resqtap")) {
        console.log(`- Remove registeredEmail: ${key} (${decoded})`);
        updates[`registeredEmails/${key}`] = null;
      }
    }
  }

  // 3. Clear publicIds
  const publicIdsSnap = await db.ref("publicIds").once("value");
  if (publicIdsSnap.exists()) {
    for (const [pid, val] of Object.entries(publicIdsSnap.val() || {})) {
      const targetUid = typeof val === "string" ? val : (val && val.uid ? val.uid : "");
      if (deleteUids.has(targetUid) || deletePublicIds.has(pid.toUpperCase()) || !resqtapUids.has(targetUid)) {
        console.log(`- Remove publicId: ${pid}`);
        updates[`publicIds/${pid}`] = null;
      }
    }
  }

  // 4. Delete user-specific top-level nodes for deleted UIDs
  const userNodes = [
    "users", "admins", "supportChats", "aiChats",
    "userNotifications", "notifications",
    "sos_history", "sos_alerts", "sos_status",
    "live_locations", "userFriends", "friendRequests",
    "sentRequests", "userRooms", "userCalls"
  ];

  for (const node of userNodes) {
    for (const uid of deleteUids) {
      updates[`${node}/${uid}`] = null;
    }
  }

  // 5. Clean cross-references in remaining @resqtap users
  // (a) userFriends
  const friendsSnap = await db.ref("userFriends").once("value");
  if (friendsSnap.exists()) {
    for (const [uid, friends] of Object.entries(friendsSnap.val() || {})) {
      if (deleteUids.has(uid)) continue;
      if (friends && typeof friends === "object") {
        for (const friendUid of Object.keys(friends)) {
          if (deleteUids.has(friendUid)) {
            console.log(`- Remove friend ${friendUid} from user ${uid}`);
            updates[`userFriends/${uid}/${friendUid}`] = null;
          }
        }
      }
    }
  }

  // (b) friendRequests
  const freqSnap = await db.ref("friendRequests").once("value");
  if (freqSnap.exists()) {
    for (const [uid, requests] of Object.entries(freqSnap.val() || {})) {
      if (deleteUids.has(uid)) continue;
      if (requests && typeof requests === "object") {
        for (const reqSenderUid of Object.keys(requests)) {
          if (deleteUids.has(reqSenderUid)) {
            console.log(`- Remove friendRequest from ${reqSenderUid} for user ${uid}`);
            updates[`friendRequests/${uid}/${reqSenderUid}`] = null;
          }
        }
      }
    }
  }

  // (c) sentRequests
  const sentSnap = await db.ref("sentRequests").once("value");
  if (sentSnap.exists()) {
    for (const [uid, requests] of Object.entries(sentSnap.val() || {})) {
      if (deleteUids.has(uid)) continue;
      if (requests && typeof requests === "object") {
        for (const targetUid of Object.keys(requests)) {
          if (deleteUids.has(targetUid)) {
            console.log(`- Remove sentRequest to ${targetUid} from user ${uid}`);
            updates[`sentRequests/${uid}/${targetUid}`] = null;
          }
        }
      }
    }
  }

  // (d) userNotifications
  const notifSnap = await db.ref("userNotifications").once("value");
  if (notifSnap.exists()) {
    for (const [uid, notifs] of Object.entries(notifSnap.val() || {})) {
      if (deleteUids.has(uid)) continue;
      if (notifs && typeof notifs === "object") {
        for (const [notifId, notif] of Object.entries(notifs)) {
          // Check if notification is related to deleted users
          const msg = notif.message || "";
          const title = notif.title || "";
          let related = false;
          for (const dUid of deleteUids) {
            if (notifId.includes(dUid.substring(0, 4)) || notif.fromUid === dUid) {
              related = true;
              break;
            }
          }
          if (related) {
            console.log(`- Remove notification ${notifId} for user ${uid}`);
            updates[`userNotifications/${uid}/${notifId}`] = null;
          }
        }
      }
    }
  }

  // 6. Clean Rooms
  const roomsSnap = await db.ref("rooms").once("value");
  if (roomsSnap.exists()) {
    for (const [roomId, room] of Object.entries(roomsSnap.val() || {})) {
      if (!room) continue;
      const creatorUid = room.creatorUid;
      if (deleteUids.has(creatorUid)) {
        console.log(`- Delete room ${roomId} created by non-resqtap user ${creatorUid}`);
        updates[`rooms/${roomId}`] = null;
        // Also remove this room from all users' userRooms
        for (const uid of resqtapUids) {
          updates[`userRooms/${uid}/${roomId}`] = null;
        }
      } else {
        // Room created by resqtap user -> remove deleted members
        if (room.members && typeof room.members === "object") {
          for (const memberUid of Object.keys(room.members)) {
            if (deleteUids.has(memberUid)) {
              console.log(`- Remove member ${memberUid} from room ${roomId}`);
              updates[`rooms/${roomId}/members/${memberUid}`] = null;
              if (room.bells && room.bells[memberUid]) {
                updates[`rooms/${roomId}/bells/${memberUid}`] = null;
              }
            }
          }
        }
      }
    }
  }

  // 7. Clean incidentReports if any
  const incSnap = await db.ref("incidentReports").once("value");
  if (incSnap.exists()) {
    for (const [reportId, report] of Object.entries(incSnap.val() || {})) {
      if (report && deleteUids.has(report.senderUid)) {
        console.log(`- Remove incidentReport ${reportId} by ${report.senderUid}`);
        updates[`incidentReports/${reportId}`] = null;
      }
    }
  }

  // Apply RTDB updates
  console.log(`\nExecuting ${Object.keys(updates).length} RTDB updates...`);
  if (Object.keys(updates).length > 0) {
    await db.ref().update(updates);
    console.log("RTDB updates applied successfully.");
  }

  // 8. Delete users from Firebase Auth
  console.log("\nDeleting users from Firebase Auth...");
  for (const uid of deleteUids) {
    try {
      await admin.auth().deleteUser(uid);
      console.log(`[AUTH] Deleted user: ${uid}`);
    } catch (e) {
      if (e.code === "auth/user-not-found") {
        console.log(`[AUTH] User ${uid} already not in Auth.`);
      } else {
        console.warn(`[AUTH] Warning deleting ${uid}:`, e.message);
      }
    }
  }

  console.log("\n==================================================");
  console.log("   RESET SELESAI! SEMUA AKAUN SELAIN @RESQTAP");
  console.log("   TELAH DIPADAM SEPENUHNYA DARI DATABASE & AUTH.");
  console.log("==================================================");
}

main().catch(err => {
  console.error("FATAL ERROR:", err);
  process.exit(1);
});