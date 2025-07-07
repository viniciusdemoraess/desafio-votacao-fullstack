export interface Voto {
  id: string;
  pautaId: string;
  associadoId: string;
  opcaoVoto: OpcaoVoto;
  dataRegistro: string;
}

export interface VotoDTO {
  associadoId: string;
  voto: OpcaoVoto;
}

export enum OpcaoVoto {
  SIM = 'SIM',
  NAO = 'NAO'
}

export interface ResultadoVotacao {
  pautaId: string;
  total: number;
  sim: number;
  nao: number;
  resultado: string;
}
