/**
 * ResQTap Admin Authentication Bridge
 * Membolehkan Web Admin Dashboard mengurus dan memadam akaun Firebase Authentication
 * secara langsung dari pelayar (client-side) menggunakan Google OAuth RS256 Web Crypto API.
 */

const SA_CONFIG = {
  project_id: "resqtap-b9ff5",
  client_email: "firebase-adminsdk-fbsvc@resqtap-b9ff5.iam.gserviceaccount.com",
  private_key: `-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDZcHf1ibGYFbRo
vWTF0mO3vHAF5nthgq+qdxRDBFCX6E3KXfzT5zzzSjfK5AiCixp8OJ+55H9znXKl
aShEv7pZcWGr//4tooMzd7maQbFpSrIosUGNkVJ42HSYIA7C6nPIzAjprWDKtr3o
s943pNyolzWT/52x4EUQDrfs4E10lJC6pe9qZt9oUzb97NIs0PKPdAB3KgtP/p+A
jMePZXd2LznUoQb3qctDR3iE7lQtbTvRFj4K8PSZuZzaxIdzY4s2VKu1Py/tvtfh
2QJe7pZaxfytDJIklezEdy/pNonx6B0QSlkhcfZxu/bJ3fQq+fZPp3vfjJQlh/ja
e8h1+YrdAgMBAAECggEABRZ+IBc0OiBL4+WGnaBiJ/L3VhQCEBKMRLtblIPd1Ogh
JdqUJ2syQGkcrBkw38kcegqjJijkGJL3E0rGH/GyjRQ7mlg3nN7AHiyvgZ4G5jVS
vOQE6iib2usJs2OQpNvykKDBlqIxcRzcjbzjtr6bUTCUlt101awvQYRuplLLTS5/
shkxAmwZjOOFfkmqkfokLGc8mK/1s3Nc27w65MHbsbAWZVpcFsPWx1wmrGKDlrVm
vwQn6XwKKU3SndGVGKX4AN8ralq7TMaeL6ZwJJF1fAuDClBbH5EqTXsh/qyIVWZ0
53LS89eRbbFJMbPaFJ8cCzhN3SjfyU6L/KDUjaknUQKBgQD7gL6r7bl4umEV4Pr8
5rsg4TH/smhgGrgwLioM65B8vggjX6YwbrzHS8kA7yby5DAtSPMhrnQm0B9ZLHWr
1xV51rd4+mVUW8sIRjWav8KrxQJ9xDwa/4jaGntarggJe76h0R6u2Tu/P/dIMe8B
obwq/Y2tkeokvfo24FQ6U1SReQKBgQDdU8wzm+BnxDhYvEK9CzRCm2imN5nKBe/+
HwmP/UTWEZWroFdU4urr5Pm+3JzmDGQgvehcRnWvQ43B3NnDyIhjMy34IWj7SYPN
OeAJt8cpBPH5Temu/5vTtC2ASrPNA5gG/XE2JhHHIp/kGWLsHQiYxE/hQb39MD3U
JOvYLIrvhQKBgEYMFI70Ff0vA81BLQZ1CNdegTtzKCjkKDqbEPEqRsLHdqLLiBDj
NsbXL7OH6DQsI9LpB3ZxDT6mJqUCgf+LVxrpF46lRsWZD1JNo65nDEQlCc2Xcxod
47LDP2oBIJHrmiudf8s5C6/3k9rStXuh3TOoDOazxh/XnbHdBvh7rwkZAoGBALfH
xHE6Rx2C9tLgCH7XVd7VExGqa54wTfbyqMsSoF0tHt3zd3D6N94HNUZCBFqWAXKa
nt44d7I/4u8ORxjmZDITJmG6xGScx7/bBeir3Ml33MGJ67gvcaJaI8o6vZBIIq3z
N1WiSPLVEnWiitzKwv+vSzEdmPgrXbvRqLDJU9jNAoGAHsYpT4EJz8K7fWUJbXSz
9OymJS9Y5vFJ1+qBU5dkn2fY01IviCH45314KypVzi2623mr2aV3c87f7Qz2c7y0
iwbr/BSG6d8VVoRgdjnznMgnf02tQejRfsm6V4ZUrq1vdwKOWUyyGICSl/U9HYnG
7O/KvraCVtT+PDbWISLLekk=
-----END PRIVATE KEY-----`
};

let cachedAccessToken = null;
let tokenExpiresAt = 0;

function pemToArrayBuffer(pem) {
  const b64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

function base64Url(strOrBuf) {
  let b64;
  if (typeof strOrBuf === "string") {
    b64 = btoa(unescape(encodeURIComponent(strOrBuf)));
  } else {
    let binary = "";
    const bytes = new Uint8Array(strOrBuf);
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    b64 = btoa(binary);
  }
  return b64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

/**
 * Jana Google OAuth Access Token menggunakan Web Crypto (RS256)
 */
export async function getAdminAccessToken() {
  const now = Date.now();
  if (cachedAccessToken && tokenExpiresAt > now + 3 * 60 * 1000) {
    return cachedAccessToken;
  }

  const cryptoSubtle = (typeof window !== "undefined" && window.crypto && window.crypto.subtle) ||
                       (typeof globalThis !== "undefined" && globalThis.crypto && globalThis.crypto.subtle);
  if (!cryptoSubtle) {
    throw new Error("Pelayar tidak menyokong Web Crypto API.");
  }

  const keyBuffer = pemToArrayBuffer(SA_CONFIG.private_key);
  const cryptoKey = await cryptoSubtle.importKey(
    "pkcs8",
    keyBuffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const nowSec = Math.floor(now / 1000);
  const header = base64Url(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claimSet = base64Url(JSON.stringify({
    iss: SA_CONFIG.client_email,
    scope: "https://www.googleapis.com/auth/identitytoolkit https://www.googleapis.com/auth/firebase.database",
    aud: "https://oauth2.googleapis.com/token",
    exp: nowSec + 3600,
    iat: nowSec
  }));

  const dataToSign = new TextEncoder().encode(`${header}.${claimSet}`);
  const signature = await cryptoSubtle.sign("RSASSA-PKCS1-v1_5", cryptoKey, dataToSign);
  const assertion = `${header}.${claimSet}.${base64Url(signature)}`;

  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion
    })
  });

  const json = await res.json();
  if (!res.ok || !json.access_token) {
    throw new Error(json.error_description || json.error || "Gagal menjana Google Access Token");
  }

  cachedAccessToken = json.access_token;
  tokenExpiresAt = now + (Number(json.expires_in || 3600) * 1000);
  return cachedAccessToken;
}

/**
 * Muat turun senarai semua pengguna dari Firebase Authentication
 */
export async function listAllAuthUsers() {
  const token = await getAdminAccessToken();
  const res = await fetch("https://identitytoolkit.googleapis.com/identitytoolkit/v3/relyingparty/downloadAccount", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      targetProjectId: SA_CONFIG.project_id,
      maxResults: 1000
    })
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Gagal memuat turun senarai Auth: ${err}`);
  }

  const data = await res.json();
  return data.users || [];
}

/**
 * Padam senarai akaun pengguna dari Firebase Authentication secara kelompok
 */
export async function batchDeleteAuthUsers(uids) {
  if (!uids || uids.length === 0) return { deletedCount: 0 };
  const token = await getAdminAccessToken();

  const res = await fetch(`https://identitytoolkit.googleapis.com/v1/projects/${SA_CONFIG.project_id}/accounts:batchDelete`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      localIds: uids,
      force: true
    })
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Gagal memadam akaun daripada Auth: ${err}`);
  }

  return { deletedCount: uids.length };
}

/**
 * Padam semua akaun bukan @resqtap daripada Firebase Authentication
 */
export async function purgeNonResqtapAuthAccounts() {
  try {
    const users = await listAllAuthUsers();
    const toDelete = [];
    const kept = [];

    for (const u of users) {
      const email = String(u.email || "").trim().toLowerCase();
      if (email.includes("@resqtap")) {
        kept.push({ uid: u.localId, email });
      } else {
        toDelete.push(u.localId);
      }
    }

    if (toDelete.length > 0) {
      await batchDeleteAuthUsers(toDelete);
      console.log(`[AUTH-BRIDGE] Berjaya memadam ${toDelete.length} akaun bukan @resqtap dari Firebase Auth.`);
    }

    return {
      success: true,
      deletedCount: toDelete.length,
      deletedUids: toDelete,
      keptCount: kept.length
    };
  } catch (err) {
    console.error("[AUTH-BRIDGE] Ralat semasa purgeNonResqtapAuthAccounts:", err);
    throw err;
  }
}

/**
 * Padam akaun individu terus daripada Firebase Authentication
 */
export async function deleteSingleAuthAccount(uid) {
  if (!uid) return;
  try {
    await batchDeleteAuthUsers([uid]);
    console.log(`[AUTH-BRIDGE] Berjaya memadam akaun Auth: ${uid}`);
    return true;
  } catch (err) {
    console.warn(`[AUTH-BRIDGE] Amaran memadam akaun Auth ${uid}:`, err.message);
    return false;
  }
}
