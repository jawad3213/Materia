#!/usr/bin/env node
// =====================================================================================================
// Smoke test, run by the production CD right after the deploy: is the release up and serving?
// A handful of fast checks of the public entry points, not a test suite. Node 22, no dependencies.
//
//   BASE_URL         where the application is served (no trailing /)
//   SMOKE_EMAIL      optional: an account to sign in with, to check a real session end to end
//   SMOKE_PASSWORD   (reads its profile and the dashboard; changes nothing)
//
// Local run:  BASE_URL=http://localhost node tests/smoke/smoke-test.mjs
// =====================================================================================================
import { appendFileSync } from 'node:fs';

const BASE_URL = (process.env.BASE_URL || 'http://localhost').replace(/\/+$/, '');
const { SMOKE_EMAIL, SMOKE_PASSWORD } = process.env;
const SLOW_MS = 3000;

const results = [];

async function call(method, path, { token, body } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const started = Date.now();
  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(15000),
  });
  const text = await res.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch {
    json = undefined;
  }
  return { status: res.status, type: res.headers.get('content-type') || '', text, json, ms: Date.now() - started };
}

async function check(name, run) {
  try {
    const { ok, detail, ms } = await run();
    const slow = ms !== undefined && ms > SLOW_MS;
    results.push({ name, ok: ok && !slow, detail: `${detail}${ms !== undefined ? `, ${ms} ms` : ''}${slow ? ' (too slow)' : ''}` });
  } catch (error) {
    results.push({ name, ok: false, detail: String(error.message || error) });
  }
  const r = results.at(-1);
  console.log(`${r.ok ? 'PASS' : 'FAIL'}  ${r.name} - ${r.detail}`);
  return r.ok;
}

// The new containers may still be warming up: give the first check a few tries.
async function firstContact() {
  for (let attempt = 1; ; attempt++) {
    try {
      const res = await call('GET', '/api/v1/auth/me');
      if (res.status === 401) return res;
      if (attempt >= 6) return res;
    } catch (error) {
      if (attempt >= 6) throw error;
    }
    await new Promise((resolve) => setTimeout(resolve, 10000));
  }
}

await check('the API answers and asks for a session', async () => {
  const res = await firstContact();
  return { ok: res.status === 401 && res.type.includes('json'), detail: `HTTP ${res.status}`, ms: res.ms };
});

let assetPath;
await check('the web application is served', async () => {
  const res = await call('GET', '/');
  assetPath = res.text.match(/src="(\/assets\/[^"]+\.js)"/)?.[1];
  return { ok: res.status === 200 && res.type.includes('text/html') && !!assetPath, detail: `HTTP ${res.status}`, ms: res.ms };
});

await check('its JavaScript bundle is served', async () => {
  if (!assetPath) return { ok: false, detail: 'no bundle referenced by index.html' };
  const res = await call('GET', assetPath);
  return { ok: res.status === 200 && res.type.includes('javascript'), detail: `${assetPath}: HTTP ${res.status}`, ms: res.ms };
});

await check('a client-side route falls back to the application', async () => {
  const res = await call('GET', '/purchase-orders');
  return { ok: res.status === 200 && res.type.includes('text/html'), detail: `HTTP ${res.status}`, ms: res.ms };
});

await check('the backend validates requests', async () => {
  const res = await call('POST', '/api/v1/auth/login', { body: {} });
  return { ok: res.status === 400, detail: `HTTP ${res.status}`, ms: res.ms };
});

if (SMOKE_EMAIL && SMOKE_PASSWORD) {
  let token;
  let refreshToken;
  await check('the smoke account signs in', async () => {
    const res = await call('POST', '/api/v1/auth/login', { body: { email: SMOKE_EMAIL, password: SMOKE_PASSWORD } });
    token = res.json?.accessToken;
    refreshToken = res.json?.refreshToken;
    return { ok: res.status === 200 && !!token, detail: `HTTP ${res.status}`, ms: res.ms };
  });
  await check('its profile is read from the database', async () => {
    const res = await call('GET', '/api/v1/auth/me', { token });
    return { ok: res.status === 200 && res.json?.email?.toLowerCase() === SMOKE_EMAIL.toLowerCase(), detail: `HTTP ${res.status}`, ms: res.ms };
  });
  await check('the dashboard loads', async () => {
    const res = await call('GET', '/api/v1/dashboard', { token });
    return { ok: res.status === 200 || res.status === 403, detail: `HTTP ${res.status}${res.status === 403 ? ' (no dashboard permission)' : ''}`, ms: res.ms };
  });
  if (refreshToken) await call('POST', '/api/v1/auth/logout', { body: { refreshToken } }).catch(() => {});
} else {
  console.log('SKIP  signed-in checks (SMOKE_EMAIL / SMOKE_PASSWORD not set)');
}

const failed = results.filter((r) => !r.ok);
const summary = [
  '## Smoke test',
  '',
  `Target: \`${BASE_URL}\` - ${results.length - failed.length}/${results.length} passed`,
  '',
  '| | Check | Detail |',
  '| :-: | :--- | :--- |',
  ...results.map((r) => `| ${r.ok ? '✅' : '❌'} | ${r.name} | ${r.detail} |`),
  '',
].join('\n');
if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY, summary);
process.exit(failed.length ? 1 : 0);
