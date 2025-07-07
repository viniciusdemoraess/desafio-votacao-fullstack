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

  listarTodas(): Observable<Pauta[]> {
    return this.http.get<Pauta[]>(this.baseUrl);
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
