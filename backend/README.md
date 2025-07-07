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

### Via docker

##### Iniciar todos os serviços e ferramentas necessárias em segundo plano

```bash
docker compose up -d
```

*Importante:* O docker compose up -d não apenas sobe a aplicação, mas também inicia duas ferramentas de análise de código para ajudar na qualidade e segurança do projeto:

*PMD:* Ferramenta de análise estática de código Java que detecta potenciais bugs, más práticas e problemas de estilo no código-fonte.

*Semgrep:* Ferramenta de análise estática flexível que permite detectar vulnerabilidades, erros comuns e padrões de código indesejados, suportando várias linguagens.

Os containers vão iniciar, mas como o pmd e o semgrep estão configurados com restart: "no" e só rodam o comando de análise uma vez, eles vão subir, executar a análise e sair imediatamente.

Para ver o resultado da execução:

- Listar containers, incluindo os que já pararam

```bash
docker ps -a
```
- Procure os containers com nomes pmd e semgrep (ou IDs correspondentes).

- Ver logs do container PMD

```bash
docker logs pmd
```

- Vai mostrar o relatório de análise estática gerado pelo PMD, normalmente listando problemas, más práticas, etc.

- Ver logs do container Semgrep

```bash
docker logs semgrep
```

- Vai mostrar as vulnerabilidades ou padrões identificados pelo Semgrep conforme a configuração --config=auto.

#### Resumo prático:

```bash
docker compose up -d

docker logs pmd

docker logs semgrep
```

- Se quiser reexecutar as análises, rode:

```bash
docker compose up pmd
docker compose up semgrep
```

ou ainda:

```bash
docker start -a pmd
docker start -a semgrep
```

### Manualmente

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


### Volume do MongoDB no Docker Compose

No `docker-compose.yml` deste projeto, o serviço do MongoDB foi configurado com um volume nomeado chamado `mongodb_data`. Veja o trecho correspondente:

```yaml
services:
  mongodb:
    image: mongo
    container_name: mongodb
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - backend
    healthcheck:
      test: echo 'db.runCommand("ping").ok' | mongosh localhost:27017/votacao-db --quiet
      interval: 10s
      timeout: 10s
      retries: 5
      start_period: 40s

volumes:
  mongodb_data:
```

> Os dados do MongoDB são persistidos automaticamente graças ao volume nomeado `mongodb_data` configurado no `docker-compose.yml`, garantindo que todas as informações sejam salvas no disco do host mesmo após reinicializações ou remoção dos containers, preservando assim o banco de dados do sistema de votação.

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
  vus: 1000,      // 1000 usuários virtuais simultâneos
  duration: '20s', // duração total do teste
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
  let res = http.get(`${BASE_URL}/pautas?page=0&size=10`);

  check(res, {
    'status 200': (r) => r.status === 200,
    'retornou JSON': (r) => r.headers['Content-Type'].includes('application/json'),
  });

  // Pausa opcional para simular tempo entre requisições
  sleep(1);
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
