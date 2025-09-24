import http from 'k6/http';

export let options = {
  vus: 1000,
  duration: '1s',
};

export default function() {
  const payload = JSON.stringify({
    orderId: 1
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'accept': '*/*',
    },
  };

  http.post('http://localhost:9780/order/place', payload, params);
}