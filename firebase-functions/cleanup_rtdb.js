const admin = require("firebase-admin");
const serviceAccount = require("C:\\Users\\Administrator\\Downloads\\resqtap-b9ff5-firebase-adminsdk-fbsvc-012665cfc9.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com"
});

const db = admin.database();

async function main() {
  console.log("Membaca semua users dari RTDB...");
  const usersSnap = await db.ref("users").once("value");
  
  if (!usersSnap.exists()) {
    console.log("Tiada users dalam RTDB.");
    process.exit(0);
  }

  const allUsers = usersSnap.val();
  const toDelete = [];
  const toKeep = [];

  for (const [uid, data] of Object.entries(allUsers)) {
    const email = (data.email || "").trim().toLowerCase();
    if (email.includes("@resqtap")) {
      toKeep.push({ uid, email });
    } else {
      toDelete.push({ uid, email });
    }
  }

  console.log(`\nKekal (admin @resqtap): ${toKeep.length}`);
  toKeep.forEach(u => console.log(`  KEEP: ${u.uid} | ${u.email}`));

  console.log(`\nAkan dipadam: ${toDelete.length}`);
  toDelete.forEach(u => console.log(`  DELETE: ${u.uid} | ${u.email}`));

  if (toDelete.length === 0) {
    console.log("\nTiada user untuk dipadam.");
    process.exit(0);
  }

  const updates = {};
  for (const { uid } of toDelete) {
    updates[`users/${uid}`] = null;
    updates[`admins/${uid}`] = null;
    updates[`supportChats/${uid}`] = null;
    updates[`aiChats/${uid}`] = null;
    updates[`userNotifications/${uid}`] = null;
    updates[`notifications/${uid}`] = null;
    updates[`sos_history/${uid}`] = null;
    updates[`sos_alerts/${uid}`] = null;
    updates[`sos_status/${uid}`] = null;
    updates[`live_locations/${uid}`] = null;
    updates[`userFriends/${uid}`] = null;
    updates[`friendRequests/${uid}`] = null;
    updates[`sentRequests/${uid}`] = null;
    updates[`userRooms/${uid}`] = null;
  }

  // Also clean registeredEmails for deleted users
  for (const { uid, email } of toDelete) {
    if (email) {
      const sanitized = email.replace(/\./g, "_").replace(/@/g, "_at_");
      updates[`registeredEmails/${sanitized}`] = null;
    }
    // Clean publicId
    const pidSnap = await db.ref(`users/${uid}/publicId`).once("value");
    if (pidSnap.exists()) {
      const pid = String(pidSnap.val()).trim().toUpperCase();
      if (pid) updates[`publicIds/${pid}`] = null;
    }
  }

  console.log(`\nMemadam ${Object.keys(updates).length} paths dari RTDB...`);
  await db.ref().update(updates);
  console.log("DONE! Semua non-@resqtap users telah dipadam dari RTDB.");
  process.exit(0);
}

main().catch(err => {
  console.error("ERROR:", err);
  process.exit(1);
});