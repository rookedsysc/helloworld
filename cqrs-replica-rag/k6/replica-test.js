import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1, // 가상 사용자 수
  iterations: 10, // 각 사용자당 반복 횟수
};

export default function () {
  // POST 요청으로 게시물 생성
  const postRes = http.post('http://localhost:8080/posts?title=%ED%95%98%ED%95%98%EC%9B%83%EC%A7%80&content=%ED%95%98%ED%95%98%EC%9B%83%EC%A7%80', null, {
    headers: { 'accept': '*/*' },
  });

  // POST 요청이 성공했는지 확인
  check(postRes, {
    'POST status is 201': (r) => r.status === 201,
  });

  let postCount = 0;
  if (postRes.status === 200) {
    const postResponse = JSON.parse(postRes.body);
    postCount = postResponse.count;
  }

  // GET 요청으로 게시물 수 확인
  const getRes = http.get('http://localhost:8080/posts/count', {
    headers: { 'accept': '*/*' },
  });

  // GET 요청이 성공했는지 확인
  check(getRes, {
    'GET status is 200': (r) => r.status === 200,
  });

  if (getRes.status === 200) {
    const count = parseInt(getRes.body, 10);
    if(count !== postCount) {
        console.log(`Count mismatch: ${count} !== ${postCount}`);
    }
    check(count, {
      'Count matches POST response count': (c) => c === postCount,
    });
  }
}
