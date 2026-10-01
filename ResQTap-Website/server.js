const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 5000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.mp3': 'audio/mpeg',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf'
};

const cleanupScriptPath = path.join(__dirname, '..', 'cleanup_rtdb.js');
const { resetDatabaseAccounts, deleteSingleUser, getAdmin } = require(cleanupScriptPath);

// Start RTDB Cloud Task Watcher so admin actions taken on hosted web app (Firebase Hosting)
// automatically trigger Firebase Auth user deletions
async function startRtdbTaskWatcher() {
  try {
    const adminInstance = await getAdmin();
    const db = adminInstance.database();
    console.log('[WATCHER] RTDB Task Watcher active: listening for admin tasks & user deletions...');

    // 1. Listen for Clear Database requests
    db.ref('admin_tasks/clear_database').on('value', async (snapshot) => {
      const task = snapshot.val();
      if (task && task.status === 'pending') {
        console.log('[WATCHER] Detected pending clear_database task. Executing full reset...');
        try {
          await db.ref('admin_tasks/clear_database/status').set('processing');
          const result = await resetDatabaseAccounts();
          await db.ref('admin_tasks/clear_database').update({
            status: 'completed',
            completedAt: Date.now(),
            summary: result.message
          });
          console.log('[WATCHER] Clear database task completed successfully.');
        } catch (err) {
          console.error('[WATCHER] Failed clear_database task:', err);
          await db.ref('admin_tasks/clear_database').update({
            status: 'failed',
            error: err.message || String(err)
          });
        }
      }
    });

    // 2. Listen for individual user deletion requests
    db.ref('admin_user_deletions').on('child_added', async (snapshot) => {
      const uid = snapshot.key;
      const data = snapshot.val();
      if (data && data.status === 'pending') {
        console.log(`[WATCHER] Detected pending user deletion for ${uid}...`);
        try {
          await snapshot.ref.update({ status: 'processing' });
          await deleteSingleUser(uid, data.email || '');
          await snapshot.ref.update({
            status: 'completed',
            completedAt: Date.now()
          });
          console.log(`[WATCHER] Successfully deleted user ${uid} from Auth & RTDB.`);
        } catch (err) {
          console.error(`[WATCHER] Failed user deletion for ${uid}:`, err);
          await snapshot.ref.update({
            status: 'failed',
            error: err.message || String(err)
          });
        }
      }
    });
  } catch (err) {
    console.error('[WATCHER] Could not start RTDB task watcher:', err.message);
  }
}

const server = http.createServer((req, res) => {
  const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(urlObj.pathname);

  // Handle CORS Preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    });
    res.end();
    return;
  }

  // Admin API: Reset / Clear Database for non-@resqtap accounts
  if (pathname === '/api/admin/clear-database' || pathname === '/api/admin/reset-database') {
    if (req.method !== 'POST') {
      res.writeHead(405, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
      res.end(JSON.stringify({ error: 'Method Not Allowed' }));
      return;
    }

    (async () => {
      try {
        console.log('[SERVER API] Received request to clear database for non-@resqtap accounts...');
        delete require.cache[require.resolve(cleanupScriptPath)];
        const { resetDatabaseAccounts: runReset } = require(cleanupScriptPath);
        const result = await runReset();
        res.writeHead(200, {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify(result));
      } catch (err) {
        console.error('[SERVER API] Clear database error:', err);
        res.writeHead(500, {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify({ success: false, error: err.message || 'Internal server error' }));
      }
    })();
    return;
  }

  // Admin API: Delete Single User
  if (pathname === '/api/admin/delete-user') {
    if (req.method !== 'POST') {
      res.writeHead(405, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
      res.end(JSON.stringify({ error: 'Method Not Allowed' }));
      return;
    }

    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const payload = JSON.parse(body || '{}');
        const uid = payload.uid;
        const email = payload.email || '';
        if (!uid) {
          res.writeHead(400, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
          res.end(JSON.stringify({ success: false, error: 'UID is required' }));
          return;
        }

        console.log(`[SERVER API] Received request to delete user ${uid}...`);
        delete require.cache[require.resolve(cleanupScriptPath)];
        const { deleteSingleUser: runDelete } = require(cleanupScriptPath);
        const result = await runDelete(uid, email);
        res.writeHead(200, {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify(result));
      } catch (err) {
        console.error('[SERVER API] Delete user error:', err);
        res.writeHead(500, {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify({ success: false, error: err.message || 'Internal server error' }));
      }
    });
    return;
  }

  // Rewrites matching serve.json & firebase.json
  if (pathname === '/admin' || pathname.startsWith('/admin/')) {
    pathname = '/admin.html';
  } else if (pathname === '/download') {
    pathname = '/index.html';
  }

  let filePath = path.join(PUBLIC_DIR, pathname);

  // Check if directory -> index.html
  if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
    filePath = path.join(filePath, 'index.html');
  }

  // Clean URLs: /flow -> /flow.html
  if (!fs.existsSync(filePath) && fs.existsSync(filePath + '.html')) {
    filePath = filePath + '.html';
  }

  // SPA fallback
  if (!fs.existsSync(filePath)) {
    filePath = path.join(PUBLIC_DIR, 'index.html');
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('500 Internal Server Error');
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    res.writeHead(200, {
      'Content-Type': contentType,
      'Access-Control-Allow-Origin': '*',
      'Cache-Control': 'no-cache, no-store, must-revalidate'
    });
    res.end(data);
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`ResQTap Website running at http://localhost:${PORT}`);
  console.log(`Admin Dashboard: http://localhost:${PORT}/admin`);
  startRtdbTaskWatcher();
});
