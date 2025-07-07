import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PautaService } from '../../services/pauta.service';
import { AssociadoService } from '../../services/associado.service';
import { Pauta } from '../../models/pauta.model';
import { Associado } from '../../models/associado.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: `./dashboard.component.html`
})
export class DashboardComponent implements OnInit {
  pautasRecentes: Pauta[] = [];
  pautasComSessaoAberta: Pauta[] = [];
  associados: Associado[] = [];

  totalPautas = 0;
  pautasAbertas = 0;
  totalAssociados = 0;
  associadosAtivos = 0;

  constructor(
    private pautaService: PautaService,
    private associadoService: AssociadoService
  ) {}

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    // Carregar pautas
    this.pautaService.listarTodas().subscribe({
      next: (res) => {
        this.totalPautas = res.totalElements;
        this.pautasAbertas = res.content.filter(p => p.sessaoAberta).length;
        this.pautasRecentes = res.content
          .sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime())
          .slice(0, 5);
        this.pautasComSessaoAberta = res.content.filter(p => p.sessaoAberta);
      },
      error: (err) => console.error('Erro ao carregar pautas:', err)
    });

    // Carregar associados
    this.associadoService.listarTodos().subscribe({
      next: (res) => {
        this.associados = res.content;
        this.totalAssociados = res.totalElements;
        this.associadosAtivos = res.content.filter(a => a.ativo).length;
      },
      error: (err) => console.error('Erro ao carregar associados:', err)
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString('pt-BR');
  }
}
