const admin = require("firebase-admin");
const serviceAccount = require("C:\\Users\\Administrator\\Downloads\\resqtap-b9ff5-firebase-adminsdk-fbsvc-012665cfc9.json");

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://resqtap-b9ff5-default-rtdb.firebaseio.com"
  });
}

const db = admin.database();

async function triggerLiveSos() {
  const roomId = "ROOM-629S7X";
  const alertRef = db.ref(`rooms/${roomId}/sosAlerts`).push();
  const alertId = alertRef.key;
  const now = Date.now();

  const payload = {
    alertId: alertId,
    createdAt: now,
    status: "active",
    senderUid: "ZtIsALOniDcSpL3d5DIhXcDS8cE2",
    senderName: "CHESSTER",
    roomId: roomId,
    latitude: 5.2802,
    longitude: 100.4963
  };

  console.log("Pushing SOS alert to Firebase RTDB:", payload);
  await alertRef.set(payload);
  console.log("SOS Alert pushed successfully with ID:", alertId);

  // Auto-cancel after 30 seconds to allow inspection and slide test
  setTimeout(async () => {
    console.log("Auto-cancelling SOS alert...");
    await alertRef.update({
      status: "cancelled",
      cancelledAt: Date.now()
    });
    console.log("SOS alert cancelled.");
    process.exit(0);
  }, 30000);
}

triggerLiveSos().catch(err => {
  console.error("Error triggering SOS:", err);
  process.exit(1);
});
