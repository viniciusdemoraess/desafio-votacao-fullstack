package br.com.desafio_votacao.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "associados")
public class Associado {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String cpf;
    
    private LocalDateTime dataCadastro;
    
    private Boolean ativo;

    public Associado(String cpf) {
        this.cpf = cpf;
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }


}
