import http from 'k6/http';
import { check } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const RUN_ID = __ENV.RUN_ID || Date.now().toString();

const headers = {
  'Content-Type': 'application/json',
};

export const options = {
  scenarios: {
    same_key_upserts: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '10s',
      preAllocatedVUs: 10,
      maxVUs: 50,
      exec: 'sameKeyUpserts',
    },
    different_key_upserts: {
      executor: 'constant-arrival-rate',
      rate: 50,
      timeUnit: '1s',
      duration: '10s',
      preAllocatedVUs: 20,
      maxVUs: 100,
      exec: 'differentKeyUpserts',
    },
    mixed_read_write: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      stages: [
        { duration: '10s', target: 50 },
        { duration: '10s', target: 50 },
        { duration: '5s', target: 0 },
      ],
      preAllocatedVUs: 20,
      maxVUs: 100,
      exec: 'mixedReadWrite',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export function setup() {
  return {
    baseUrl: BASE_URL,
    runId: RUN_ID,
    sameKey: `k6_same_key_${RUN_ID}`,
    mixedKey: `k6_mixed_key_${RUN_ID}`,
    diffKeyPrefix: `k6_diff_key_${RUN_ID}_`,
  };
}

export function sameKeyUpserts(data) {
  const uniqueValue = `same_${__VU}_${__ITER}`;
  const res = http.post(
    `${data.baseUrl}/object`,
    JSON.stringify({ [data.sameKey]: uniqueValue }),
    { headers }
  );

  check(res, {
    'same-key POST status is 200': (r) => r.status === 200,
    'same-key POST response code is SUCCESS': (r) => {
      if (r.status !== 200) return false;
      const body = JSON.parse(r.body);
      return body.code === 'SUCCESS';
    },
  });
}

export function differentKeyUpserts(data) {
  const key = `${data.diffKeyPrefix}${__VU}_${__ITER}`;
  const value = `diff_${__VU}_${__ITER}`;
  const res = http.post(
    `${data.baseUrl}/object`,
    JSON.stringify({ [key]: value }),
    { headers }
  );

  check(res, {
    'diff-key POST status is 200': (r) => r.status === 200,
    'diff-key POST response code is SUCCESS': (r) => {
      if (r.status !== 200) return false;
      const body = JSON.parse(r.body);
      return body.code === 'SUCCESS';
    },
  });
}

export function mixedReadWrite(data) {
  const action = randomIntBetween(1, 2);

  if (action === 1) {
    const value = `mixed_write_${__VU}_${Date.now()}`;
    const res = http.post(
      `${data.baseUrl}/object`,
      JSON.stringify({ [data.mixedKey]: value }),
      { headers }
    );
    check(res, {
      'mixed-write POST status is 200': (r) => r.status === 200,
    });
  } else {
    const res = http.get(`${data.baseUrl}/object/${data.mixedKey}`);
    check(res, {
      'mixed-read GET status is 200': (r) => r.status === 200,
    });
  }
}

export function teardown(data) {
  const sameKeyRes = http.get(`${data.baseUrl}/object/${data.sameKey}`);
  check(sameKeyRes, {
    'teardown: same key is readable': (r) => r.status === 200,
  });

  const mixedKeyRes = http.get(`${data.baseUrl}/object/${data.mixedKey}`);
  check(mixedKeyRes, {
    'teardown: mixed key is readable': (r) => r.status === 200,
  });
}
