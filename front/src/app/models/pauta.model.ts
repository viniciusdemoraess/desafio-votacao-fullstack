export interface Pauta {
  id: string;
  titulo: string;
  descricao: string;
  dataCriacao: string;
  sessaoAberta: boolean;
  inicioSessao?: string;
  fimSessao?: string;
}

export interface PautaDTO {
  titulo: string;
  descricao: string;
}
