import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // Aumenta para 50 usuários em 30s
    { duration: '1m', target: 100 }, // Mantém 100 usuários por 1 min
    { duration: '20s', target: 0 },  // Reduz para 0
  ],
};

export default function() {
  const pautaId = 'sua-pauta-id';
  
  // Gera um CPF aleatório a cada requisição
  const cpf = Math.floor(10000000000 + Math.random() * 90000000000).toString();
  
  // Voto aleatório (sim ou não)
  const voto = Math.random() > 0.5;
  
  const payload = JSON.stringify({
    associadoId: cpf,
    voto: voto
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`http://localhost:8080/api/v1/pautas/${pautaId}/votos`, payload, params);
  
  check(res, {
    'status is 201': (r) => r.status === 201 || r.status === 400 || r.status === 403 || r.status === 409,
  });
  
  sleep(0.1);
}