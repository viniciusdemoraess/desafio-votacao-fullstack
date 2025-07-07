import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Associado, AssociadoDTO, CpfStatus } from '../models/associado.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssociadoService {
  private readonly baseUrl = `${environment.apiUrl}/associados`;

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Associado[]> {
    return this.http.get<Associado[]>(this.baseUrl);
  }

  buscarPorId(id: string): Observable<Associado> {
    return this.http.get<Associado>(`${this.baseUrl}/${id}`);
  }

  buscarPorCpf(cpf: string): Observable<Associado> {
    return this.http.get<Associado>(`${this.baseUrl}/cpf/${cpf}`);
  }

  criar(associado: AssociadoDTO): Observable<Associado> {
    return this.http.post<Associado>(this.baseUrl, associado);
  }

  atualizar(id: string, associado: AssociadoDTO): Observable<Associado> {
    return this.http.put<Associado>(`${this.baseUrl}/${id}`, associado);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  alterarStatus(id: string, ativo: boolean): Observable<Associado> {
    return this.http.patch<Associado>(`${this.baseUrl}/${id}/status?ativo=${ativo}`, {});
  }

  verificarStatusCpf(cpf: string): Observable<CpfStatus> {
    return this.http.get<CpfStatus>(`${this.baseUrl}/cpf/${cpf}/status`);
  }
}
