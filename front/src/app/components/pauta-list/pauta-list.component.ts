import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PautaService } from '../../services/pauta.service';
import { Pauta } from '../../models/pauta.model';

@Component({
  selector: 'app-pauta-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  styles: [`./pauta-list.scss`],
  templateUrl: `./pauta-list.component.html`
})
export class PautaListComponent implements OnInit {
  pautas: Pauta[] = [];
  loading = false;
  error: string | null = null;

  // Propriedades para o modal de duração
  duracaoSelecionada: number = 1;
  pautaIdSelecionada: string = '';
  pautaSelecionada: Pauta | null = null;
  mostrandoModal: boolean = false;

  constructor(private pautaService: PautaService) {}

  ngOnInit() {
    this.carregarPautas();
  }

  carregarPautas() {
    this.loading = true;
    this.error = null;

    this.pautaService.listarTodas().subscribe({
      next: (pautas) => {
        this.pautas = pautas;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar pautas';
        this.loading = false;
        console.error(err);
      }
    });
  }

  abrirSessao(pautaId: string) {
    this.loading = true;
    this.error = null;

    this.pautaService.abrirSessaoVotacao(pautaId, this.duracaoSelecionada).subscribe({
      next: () => {
        this.carregarPautas();
      },
      error: (err) => {
        this.error = 'Erro ao abrir sessão de votação';
        this.loading = false;
        console.error(err);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  mostrarModalDuracao(pautaId: string) {
    this.pautaIdSelecionada = pautaId;
    this.duracaoSelecionada = 1; // Reset para valor padrão

    // Buscar dados da pauta selecionada
    this.pautaSelecionada = this.pautas.find(p => p.id === pautaId) || null;

    this.mostrandoModal = true;
  }

  fecharModal() {
    this.mostrandoModal = false;
  }

  confirmarAberturaSessao() {
    if (!this.duracaoSelecionada || this.duracaoSelecionada < 1 || this.duracaoSelecionada > 60) {
      alert('❌ Duração inválida. Digite um número entre 1 e 60 minutos.');
      return;
    }

    // Mostrar confirmação final com resumo
    const pautaInfo = this.pautaSelecionada ? this.pautaSelecionada.titulo : 'Pauta selecionada';
    const confirmacao = confirm(
      `🗳️ Confirmar abertura de sessão?\n\n` +
      `📋 Pauta: ${pautaInfo}\n` +
      `⏰ Duração: ${this.duracaoSelecionada} minutos\n\n` +
      `A sessão ficará aberta para votação durante o tempo especificado.`
    );

    if (!confirmacao) {
      return; // Usuário cancelou
    }

    // Fechar modal
    this.fecharModal();

    // Executar abertura da sessão
    this.abrirSessao(this.pautaIdSelecionada);
  }
}
