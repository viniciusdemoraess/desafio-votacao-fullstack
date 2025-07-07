import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PautaService } from '../../services/pauta.service';
import { VotoService } from '../../services/voto.service';
import { Pauta } from '../../models/pauta.model';
import { ResultadoVotacao } from '../../models/voto.model';
import { Subscription, switchMap, timer } from 'rxjs';

@Component({
  selector: 'app-pauta-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: `./pauta-detail.component.html`
})
export class PautaDetailComponent implements OnInit, OnDestroy {
  pauta: Pauta | null = null;
  resultado: ResultadoVotacao | null = null;
  loading = false;
  loadingResultado = false;
  error: string | null = null;
  private pollingSub!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private pautaService: PautaService,
    private votoService: VotoService
  ) {}

  polling(){
    const pautaId = this.route.snapshot.paramMap.get('id') || '';

    this.pollingSub = timer(0, 30000).pipe(
      switchMap(() => {
        console.log('⏰ Fazendo polling...');
        return this.pautaService.verificarSessaoAberta(pautaId);
      })
    ).subscribe({
      next: (res) => {
        console.log('Resposta polling:', res);

        if (this.pauta)
          this.pauta.sessaoAberta = res;

      },
      error: err => {
        console.error('Erro ao verificar status da sessão:', err);
      }
    });
  }


  ngOnInit() {
    const pautaId = this.route.snapshot.paramMap.get('id');
    if (pautaId) {
      this.carregarPauta(pautaId);
      this.carregarResultado();
    }
    this.polling();
  }

  ngOnDestroy() {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
      console.log('✅ Polling parado ao sair do componente.');
    }
  }

  carregarPauta(id: string) {
    this.loading = true;
    this.error = null;

    this.pautaService.buscarPorId(id).subscribe({
      next: (pauta) => {
        this.pauta = pauta;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar pauta';
        this.loading = false;
        console.error(err);
      }
    });
  }

  carregarResultado() {
    if (!this.pauta) return;

    this.loadingResultado = true;

    this.votoService.obterResultado(this.pauta.id).subscribe({
      next: (resultado) => {
        this.resultado = resultado;
        this.loadingResultado = false;
      },
      error: (err) => {
        this.loadingResultado = false;
        console.error(err);
      }
    });
  }

  abrirSessao() {
    if (!this.pauta) return;

    // Perguntar ao usuário a duração da sessão
    const duracaoStr = prompt(
      '⏰ Por quantos minutos a sessão de votação deve ficar aberta?\n\n' +
      '(Digite um número entre 1 e 60 minutos)',
      '5'
    );

    if (duracaoStr === null) {
      return; // Usuário cancelou
    }

    const duracao = parseInt(duracaoStr);
    if (isNaN(duracao) || duracao < 1 || duracao > 60) {
      alert('❌ Duração inválida. Digite um número entre 1 e 60 minutos.');
      return;
    }

    this.loading = true;
    this.error = null;

    this.pautaService.abrirSessaoVotacao(this.pauta.id, duracao).subscribe({
      next: (pauta) => {
        this.pauta = pauta;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao abrir sessão de votação';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getPercentualSim(): number {
    if (!this.resultado || this.resultado.total === 0) return 0;
    return (this.resultado.sim / this.resultado.total) * 100;
  }

  getPercentualNao(): number {
    if (!this.resultado || this.resultado.total === 0) return 0;
    return (this.resultado.nao / this.resultado.total) * 100;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString('pt-BR');
  }
}
