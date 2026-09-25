let admin;
try {
  admin = require("firebase-admin");
} catch (e) {
  admin = require("./firebase-functions/node_modules/firebase-admin");
}
const serviceAccount = require("C:\\Users\\Administrator\\Downloads\\resqtap-b9ff5-firebase-adminsdk-fbsvc-012665cfc9.json");

if (!admin.apps || admin.apps.length === 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com"
  });
}

const db = admin.database();

async function resetDatabaseAccounts() {
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

  const updates = {};

  // 2. Clear registeredEmails (Always check and remove non-@resqtap orphan emails)
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
        // Clean messages within the room sent by deleted users
        if (room.messages && typeof room.messages === "object") {
          for (const [msgId, msg] of Object.entries(room.messages)) {
            if (msg && deleteUids.has(msg.senderUid)) {
              console.log(`- Remove message ${msgId} sent by ${msg.senderUid} from room ${roomId}`);
              updates[`rooms/${roomId}/messages/${msgId}`] = null;
              stats.messagesRemoved++;
            }
          }
        }

        // Clean sosAlerts within the room sent by deleted users
        if (room.sosAlerts && typeof room.sosAlerts === "object") {
          for (const [alertId, alert] of Object.entries(room.sosAlerts)) {
            if (alert && (deleteUids.has(alert.fromUid) || deleteUids.has(alert.senderUid))) {
              console.log(`- Remove sosAlert ${alertId} by ${alert.fromUid || alert.senderUid} from room ${roomId}`);
              updates[`rooms/${roomId}/sosAlerts/${alertId}`] = null;
              stats.sosAlertsRemoved++;
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
        stats.incidentReportsRemoved++;
      }
    }
  }

  // 8. Clean calls associated with deleted UIDs
  const callsSnap = await db.ref("calls").once("value");
  if (callsSnap.exists()) {
    for (const [callId, callData] of Object.entries(callsSnap.val() || {})) {
      if (callData && (deleteUids.has(callData.callerUid) || deleteUids.has(callData.calleeUid))) {
        console.log(`- Remove call ${callId}`);
        updates[`calls/${callId}`] = null;
        stats.callsRemoved++;
      }
    }
  }

  // 8b. Clean call_logs associated with deleted UIDs
  const callLogsSnap = await db.ref("call_logs").once("value");
  if (callLogsSnap.exists()) {
    for (const [callLogId, callLogData] of Object.entries(callLogsSnap.val() || {})) {
      if (callLogData && (deleteUids.has(callLogData.callerUid) || deleteUids.has(callLogData.calleeUid))) {
        console.log(`- Remove call_log ${callLogId}`);
        updates[`call_logs/${callLogId}`] = null;
      }
    }
  }

  // 9. Clean adminCalls/incoming if caller is a deleted user or test call left over
  const adminCallsSnap = await db.ref("adminCalls/incoming").once("value");
  if (adminCallsSnap.exists()) {
    const call = adminCallsSnap.val();
    if (call && (deleteUids.has(call.callerUid) || !call.status || call.status === "cancelled" || call.status === "declined" || call.status === "ended")) {
      console.log(`- Clear stale/deleted adminCalls/incoming (caller: ${call.callerUid || "unknown"})`);
      updates["adminCalls/incoming"] = null;
      stats.adminCallsRemoved++;
    }
  }

  // 10. Clean standalone / legacy sos_alerts
  const topSosSnap = await db.ref("sos_alerts").once("value");
  if (topSosSnap.exists()) {
    for (const [alertId, alert] of Object.entries(topSosSnap.val() || {})) {
      if (alert && (deleteUids.has(alert.senderUid) || deleteUids.has(alert.fromUid))) {
        console.log(`- Remove standalone sos_alert ${alertId} by ${alert.senderUid || alert.fromUid}`);
        updates[`sos_alerts/${alertId}`] = null;
        stats.sosAlertsRemoved++;
      }
    }
  }

  // 11. Record Session Audit Log into admin_audit_logs for Admin Dashboard
  const auditLogId = `audit_${Date.now()}`;
  const auditTimestamp = Date.now();
  updates[`admin_audit_logs/${auditLogId}`] = {
    id: auditLogId,
    type: "Database Reset",
    action: "clear_database",
    triggeredBy: "Admin Session",
    accountsDeleted: deleteUids.size,
    accountsKept: resqtapUids.size,
    roomsDeleted: stats.roomsDeleted,
    messagesRemoved: stats.messagesRemoved,
    sosAlertsRemoved: stats.sosAlertsRemoved,
    callsRemoved: stats.callsRemoved,
    incidentReportsRemoved: stats.incidentReportsRemoved,
    details: `Sesi pembersihan database selesai: ${deleteUids.size} akaun, ${stats.roomsDeleted} bilik, ${stats.messagesRemoved} mesej, dan ${stats.callsRemoved} panggilan dipadam.`,
    timestamp: auditTimestamp,
    createdAt: auditTimestamp
  };

  // Apply RTDB updates
  stats.totalRtdbUpdates = Object.keys(updates).length;
  console.log(`\nExecuting ${stats.totalRtdbUpdates} RTDB updates...`);
  if (stats.totalRtdbUpdates > 0) {
    await db.ref().update(updates);
    console.log("RTDB updates applied successfully.");
  }

  // 12. Delete users from Firebase Auth
  console.log("\nDeleting users from Firebase Auth...");
  for (const uid of deleteUids) {
    try {
      await admin.auth().deleteUser(uid);
      console.log(`[AUTH] Deleted user: ${uid}`);
      stats.authDeleted++;
    } catch (e) {
      if (e.code === "auth/user-not-found") {
        console.log(`[AUTH] User ${uid} already not in Auth.`);
      } else {
        console.warn(`[AUTH] Warning deleting ${uid}:`, e.message);
      }
    }
  }

  const durationSec = ((Date.now() - startTime) / 1000).toFixed(2);

  console.log("\n==================================================");
  console.log("             STATISTIK PEMBERSIHAN                ");
  console.log("==================================================");
  console.log(`- Akaun Auth Dipadam        : ${stats.authDeleted}`);
  console.log(`- Akaun @resqtap Dikekalkan : ${resqtapUids.size}`);
  console.log(`- Profil Pengguna RTDB      : ${deleteUids.size}`);
  console.log(`- Public IDs Dibuang        : ${deletePublicIds.size}`);
  console.log(`- Emel Dibuang              : ${deleteEmails.size}`);
  console.log(`- Bilik Dipadam Penuh       : ${stats.roomsDeleted}`);
  console.log(`- Ahli Bilik Dikeluarkan    : ${stats.membersRemoved}`);
  console.log(`- Mesej Sembang Dibuang     : ${stats.messagesRemoved}`);
  console.log(`- Kes SOS Dibuang           : ${stats.sosAlertsRemoved}`);
  console.log(`- Panggilan Dibuang         : ${stats.callsRemoved}`);
  console.log(`- Panggilan Masuk Admin     : ${stats.adminCallsRemoved}`);
  console.log(`- Laporan Insiden Dibuang   : ${stats.incidentReportsRemoved}`);
  console.log(`- Jumlah Kemaskini RTDB     : ${stats.totalRtdbUpdates}`);
  console.log(`- Masa Diambil              : ${durationSec}s`);
  console.log("==================================================");
  console.log("   RESET SELESAI! SEMUA AKAUN SELAIN @RESQTAP");
  console.log("   TELAH DIPADAM SEPENUHNYA DARI DATABASE & AUTH.");
  console.log("==================================================");

  return {
    success: true,
    keepCount: resqtapUids.size,
    deleteCount: deleteUids.size,
    deletedUids: Array.from(deleteUids),
    deletedEmails: Array.from(deleteEmails),
    stats,
    durationSec,
    message: `Reset selesai! ${deleteUids.size} akaun selain @resqtap telah dipadam sepenuhnya dari Database & Auth (${durationSec}s).`
  };
}

if (require.main === module) {
  resetDatabaseAccounts()
    .then((res) => {
      console.log(res.message);
      process.exit(0);
    })
    .catch(err => {
      console.error("FATAL ERROR:", err);
      process.exit(1);
    });
}

module.exports = { resetDatabaseAccounts };