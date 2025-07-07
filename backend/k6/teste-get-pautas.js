import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1000,      
  duration: '20s', 
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
  let res = http.get(`${BASE_URL}/pautas?page=0&size=10`);

  check(res, {
    'status 200': (r) => r.status === 200,
    'retornou JSON': (r) => r.headers['Content-Type'].includes('application/json'),
  });

}
