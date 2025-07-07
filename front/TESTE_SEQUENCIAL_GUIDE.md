# Guia do Teste Sequencial Completo

## 📋 Visão Geral

O componente `api-test` implementa um teste sequencial completo que executa toda a funcionalidade principal do sistema de votação em uma única sequência automatizada.

## 🚀 Como Executar o Teste

1. **Inicie o backend** (Spring Boot) na porta 8080
2. **Inicie o frontend** Angular:
   ```bash
   cd /home/vinicius/votosangular
   ng serve --port 4201
   ```
3. **Acesse** `http://localhost:4201/api-test`
4. **Clique** no botão "Iniciar Teste Completo"

## 🔄 Sequência do Teste

O teste executa automaticamente os seguintes passos:

### 1. 🔍 Verificação de Conexão
- Testa se a API está respondendo
- Endpoint: `GET /api/v1/pautas`

### 2. 👤 Criação de Associado
- Gera um CPF válido automaticamente
- Cria um novo associado
- Endpoint: `POST /api/v1/associados`

### 3. ✅ Verificação do Associado
- Confirma se o associado foi criado corretamente
- Endpoint: `GET /api/v1/associados/{id}`

### 4. 📋 Criação de Pauta
- Cria uma nova pauta com título e descrição automáticos
- Endpoint: `POST /api/v1/pautas`

### 5. ✅ Verificação da Pauta
- Confirma se a pauta foi criada corretamente
- Endpoint: `GET /api/v1/pautas/{id}`

### 6. 🚀 Abertura de Sessão
- Abre uma sessão de votação para a pauta criada
- Duração: 5 minutos
- Endpoint: `PUT /api/v1/pautas/{id}/abrir-sessao?duracaoMinutos=5`

### 7. 🗳️ Registro de Voto
- Registra um voto "SIM" do associado criado na pauta
- Endpoint: `POST /api/v1/pautas/{id}/votos`

## 📊 Resultados do Teste

### ✅ Sucesso
- Cada passo exibe um log com ✅ indicando sucesso
- Dados detalhados são mostrados no JSON de resposta
- Ao final: "🎉 TESTE COMPLETO - Todos os testes foram executados com sucesso!"

### ❌ Erro
- Passos com erro exibem ❌ com detalhes do problema
- O teste para na primeira falha
- Informações de debug são disponibilizadas

## 🔧 Funcionalidades Adicionais

### Testes Individuais
- **Teste de Voto Individual**: Testa envio de voto com IDs fixos
- **Teste de Listagem**: Lista todas as pautas existentes

### Geração de CPF
- CPF válido gerado automaticamente usando algoritmo de validação
- Cada execução cria um associado único

### Logs Detalhados
- Cada etapa é logada no console do navegador
- Interface mostra progresso em tempo real
- Dados de resposta da API são exibidos

## 🛠️ Tecnologias Utilizadas

- **Angular 17+** com Standalone Components
- **Bootstrap 5** para UI responsiva
- **HttpClient** para comunicação com API
- **Services** tipados para cada endpoint
- **Async/Await** para controle de fluxo
- **TypeScript** com tipagem completa

## 🎯 Objetivo do Teste

Este teste valida de forma automatizada:

1. **Conectividade** entre frontend e backend
2. **CRUD de Associados** (Create + Read)
3. **CRUD de Pautas** (Create + Read)
4. **Abertura de Sessões** de votação
5. **Registro de Votos** com validações
6. **Fluxo completo** end-to-end do sistema

## 📝 Observações

- O teste usa dados reais criados dinamicamente
- Cada execução cria novos registros no banco
- É recomendado limpar dados de teste periodicamente
- Logs detalhados facilitam debugging de problemas

## 🚨 Requisitos

- Backend Spring Boot rodando na porta 8080
- API endpoints implementados conforme OpenAPI spec
- Banco de dados configurado e acessível
- CORS configurado para permitir requisições do frontend
