import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pauta, PautaDTO } from '../models/pauta.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PautaService {
  private readonly baseUrl = `${environment.apiUrl}/pautas`;

  constructor(private http: HttpClient) {}

  listarTodas(page: number = 0, size: number = 10): Observable<{ content: Pauta[], totalElements: number }> {
    return this.http.get<{ content: Pauta[], totalElements: number }>(
      `${this.baseUrl}?page=${page}&size=${size}`
    );
  }

  buscarPorId(id: string): Observable<Pauta> {
    return this.http.get<Pauta>(`${this.baseUrl}/${id}`);
  }

  criar(pauta: PautaDTO): Observable<Pauta> {
    return this.http.post<Pauta>(this.baseUrl, pauta);
  }

  abrirSessaoVotacao(id: string, duracaoMinutos?: number): Observable<Pauta> {
    const params = duracaoMinutos ? `?duracaoMinutos=${duracaoMinutos}` : '';
    return this.http.post<Pauta>(`${this.baseUrl}/${id}/sessao${params}`, {});
  }

  verificarSessaoAberta(id: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${id}/sessao/status`);
  }
}
