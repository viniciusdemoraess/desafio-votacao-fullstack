package br.com.desafio_votacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PautaDTO {
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
    private String titulo;
    
    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 5, max = 500, message = "A descrição deve ter entre 5 e 500 caracteres")
    private String descricao;

}
