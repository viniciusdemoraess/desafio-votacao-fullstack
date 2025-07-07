import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PautaService } from '../../services/pauta.service';

@Component({
  selector: 'app-pauta-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: `./pauta-form.component.html`
})
export class PautaFormComponent {
  pautaForm: FormGroup;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private pautaService: PautaService,
    private router: Router
  ) {
    this.pautaForm = this.fb.group({
      titulo: ['', [Validators.required, Validators.minLength(3)]],
      descricao: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit() {
    if (this.pautaForm.valid) {
      this.loading = true;
      this.error = null;

      this.pautaService.criar(this.pautaForm.value).subscribe({
        next: () => {
          this.router.navigate(['/pautas']);
        },
        error: (err) => {
          this.error = 'Erro ao criar pauta';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.pautaForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}
