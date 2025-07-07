# Frontend Angular - Sistema de Votação

Este é o frontend Angular para o Sistema de Votação, desenvolvido como parte do desafio de desenvolvimento.

## 🚀 Funcionalidades

### Gestão de Pautas
- ✅ Listar todas as pautas cadastradas
- ✅ Criar novas pautas
- ✅ Visualizar detalhes de uma pauta
- ✅ Abrir sessões de votação
- ✅ Acompanhar status das sessões (aberta/fechada)

### Gestão de Associados
- ✅ Listar associados cadastrados
- ✅ Cadastrar novos associados
- ✅ Validar CPF
- ✅ Ativar/inativar associados
- ✅ Verificar status de votação do CPF
- ✅ Excluir associados

### Sistema de Votação
- ✅ Votar em pautas com sessão aberta
- ✅ Validação de associado habilitado
- ✅ Prevenção de voto duplicado
- ✅ Contabilização de votos em tempo real
- ✅ Visualização de resultados

### Dashboard
- ✅ Visão geral do sistema
- ✅ Estatísticas gerais
- ✅ Ações rápidas
- ✅ Pautas recentes
- ✅ Sessões em andamento

## 🏗️ Arquitetura

### Estrutura de Componentes
```
src/app/
├── components/
│   ├── dashboard/          # Página inicial com visão geral
│   ├── navbar/             # Navegação principal
│   ├── pauta-list/         # Lista de pautas
│   ├── pauta-form/         # Formulário para criar pautas
│   ├── pauta-detail/       # Detalhes e resultados da pauta
│   ├── voting/             # Interface de votação
│   └── associado-list/     # Gerenciamento de associados
├── models/                 # Interfaces TypeScript
│   ├── associado.model.ts
│   ├── pauta.model.ts
│   └── voto.model.ts
└── services/               # Serviços para comunicação com API
    ├── associado.service.ts
    ├── pauta.service.ts
    └── voto.service.ts
```

### Tecnologias Utilizadas
- **Angular 19** - Framework principal
- **Bootstrap 5** - Framework CSS para UI responsiva
- **TypeScript** - Tipagem estática
- **RxJS** - Programação reativa
- **Angular Router** - Roteamento
- **Angular Forms** - Formulários reativos
- **HttpClient** - Comunicação com API REST

## 🛠️ Configuração e Execução

### Pré-requisitos
- Node.js 18+ ou 20+ (versões LTS recomendadas)
- npm ou yarn
- Backend da API rodando em http://localhost:8080

### Instalação
```bash
# Clonar o repositório
git clone <repository-url>
cd votosangular

# Instalar dependências
npm install --legacy-peer-deps

# Iniciar servidor de desenvolvimento
npm run start
```

A aplicação estará disponível em: `http://localhost:4200`

### Scripts Disponíveis
```bash
npm run start          # Inicia servidor de desenvolvimento
npm run build      # Build para produção
npm run test       # Executa testes
npm run watch      # Build em modo watch
```

## 🎨 Interface do Usuário

### Design System
- **Cores**: Bootstrap color palette
- **Tipografia**: Inter font family
- **Componentes**: Bootstrap components
- **Responsividade**: Mobile-first design
- **Iconografia**: Bootstrap Icons

### Funcionalidades da Interface

#### Dashboard
- Cards com estatísticas do sistema
- Lista de pautas recentes
- Ações rápidas
- Indicadores de sessões ativas

#### Gestão de Pautas
- Grid responsivo de cards
- Badges de status (sessão aberta/fechada)
- Botões contextuais baseados no estado
- Modal de resultados em tempo real

#### Sistema de Votação
- Interface intuitiva com botões grandes
- Validação em tempo real
- Feedback visual de confirmação
- Prevenção de erros de usuário

#### Gestão de Associados
- Tabela responsiva
- Formulário inline para cadastro
- Ações em grupo (ativar/inativar/excluir)
- Validação de CPF

## 🔌 Integração com Backend

### Endpoints Utilizados
```typescript
// Pautas
GET    /api/v1/pautas                    # Listar pautas
POST   /api/v1/pautas                    # Criar pauta
GET    /api/v1/pautas/{id}               # Buscar pauta
POST   /api/v1/pautas/{id}/sessao        # Abrir sessão

// Associados
GET    /api/v1/associados                # Listar associados
POST   /api/v1/associados                # Criar associado
PATCH  /api/v1/associados/{id}/status    # Alterar status
DELETE /api/v1/associados/{id}           # Excluir associado
GET    /api/v1/associados/cpf/{cpf}/status # Verificar CPF

// Votos
POST   /api/v1/pautas/{id}/votos         # Registrar voto
GET    /api/v1/pautas/{id}/votos/resultado # Obter resultado
```

### Tratamento de Erros
- Interceptação de erros HTTP
- Mensagens de feedback para o usuário
- Validação de formulários
- Estados de loading

## 📱 Responsividade

A aplicação é totalmente responsiva e funciona em:
- 📱 Mobile (320px+)
- 📟 Tablet (768px+)
- 💻 Desktop (1024px+)
- 🖥️ Large screens (1200px+)

## 🔒 Validações

### Formulários
- **CPF**: Formato e dígitos verificadores
- **Pautas**: Título e descrição obrigatórios
- **Votação**: Seleção de associado e voto obrigatórios

### Regras de Negócio
- Associado só pode votar uma vez por pauta
- Apenas associados ativos podem votar
- Votação só permitida em sessões abertas
- CPF único por associado

## 🚀 Deploy para Produção

### Build
```bash
npm run build
```

### Configuração de Ambiente
Criar arquivo `src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://sua-api-producao.com/api/v1'
};
```

## 📋 Funcionalidades Futuras

### Melhorias Planejadas
- [ ] Autenticação e autorização
- [ ] Notificações em tempo real (WebSocket)
- [ ] Relatórios e analytics
- [ ] Histórico de votações
- [ ] Configurações de sistema
- [ ] Temas personalizáveis
- [ ] PWA (Progressive Web App)
- [ ] Testes E2E

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👥 Autores

- **Desenvolvedor Frontend** - Implementação Angular

---

**Sistema de Votação** - Uma solução completa para gerenciamento de assembleias e votações cooperativistas.
