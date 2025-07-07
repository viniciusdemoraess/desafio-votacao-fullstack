package br.com.desafio_votacao.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@ToString
@Document(collection = "pautas")
public class Pauta {
    @Id
    private String id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataCriacao;
    private Boolean sessaoAberta;
    private LocalDateTime inicioSessao;
    private LocalDateTime fimSessao;

    public Pauta() {
        this.dataCriacao = LocalDateTime.now();
        this.sessaoAberta = false;
    }

}
