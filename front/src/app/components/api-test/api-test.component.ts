import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { VotoService } from '../../services/voto.service';
import { AssociadoService } from '../../services/associado.service';
import { PautaService } from '../../services/pauta.service';
import { generateValidCpf } from '../../utils/cpf-validator';

@Component({
  selector: 'app-api-test',
  standalone: true,
  imports: [CommonModule],
  templateUrl: `./api-test.component.html`
})
export class ApiTestComponent {
  apiUrl = 'http://localhost:8080/api/v1';
  statusConexao = 'Desconhecido';

  votoResponse: any = null;
  votoError: any = null;
  pautasResponse: any = null;

  testAndoCompleto = false;
  logTeste: Array<{step: string, message: string, status: 'info' | 'success' | 'error', data?: any}> = [];

  associadoCriado: any = null;
  pautaCriada: any = null;
  sessaoAberta: any = null;
  votoCriado: any = null;

  constructor(
    private http: HttpClient,
    private votoService: VotoService,
    private associadoService: AssociadoService,
    private pautaService: PautaService
  ) {
    this.verificarConexao();
  }

  async executarTesteCompleto() {
    this.testAndoCompleto = true;
    this.logTeste = [];
    this.limparDados();

    try {
      await this.verificarConexaoAsync();

      await this.criarAssociadoTeste();

      await this.verificarAssociadoCriado();

      await this.criarPautaTeste();

      await this.verificarPautaCriada();

      await this.abrirSessaoTeste();

      await this.registrarVotoTeste();

      this.adicionarLog('🎉 TESTE COMPLETO', 'Todos os testes foram executados com sucesso!', 'success');

    } catch (error) {
      this.adicionarLog('❌ ERRO', 'Teste falhou: ' + (error as any).message, 'error', error);
    } finally {
      this.testAndoCompleto = false;
    }
  }


  private async verificarConexaoAsync(): Promise<void> {
    this.adicionarLog('🔍 Passo 1', 'Verificando conexão com API...', 'info');

    return new Promise((resolve, reject) => {
      this.http.get(`${this.apiUrl}/pautas`).subscribe({
        next: () => {
          this.statusConexao = 'Conectado';
          this.adicionarLog('✅ Passo 1', 'Conexão com API estabelecida', 'success');
          resolve();
        },
        error: (error) => {
          this.statusConexao = 'Erro de conexão';
          this.adicionarLog('❌ Passo 1', 'Falha na conexão com API', 'error', error);
          reject(new Error('Falha na conexão com API'));
        }
      });
    });
  }

  private async criarAssociadoTeste(): Promise<void> {
    this.adicionarLog('👤 Passo 2', 'Criando associado com CPF válido...', 'info');

    const cpfTeste = this.gerarCpfValido();

    return new Promise((resolve, reject) => {
      this.associadoService.criar({ cpf: cpfTeste }).subscribe({
        next: (associado) => {
          this.associadoCriado = associado;
          this.adicionarLog('✅ Passo 2', `Associado criado com sucesso. ID: ${associado.id}`, 'success', associado);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 2', 'Falha ao criar associado', 'error', error);
          reject(new Error('Falha ao criar associado'));
        }
      });
    });
  }

  private async verificarAssociadoCriado(): Promise<void> {
    this.adicionarLog('🔍 Passo 3', 'Verificando se associado foi criado...', 'info');

    return new Promise((resolve, reject) => {
      this.associadoService.buscarPorId(this.associadoCriado.id).subscribe({
        next: (associado) => {
          this.adicionarLog('✅ Passo 3', 'Associado encontrado com sucesso', 'success', associado);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 3', 'Associado não encontrado', 'error', error);
          reject(new Error('Associado não encontrado'));
        }
      });
    });
  }

  private async criarPautaTeste(): Promise<void> {
    this.adicionarLog('📋 Passo 4', 'Criando nova pauta...', 'info');

    const pautaData = {
      titulo: `Pauta de Teste - ${new Date().toLocaleString()}`,
      descricao: 'Esta é uma pauta criada automaticamente para teste do sistema de votação.'
    };

    return new Promise((resolve, reject) => {
      this.pautaService.criar(pautaData).subscribe({
        next: (pauta) => {
          this.pautaCriada = pauta;
          this.adicionarLog('✅ Passo 4', `Pauta criada com sucesso. ID: ${pauta.id}`, 'success', pauta);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 4', 'Falha ao criar pauta', 'error', error);
          reject(new Error('Falha ao criar pauta'));
        }
      });
    });
  }

  private async verificarPautaCriada(): Promise<void> {
    this.adicionarLog('🔍 Passo 5', 'Verificando se pauta foi criada...', 'info');

    return new Promise((resolve, reject) => {
      this.pautaService.buscarPorId(this.pautaCriada.id).subscribe({
        next: (pauta) => {
          this.adicionarLog('✅ Passo 5', 'Pauta encontrada com sucesso', 'success', pauta);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 5', 'Pauta não encontrada', 'error', error);
          reject(new Error('Pauta não encontrada'));
        }
      });
    });
  }

  private async abrirSessaoTeste(): Promise<void> {
    this.adicionarLog('🚀 Passo 6', 'Solicitando duração da sessão...', 'info');

    const duracaoStr = prompt(
      '⏰ Por quantos minutos a sessão de votação deve ficar aberta?\n\n' +
      '(Digite um número entre 1 e 60 minutos)',
      '5'
    );

    if (duracaoStr === null) {
      throw new Error('Usuário cancelou a operação');
    }

    const duracao = parseInt(duracaoStr);
    if (isNaN(duracao) || duracao < 1 || duracao > 60) {
      throw new Error('Duração inválida. Digite um número entre 1 e 60 minutos');
    }

    this.adicionarLog('⏰ Passo 6', `Abrindo sessão de votação por ${duracao} minutos...`, 'info');

    return new Promise((resolve, reject) => {
      this.pautaService.abrirSessaoVotacao(this.pautaCriada.id, duracao).subscribe({
        next: (pauta) => {
          this.sessaoAberta = pauta;
          this.adicionarLog('✅ Passo 6', `Sessão de votação aberta com sucesso por ${duracao} minutos`, 'success', pauta);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 6', 'Falha ao abrir sessão', 'error', error);
          reject(new Error('Falha ao abrir sessão'));
        }
      });
    });
  }

  private async registrarVotoTeste(): Promise<void> {
    this.adicionarLog('🗳️ Passo 7', 'Registrando voto...', 'info');

    const votoData = {
      associadoId: this.associadoCriado.id,
      voto: 'SIM' as any
    };

    return new Promise((resolve, reject) => {
      this.votoService.registrarVoto(this.pautaCriada.id, votoData).subscribe({
        next: (voto) => {
          this.votoCriado = voto;
          this.adicionarLog('✅ Passo 7', 'Voto registrado com sucesso!', 'success', voto);
          resolve();
        },
        error: (error) => {
          this.adicionarLog('❌ Passo 7', 'Falha ao registrar voto', 'error', error);
          reject(new Error('Falha ao registrar voto: ' + (error.message || JSON.stringify(error))));
        }
      });
    });
  }


  private adicionarLog(step: string, message: string, status: 'info' | 'success' | 'error', data?: any) {
    this.logTeste.push({ step, message, status, data });
    console.log(`[${step}] ${message}`, data || '');
  }

  private limparDados() {
    this.associadoCriado = null;
    this.pautaCriada = null;
    this.sessaoAberta = null;
    this.votoCriado = null;
  }

  private gerarCpfValido(): string {
    const randomNumbers = () => Math.floor(Math.random() * 9);

    const cpfBase = Array.from({ length: 9 }, randomNumbers);

    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += cpfBase[i] * (10 - i);
    }
    let remainder = 11 - (sum % 11);
    const digit1 = remainder < 2 ? 0 : remainder;

    sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += cpfBase[i] * (11 - i);
    }
    sum += digit1 * 2;
    remainder = 11 - (sum % 11);
    const digit2 = remainder < 2 ? 0 : remainder;

    const cpf = [...cpfBase, digit1, digit2].join('');
    return cpf;
  }

  private verificarConexao() {
    this.http.get(`${this.apiUrl}/pautas`).subscribe({
      next: () => this.statusConexao = 'Conectado',
      error: () => this.statusConexao = 'Erro de conexão'
    });
  }


  testarVoto() {
    this.votoResponse = null;
    this.votoError = null;

    const testData = {
      associadoId: '6866c1d301476dfeea92ba36',
      voto: 'SIM'
    };

    const testPautaId = '6867ee9d2a3949bb456ee3bb';
    const url = `${this.apiUrl}/pautas/${testPautaId}/votos`;

    console.log('Testando voto com dados reais:', {
      url: url,
      data: testData,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    this.http.post(url, testData, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).subscribe({
      next: (response) => {
        console.log('Sucesso:', response);
        this.votoResponse = response;
      },
      error: (error) => {
        console.error('Erro completo:', error);
        this.votoError = {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error,
          url: error.url,
          body: error.error
        };
      }
    });
  }

  testarVotoComService() {
    this.votoResponse = null;
    this.votoError = null;

    const testPautaId = '6867ee9d2a3949bb456ee3bb';
    const testAssociadoId = '6866c1d301476dfeea92ba36';
    const testVoto = 'SIM';

    console.log('Testando com VotoService...');

    this.votoService.testarVotoComCurl(testPautaId, testAssociadoId, testVoto).subscribe({
      next: (response) => {
        console.log('Sucesso com VotoService:', response);
        this.votoResponse = response;
      },
      error: (error) => {
        console.error('Erro com VotoService:', error);
        this.votoError = {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error,
          url: error.url,
          body: error.error
        };
      }
    });
  }

  testarListagem() {
    this.pautasResponse = null;

    const url = `${this.apiUrl}/pautas`;

    this.http.get(url).subscribe({
      next: (response) => {
        console.log('Pautas:', response);
        this.pautasResponse = response;
      },
      error: (error) => {
        console.error('Erro ao listar pautas:', error);
      }
    });
  }
}
