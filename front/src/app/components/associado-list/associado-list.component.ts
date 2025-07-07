import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { AssociadoService } from '../../services/associado.service';
import { Associado } from '../../models/associado.model';
import { CpfValidator } from '../../utils/cpf-validator';
import { DateUtils } from '../../utils/date-utils';

@Component({
  selector: 'app-associado-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: `./associado-list.component.html`
})
export class AssociadoListComponent implements OnInit {
  associados: Associado[] = [];
  associadoForm: FormGroup;
  loading = false;
  loadingStatus: string | null = null;
  error: string | null = null;
  success: string | null = null;
  confirmMessage = '';
  pendingAction: (() => void) | null = null;

  constructor(
    private fb: FormBuilder,
    private associadoService: AssociadoService
  ) {
    this.associadoForm = this.fb.group({
      cpf: ['', [
        Validators.required]
      ]
    });
  }

  ngOnInit() {
    this.carregarAssociados();
  }

  carregarAssociados() {
    this.associadoService.listarTodos().subscribe({
      next: (associados) => {
        this.associados = associados;
      },
      error: (err) => {
        this.error = 'Erro ao carregar associados';
        console.error(err);
      }
    });
  }

  cadastrarAssociado() {
    if (this.associadoForm.valid) {
      this.loading = true;
      this.error = null;
      this.success = null;

      const cpf = this.associadoForm.value.cpf.replace(/\D/g, ''); // Remove formatação

      this.associadoService.criar({ cpf }).subscribe({
        next: () => {
          this.success = 'Associado cadastrado com sucesso';
          this.associadoForm.reset();
          this.carregarAssociados();
          this.loading = false;
        },
        error: (err) => {
          if (err.status === 409) {
            this.error = 'Já existe um associado com este CPF';
          } else if (err.status === 400) {
            this.error = 'CPF inválido';
          } else {
            this.error = 'Erro ao cadastrar associado';
          }
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  verificarStatusCpf(cpf: string) {
    const associado = this.associados.find(a => a.cpf === cpf);
    if (!associado) return;

    this.loadingStatus = associado.id;
    this.error = null;
    this.success = null;

    this.associadoService.verificarStatusCpf(cpf).subscribe({
      next: (status) => {
        const message = status.status === 'ABLE_TO_VOTE'
          ? 'CPF habilitado para votar'
          : 'CPF não habilitado para votar';
        this.success = message;
        this.loadingStatus = null;
      },
      error: (err) => {
        const message = err.status === 'ABLE_TO_VOTE'
          ? 'CPF habilitado para votar'
          : 'CPF não habilitado para votar';
        this.error = message || 'Erro ao verificar status do CPF';
        this.loadingStatus = null;
        console.error(err);
      }
    });
  }

  alterarStatus(associado: Associado) {
    this.confirmMessage = `Confirma ${associado.ativo ? 'inativação' : 'ativação'} do associado ${associado.cpf}?`;
    this.pendingAction = () => {
      this.loadingStatus = associado.id;
      this.error = null;
      this.success = null;

      this.associadoService.alterarStatus(associado.id, !associado.ativo).subscribe({
        next: () => {
          this.success = `Associado ${!associado.ativo ? 'ativado' : 'inativado'} com sucesso`;
          this.carregarAssociados();
          this.loadingStatus = null;
        },
        error: (err) => {
          this.error = 'Erro ao alterar status do associado';
          this.loadingStatus = null;
          console.error(err);
        }
      });
    };
    // Aqui você pode usar uma biblioteca de modal ou implementar um modal personalizado
  }

  excluirAssociado(associado: Associado) {
    this.confirmMessage = `Confirma exclusão do associado ${associado.cpf}?`;
    this.pendingAction = () => {
      this.loadingStatus = associado.id;
      this.error = null;
      this.success = null;

      this.associadoService.excluir(associado.id).subscribe({
        next: () => {
          this.success = 'Associado excluído com sucesso';
          this.carregarAssociados();
          this.loadingStatus = null;
        },
        error: (err) => {
          this.error = 'Erro ao excluir associado';
          this.loadingStatus = null;
          console.error(err);
        }
      });
    };
  }

  confirmarAcao() {
    if (this.pendingAction) {
      this.pendingAction();
      this.pendingAction = null;
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.associadoForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  formatarCpf(event: any) {
    let value = event.target.value.replace(/\D/g, '');

    if (value.length > 11) {
      value = value.substring(0, 11);
    }

    if (value.length > 9) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
      value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    } else if (value.length > 3) {
      value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
    }

    event.target.value = value;
    this.associadoForm.get('cpf')?.setValue(value);
  }

  formatDate(dateString: string): string {
    return DateUtils.formatDate(dateString);
  }
}
