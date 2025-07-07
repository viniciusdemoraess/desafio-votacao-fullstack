package br.com.desafio_votacao.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import br.com.desafio_votacao.enums.VotoOpcao;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@ToString
@Document(collection = "votos")
@CompoundIndex(name = "pauta_associado_idx", def = "{'pautaId': 1, 'associadoId': 1}", unique = true)
public class Voto {
    @Id
    private String id;
    private String pautaId;
    private String associadoId;
    private VotoOpcao opcaoVoto;
    private LocalDateTime dataRegistro;

    public Voto() {
        this.dataRegistro = LocalDateTime.now();
    }

   
}
