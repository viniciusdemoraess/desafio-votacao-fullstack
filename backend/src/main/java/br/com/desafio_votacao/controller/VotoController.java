package br.com.desafio_votacao.controller;

import br.com.desafio_votacao.dto.VotoDTO;
import br.com.desafio_votacao.model.Voto;
import br.com.desafio_votacao.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
@CrossOrigin(origins = "*")
@Tag(name = "Votos", description = "Endpoints para gerenciamento de votos em pautas")
@AllArgsConstructor
public class VotoController {

    private static final Logger logger = LoggerFactory.getLogger(VotoController.class);

    @Autowired
    private VotoService votoService;

    @PostMapping
    @Operation(summary = "Registra um voto", description = "Registra o voto de um associado em uma pauta específica")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro ao registrar voto - associado não habilitado, sessão fechada"),
        @ApiResponse(responseCode = "404", description = "Pauta ou associado não encontrado"),
        @ApiResponse(responseCode = "409", description = "Associado já votou nesta pauta"),
        @ApiResponse(responseCode = "403", description = "Associado não está habilitado para votar")
    })
    public Mono<ResponseEntity<Voto>> registrarVoto(
            @PathVariable String pautaId,
            @Valid @RequestBody VotoDTO votoDTO) {
        
        logger.info("[registrarVoto()] Registrando voto para pauta ID: {} do associado ID: {}", 
                pautaId, votoDTO.getAssociadoId());
        
        return votoService.registrarVoto(pautaId, votoDTO)
                .map(voto -> ResponseEntity.status(HttpStatus.CREATED).body(voto));
    }

    @GetMapping
    @Operation(summary = "Lista votos de uma pauta", description = "Retorna todos os votos registrados para uma pauta específica")
    @ApiResponse(responseCode = "200", description = "Votos listados com sucesso")
    public Flux<Voto> listarVotosPorPauta(@PathVariable String pautaId) {
        logger.info("[listarVotosPorPauta()] Listando votos da pauta ID: {}", pautaId);
        return votoService.listarVotosPorPauta(pautaId);
    }

    @GetMapping("/resultado")
    @Operation(summary = "Obter resultado da votação", description = "Retorna a contabilização dos votos de uma pauta")
    @ApiResponse(responseCode = "200", description = "Resultado obtido com sucesso")
    public Mono<Map<String, Long>> obterResultado(@PathVariable String pautaId) {
        logger.info("[obterResultado()] Obtendo resultado da votação para pauta ID: {}", pautaId);
        return votoService.contabilizarVotosPorPauta(pautaId);
    }
}
