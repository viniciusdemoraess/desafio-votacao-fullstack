# Sistema de Votação - Documentação

## Descrição

Sistema de votação para cooperativas, onde cada associado possui um voto e as decisões são tomadas em assembleias. O sistema permite:

- Cadastrar uma nova pauta
- Abrir uma sessão de votação em uma pauta (customizável ou 1 min por default)
- Receber votos dos associados (Sim/Não, um voto por associado por pauta)
- Contabilizar e dar o resultado da votação

## Arquitetura e Tecnologias

### Backend
- **Java 21** com **Spring Boot 3.5.3**
- **Arquitetura Reativa** usando WebFlux e MongoDB Reactive
- **MongoDB** para persistência dos dados
- **Swagger/OpenAPI** para documentação da API
- Testes unitários com JUnit e Mockito

### Decisões de Implementação

1. **Arquitetura Reativa**
   - Utilizei WebFlux para garantir alta escalabilidade, crucial para cenários com centenas de milhares de votos (tarefa bônus 2)
   - Implementação baseada em eventos, permitindo melhor performance sob carga

2. **Versionamento de API (tarefa bônus 3)**
   - Adotei versionamento via URI path (/api/v1/...)
   - Facilmente identificável pelos clientes
   - Permite evolução da API sem quebrar compatibilidade

3. **Validação de CPF (tarefa bônus 1)**
   - Cliente fake que retorna aleatoriamente se um CPF é válido
   - Integrado ao processo de votação para validar associados

4. **Tratamento de Erros**
   - Handler global de exceções para respostas consistentes
   - Registro de logs detalhados para monitoramento e debugging

5. **Segurança**
   - Conforme solicitado, a segurança foi abstraída para fins de exercício

## Estrutura do Projeto

```
/src
  /main
    /java/br/com/desafio_votacao
      /client            # Clientes para APIs externas (validação de CPF)
      /config            # Configurações (OpenAPI, Versionamento)
      /controller        # Controllers REST
      /dto               # Objetos de transferência de dados
      /exception         # Tratamento global de exceções
      /model             # Entidades do domínio
      /repository        # Acesso ao MongoDB
      /service           # Lógica de negócio
    /resources           # Configurações da aplicação
  /test                  # Testes unitários
```

## Requisitos para Execução

- Java 21
- MongoDB (instalado localmente ou via Docker)
- Maven

## Instruções para Execução

1. **Configurar MongoDB**

   Opção 1: MongoDB Local
   ```bash
   # Instalar MongoDB na máquina local (se já não estiver instalado)
   # Iniciar o serviço do MongoDB
   ```

   Opção 2: MongoDB via Docker
   ```bash
   # Executar o MongoDB via Docker
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

2. **Executar a Aplicação**

   ```bash
   # No diretório raiz do projeto
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

3. **Acessar a Documentação da API**

   Após iniciar a aplicação, acesse a documentação Swagger:
   ```
   http://localhost:8080/webjars/swagger-ui/index.html
   ```

## Testando a API

### Cadastrando uma Pauta

```bash
curl -X POST http://localhost:8080/api/v1/pautas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Aprovação de Orçamento 2026", "descricao": "Votação para aprovação do orçamento do próximo ano"}'
```

### Abrindo uma Sessão de Votação

```bash
curl -X POST "http://localhost:8080/api/v1/pautas/{PAUTA_ID}/sessao?duracaoMinutos=5"
```

### Registrando um Voto

```bash
curl -X POST http://localhost:8080/api/v1/pautas/{PAUTA_ID}/votos \
  -H "Content-Type: application/json" \
  -d '{"associadoId": "12345678901", "voto": true}'
```

### Verificando o Resultado da Votação

```bash
curl http://localhost:8080/api/v1/pautas/{PAUTA_ID}/votos/resultado
```

## Testes de Performance (Tarefa Bônus 2)

Para simular cenários com muitos votos e garantir performance, recomendo a ferramenta k6 ou Apache JMeter:

### Exemplo com k6

1. Instalar k6: https://k6.io/docs/getting-started/installation/

2. Criar script de teste load-test.js:
```javascript
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
  // Substitua pela ID de uma pauta real
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
```

3. Executar:
```bash
k6 run load-test.js
```

## Melhorias Futuras

1. Implementação de autenticação e autorização
2. Notificações em tempo real dos resultados
3. Expansão da API com mais endpoints para gestão de associados
4. Implementação de métricas e dashboards para monitoramento
