import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Voto, VotoDTO, ResultadoVotacao } from '../models/voto.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VotoService {
  private readonly baseUrl = `${environment.apiUrl}/pautas`;

  constructor(private http: HttpClient) {}

  listarVotosPorPauta(pautaId: string): Observable<Voto[]> {
    return this.http.get<Voto[]>(`${this.baseUrl}/${pautaId}/votos`);
  }

  registrarVoto(pautaId: string, voto: VotoDTO): Observable<Voto> {
    const url = `${this.baseUrl}/${pautaId}/votos`;
    
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    console.log('VotoService - registrarVoto:', {
      url: url,
      pautaId: pautaId,
      voto: voto,
      baseUrl: this.baseUrl,
      headers: headers.keys()
    });
    
    return this.http.post<Voto>(url, voto, { headers });
  }

  obterResultado(pautaId: string): Observable<ResultadoVotacao> {
    return this.http.get<ResultadoVotacao>(`${this.baseUrl}/${pautaId}/votos/resultado`);
  }

  // Método de teste para debug
  testarVotoComCurl(pautaId: string, associadoId: string, voto: string): Observable<any> {
    const url = `${this.baseUrl}/${pautaId}/votos`;
    const data = { associadoId, voto };
    
    console.log('Teste como curl:', {
      url,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    
    return this.http.post(url, data, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }
}
