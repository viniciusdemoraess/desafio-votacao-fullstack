import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PautaService } from '../../services/pauta.service';
import { VotoService } from '../../services/voto.service';
import { AssociadoService } from '../../services/associado.service';
import { Pauta } from '../../models/pauta.model';
import { OpcaoVoto } from '../../models/voto.model';
import { Associado } from '../../models/associado.model';

@Component({
  selector: 'app-voting',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: `./voting.component.html`
})
export class VotingComponent implements OnInit {
  pauta: Pauta | null = null;
  associados: Associado[] = [];
  votoForm: FormGroup;
  loading = false;
  votoConfirmado = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private pautaService: PautaService,
    private votoService: VotoService,
    private associadoService: AssociadoService
  ) {
    this.votoForm = this.fb.group({
      associadoId: ['', Validators.required],
      voto: ['', Validators.required]
    });
  }

  ngOnInit() {
    const pautaId = this.route.snapshot.paramMap.get('id');
    if (pautaId) {
      this.carregarPauta(pautaId);
      this.carregarAssociados();
    }
  }

  carregarPauta(id: string) {
    this.pautaService.buscarPorId(id).subscribe({
      next: (pauta) => {
        this.pauta = pauta;
      },
      error: (err) => {
        this.error = 'Erro ao carregar pauta';
        console.error(err);
      }
    });
  }

  carregarAssociados() {
    this.associadoService.listarTodos().subscribe({
      next: (associados) => {
        this.associados = associados.filter(a => a.ativo);
      },
      error: (err) => {
        this.error = 'Erro ao carregar associados';
        console.error(err);
      }
    });
  }

  selecionarVoto(opcao: string) {
    this.votoForm.patchValue({ voto: opcao });
  }

  onSubmit() {
    if (this.votoForm.valid && this.pauta) {
      this.loading = true;
      this.error = null;

      const votoData = {
        associadoId: this.votoForm.value.associadoId,
        voto: this.votoForm.value.voto as OpcaoVoto
      };

      console.log('Enviando voto:', {
        pautaId: this.pauta.id,
        votoData: votoData,
        url: `${this.votoService['baseUrl']}/${this.pauta.id}/votos`
      });

      this.votoService.registrarVoto(this.pauta.id, votoData).subscribe({
        next: (response) => {
          console.log('Voto registrado com sucesso:', response);
          this.votoConfirmado = true;
          this.loading = false;

          // Redirecionar após 2 segundos
          setTimeout(() => {
            this.router.navigate(['/pautas', this.pauta?.id]);
          }, 2000);
        },
        error: (err) => {
          console.error('Erro completo ao registrar voto:', err);
          this.loading = false;

          if (err.status === 409) {
            this.error = 'Este associado já votou nesta pauta';
          } else if (err.status === 403) {
            this.error = 'Associado não está habilitado para votar';
          } else if (err.status === 400) {
            this.error = 'Sessão de votação está fechada ou dados inválidos';
          } else {
            const message = err?.error?.status === 'UNABLE_TO_VOTE' ? 'CPF não habilitado para votar' : err.status;
            this.error = `${message}`;
          }
        }
      });
    } else {
      console.log('Formulário inválido:', {
        formValid: this.votoForm.valid,
        formValue: this.votoForm.value,
        formErrors: this.votoForm.errors
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.votoForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}
