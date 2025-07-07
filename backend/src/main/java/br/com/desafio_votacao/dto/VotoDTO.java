package br.com.desafio_votacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import br.com.desafio_votacao.enums.VotoOpcao;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VotoDTO {
    @NotBlank(message = "O ID do associado é obrigatório")
    private String associadoId;
    
    @NotNull(message = "O voto é obrigatório (SIM ou NAO)")
    private VotoOpcao voto;

}
