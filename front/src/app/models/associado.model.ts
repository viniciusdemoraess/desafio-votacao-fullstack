export interface Associado {
  id: string;
  cpf: string;
  dataCadastro: string;
  ativo: boolean;
}

export interface AssociadoDTO {
  cpf: string;
}

export interface CpfStatus {
  status: 'ABLE_TO_VOTE' | 'UNABLE_TO_VOTE';
}
