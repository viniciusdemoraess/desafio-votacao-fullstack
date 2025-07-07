package br.com.desafio_votacao.controller;

import br.com.desafio_votacao.dto.PautaDTO;
import br.com.desafio_votacao.model.Pauta;
import br.com.desafio_votacao.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/pautas")
@CrossOrigin(origins = "*")
@Tag(name = "Pautas", description = "Endpoints para gerenciamento de pautas e sessões de votação")
@AllArgsConstructor
public class PautaController {

    private static final Logger logger = LoggerFactory.getLogger(PautaController.class);

    private final PautaService pautaService;

    @GetMapping
    @Operation(summary = "Lista todas as pautas", description = "Retorna uma lista com todas as pautas cadastradas no sistema")
    @ApiResponse(responseCode = "200", description = "Pautas listadas com sucesso")
    public Flux<Pauta> listarPautas(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("[listarPautas()] Listando pautas página {} com tamanho {}", page, size);
        return pautaService.listarPautasPaginadas(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma pauta pelo ID", description = "Retorna os dados de uma pauta específica pelo seu ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pauta encontrada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public Mono<ResponseEntity<Pauta>> buscarPautaPorId(@PathVariable String id) {
        logger.info("[buscarPautaPorId()] Buscando pauta com ID: {}", id);
        return pautaService.buscarPautaPorId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova pauta", description = "Cadastra uma nova pauta no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public Mono<Pauta> criarPauta(@Valid @RequestBody PautaDTO pautaDTO) {
        logger.info("[criarPauta] Criando nova pauta: {}", pautaDTO.getTitulo());
        return pautaService.criarPauta(pautaDTO);
    }

    @PostMapping("/{id}/sessao")
    @Operation(summary = "Abre sessão de votação", description = "Abre uma sessão de votação para uma pauta específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessão aberta com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro ao abrir sessão"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public Mono<ResponseEntity<Pauta>> abrirSessaoVotacao(
            @PathVariable String id,
            @RequestParam(required = false) Integer duracaoMinutos) {
        
        logger.info("[abrirSessaoVotacao()] Abrindo sessão para pauta ID: {} com duração: {} minutos", 
                id, duracaoMinutos != null ? duracaoMinutos : "padrão (1)");
        
        return pautaService.abrirSessaoVotacao(id, duracaoMinutos)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    logger.error("Erro ao abrir sessão: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @GetMapping("/{id}/sessao/status")
    @Operation(summary = "Verifica status da sessão", description = "Verifica se a sessão de votação de uma pauta está aberta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status verificado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public Mono<ResponseEntity<Boolean>> verificarSessaoAberta(@PathVariable String id) {
        logger.info("[verificarSessaoAberta()] Verificando status da sessão para pauta ID: {}", id);
        return pautaService.verificarSessaoAberta(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
