// =====================================================================================================
// Performance test of the API (k6), run by the staging CD after every deploy.
//
//   BASE_URL       where the application is served, e.g. http://staging.example.com (no trailing /)
//   TEST_EMAIL     an account that can read the main screens (the CD uses a dedicated staging account)
//   TEST_PASSWORD
//   VUS            peak virtual users (default 20)
//   DURATION       how long the peak lasts (default 2m)
//
// One sign-in, then every virtual user walks through the screens people use most (dashboard, orders,
// suppliers, materials, invoices...) with think time. The run fails when a threshold below is crossed.
// Read-only: it changes no data.
//
// Local run:  k6 run -e BASE_URL=http://localhost -e TEST_EMAIL=... -e TEST_PASSWORD=... tests/performance/load-test.js
// =====================================================================================================
import http from 'k6/http';
import { check, fail, group, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost').replace(/\/+$/, '');
const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || '2m';

const throttled = new Counter('throttled_requests');

export const options = {
  scenarios: {
    browse: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: VUS }, // ramp up
        { duration: DURATION, target: VUS }, // steady load
        { duration: '20s', target: 0 }, // ramp down
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // Errors: under 1% of the requests, and every functional check passing
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
    // Latency of the API calls (static files excluded)
    'http_req_duration{kind:api}': ['p(95)<800', 'p(99)<2000'],
    // The per-client rate limit must not be what the test measures (see api_rate_limit_per_minute)
    throttled_requests: ['count<1'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// Pages read by the virtual users, each with the share of the traffic it gets
const SCREENS = [
  { name: 'dashboard', path: '/api/v1/dashboard', weight: 3 },
  { name: 'profile', path: '/api/v1/auth/me', weight: 1 },
  { name: 'purchase orders', path: '/api/v1/purchase-orders?page=0&size=20', weight: 3 },
  { name: 'requisitions', path: '/api/v1/purchase-requisitions?page=0&size=20', weight: 2 },
  { name: 'suppliers', path: '/api/v1/masterdata/suppliers?page=0&size=20', weight: 2 },
  { name: 'materials', path: '/api/v1/masterdata/materials?page=0&size=20', weight: 2 },
  { name: 'goods receipts', path: '/api/v1/goods-receipts?page=0&size=20', weight: 1 },
  { name: 'invoices', path: '/api/v1/invoices?page=0&size=20', weight: 2 },
  { name: 'payments', path: '/api/v1/payments?page=0&size=20', weight: 1 },
];
const WEIGHTED = SCREENS.flatMap((screen) => Array(screen.weight).fill(screen));

export function setup() {
  if (!__ENV.TEST_EMAIL || !__ENV.TEST_PASSWORD) {
    fail('TEST_EMAIL and TEST_PASSWORD are required');
  }
  const res = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: __ENV.TEST_EMAIL, password: __ENV.TEST_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' }, tags: { kind: 'login' } },
  );
  if (res.status !== 200 || !res.json('accessToken')) {
    fail(`sign-in failed: HTTP ${res.status} ${String(res.body).slice(0, 200)}`);
  }
  // Every screen must answer before the load starts: a 403 here means the account lacks a permission
  const headers = { Authorization: `Bearer ${res.json('accessToken')}` };
  for (const screen of SCREENS) {
    const probe = http.get(`${BASE_URL}${screen.path}`, { headers, tags: { kind: 'probe' } });
    if (probe.status !== 200) {
      fail(`${screen.name} (${screen.path}) answered HTTP ${probe.status}; the test account must be able to read it`);
    }
  }
  return { token: res.json('accessToken') };
}

export default function (data) {
  const params = { headers: { Authorization: `Bearer ${data.token}` }, tags: { kind: 'api' } };

  group('open the application', () => {
    const page = http.get(`${BASE_URL}/`, { tags: { kind: 'static' } });
    check(page, { 'home page served': (r) => r.status === 200 });
  });

  group('browse', () => {
    for (let i = 0; i < 4; i++) {
      const screen = WEIGHTED[Math.floor(Math.random() * WEIGHTED.length)];
      const res = http.get(`${BASE_URL}${screen.path}`, { ...params, tags: { ...params.tags, screen: screen.name } });
      if (res.status === 429) {
        throttled.add(1);
      }
      check(res, {
        [`${screen.name}: 200`]: (r) => r.status === 200,
        [`${screen.name}: JSON body`]: (r) => String(r.headers['Content-Type'] || '').includes('application/json'),
      });
      sleep(0.5 + Math.random()); // think time
    }
  });
}

export function handleSummary(data) {
  const metric = (name, stat) => {
    const m = data.metrics[name];
    return m && m.values[stat] !== undefined ? m.values[stat] : NaN;
  };
  const failedThresholds = Object.entries(data.metrics)
    .filter(([, m]) => m.thresholds && Object.values(m.thresholds).some((t) => !t.ok))
    .map(([name]) => name);

  const lines = [
    '## Performance test (k6)',
    '',
    `Target: \`${BASE_URL}\`, ${VUS} virtual users for ${DURATION}`,
    '',
    '| Measure | Value | Limit |',
    '| :--- | ---: | ---: |',
    `| Requests | ${metric('http_reqs', 'count')} (${metric('http_reqs', 'rate').toFixed(1)}/s) | |`,
    `| Failed requests | ${(metric('http_req_failed', 'rate') * 100).toFixed(2)}% | < 1% |`,
    `| Checks passed | ${(metric('checks', 'rate') * 100).toFixed(2)}% | > 99% |`,
    `| API latency p95 | ${metric('http_req_duration{kind:api}', 'p(95)').toFixed(0)} ms | < 800 ms |`,
    `| API latency p99 | ${metric('http_req_duration{kind:api}', 'p(99)').toFixed(0)} ms | < 2000 ms |`,
    `| Rate-limited (429) | ${metric('throttled_requests', 'count') || 0} | 0 |`,
    '',
    failedThresholds.length ? `**Failed:** ${failedThresholds.join(', ')}` : '**All thresholds passed.**',
    '',
  ];
  if (metric('throttled_requests', 'count') > 0) {
    lines.push('The API answered 429: raise `api_rate_limit_per_minute` in the staging Terraform variables.', '');
  }

  return {
    stdout: `${lines.join('\n')}\n`,
    'results/summary.md': lines.join('\n'),
    'results/summary.json': JSON.stringify(data, null, 1),
  };
}
