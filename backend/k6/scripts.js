import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 20, 
  duration: '30s',
};

const BASE_URL = 'http://localhost:8080/api/v1'; // ajuste pra sua URL real

// Função para gerar CPF randomizado válido (simplificado)
function gerarCpfValido() {
    function calcularDigito(cpf, pesoInicial) {
      let soma = 0;
      for (let i = 0; i < cpf.length; i++) {
        soma += cpf[i] * (pesoInicial - i);
      }
      let resto = soma % 11;
      return resto < 2 ? 0 : 11 - resto;
    }
  
    let cpf = [];
    // Gera os 9 primeiros dígitos aleatórios
    for (let i = 0; i < 9; i++) {
      cpf.push(Math.floor(Math.random() * 10));
    }
    // Calcula o primeiro dígito verificador
    cpf.push(calcularDigito(cpf, 10));
    // Calcula o segundo dígito verificador
    cpf.push(calcularDigito(cpf, 11));
  
    return cpf.join('');
  }
  

export default function () {
  // 1) Criar pauta (POST /pautas)
  let pautaPayload = JSON.stringify({
    titulo: `Pauta teste k6 ${__VU}-${__ITER}`, // variando titulo por usuário e iteração
    descricao: 'Descrição da pauta para teste de votação'
  });
  let pautaRes = http.post(`${BASE_URL}/pautas`, pautaPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(pautaRes, { 'criou pauta': (r) => r.status === 200 || r.status === 201 });

  let pautaId = pautaRes.json('id');
  if (!pautaId) {
    console.error('Não recebeu id da pauta');
    return;
  }

  // 2) Abrir sessão da pauta (POST /pautas/{id}/sessao?duracaoMinutos=5)
  let sessaoRes = http.post(
    `${BASE_URL}/pautas/${pautaId}/sessao?duracaoMinutos=5`,
    null,
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(sessaoRes, { 'abriu sessao': (r) => r.status === 200 || r.status === 201 });

  // 3) Criar associado (POST /associados) com CPF random
  let associadoPayload = JSON.stringify({
    cpf: gerarCpfValido(),
  });
  let associadoRes = http.post(`${BASE_URL}/associados`, associadoPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(associadoRes, { 'criou associado': (r) => r.status === 200 || r.status === 201 });

  let associadoId = associadoRes.json('id');
  if (!associadoId) {
    console.error('Não recebeu id do associado');
    return;
  }

  // 4) Votar na pauta (POST /pautas/{pautaId}/votos)
  let votoPayload = JSON.stringify({
    associadoId: associadoId,
    voto: 'SIM', // ou 'NAO'
  });
  let votoRes = http.post(`${BASE_URL}/pautas/${pautaId}/votos`, votoPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(votoRes, { 'voto registrado': (r) => r.status === 200 || r.status === 201 });

  sleep(1); // espera 1s antes da próxima iteração do VU
}
