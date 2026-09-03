const admin = require("firebase-admin");
const serviceAccount = require("C:\\Users\\Administrator\\Downloads\\resqtap-b9ff5-firebase-adminsdk-fbsvc-012665cfc9.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com"
});

const db = admin.database();

async function main() {
  // 1. Clear registeredEmails yang bukan @resqtap
  console.log("=== REGISTERED EMAILS ===");
  const emailsSnap = await db.ref("registeredEmails").once("value");
  const emailUpdates = {};
  if (emailsSnap.exists()) {
    for (const [key, val] of Object.entries(emailsSnap.val() || {})) {
      const decoded = key.replace(/_at_/g, "@").replace(/_/g, ".");
      if (decoded.includes("@resqtap")) {
        console.log(`  KEEP: ${key} (${decoded})`);
      } else {
        console.log(`  DELETE: ${key} (${decoded})`);
        emailUpdates[key] = null;
      }
    }
  }
  if (Object.keys(emailUpdates).length > 0) {
    await db.ref("registeredEmails").update(emailUpdates);
    console.log(`Deleted ${Object.keys(emailUpdates).length} non-resqtap emails.`);
  } else {
    console.log("No non-resqtap emails found.");
  }

  // 2. Clear semua node lain yang ada non-resqtap UIDs
  // First get resqtap UIDs from users node
  console.log("\n=== USERS (check survivors) ===");
  const usersSnap = await db.ref("users").once("value");
  const resqtapUids = new Set();
  if (usersSnap.exists()) {
    for (const [uid, data] of Object.entries(usersSnap.val() || {})) {
      const email = (data.email || "").trim().toLowerCase();
      if (email.includes("@resqtap")) {
        resqtapUids.add(uid);
        console.log(`  KEEP: ${uid} | ${email}`);
      } else {
        console.log(`  STALE (will delete): ${uid} | ${email}`);
      }
    }
  }

  // 3. Scan and clean ALL top-level nodes for non-resqtap UIDs
  const nodesToClean = [
    "users", "admins", "supportChats", "aiChats",
    "userNotifications", "notifications", 
    "sos_history", "sos_alerts", "sos_status",
    "live_locations", "userFriends", "friendRequests",
    "sentRequests", "userRooms", "publicIds"
  ];

  for (const node of nodesToClean) {
    console.log(`\n=== ${node} ===`);
    const snap = await db.ref(node).once("value");
    if (!snap.exists()) {
      console.log("  (empty)");
      continue;
    }
    const updates = {};
    let keepCount = 0;
    let deleteCount = 0;
    for (const key of Object.keys(snap.val() || {})) {
      if (resqtapUids.has(key)) {
        keepCount++;
      } else {
        // For publicIds, check if value points to resqtap uid
        if (node === "publicIds") {
          const val = snap.val()[key];
          if (typeof val === "string" && resqtapUids.has(val)) {
            keepCount++;
            continue;
          }
          if (typeof val === "object" && val && val.uid && resqtapUids.has(val.uid)) {
            keepCount++;
            continue;
          }
        }
        deleteCount++;
        updates[key] = null;
        console.log(`  DELETE: ${key}`);
      }
    }
    if (deleteCount > 0) {
      await db.ref(node).update(updates);
    }
    console.log(`  Keep: ${keepCount}, Deleted: ${deleteCount}`);
  }

  // 4. Delete non-resqtap users from Firebase Auth
  console.log("\n=== FIREBASE AUTH CLEANUP ===");
  let nextPageToken;
  let authDeleted = 0;
  let authKept = 0;
  do {
    const listResult = await admin.auth().listUsers(1000, nextPageToken);
    for (const user of listResult.users) {
      const email = (user.email || "").trim().toLowerCase();
      if (email.includes("@resqtap")) {
        console.log(`  KEEP AUTH: ${user.uid} | ${email}`);
        authKept++;
      } else {
        console.log(`  DELETE AUTH: ${user.uid} | ${email}`);
        await admin.auth().deleteUser(user.uid);
        authDeleted++;
      }
    }
    nextPageToken = listResult.pageToken;
  } while (nextPageToken);
  console.log(`Auth - Keep: ${authKept}, Deleted: ${authDeleted}`);

  console.log("\n=== DONE! Semua non-@resqtap data cleared. ===");
  process.exit(0);
}

main().catch(err => {
  console.error("ERROR:", err);
  process.exit(1);
});