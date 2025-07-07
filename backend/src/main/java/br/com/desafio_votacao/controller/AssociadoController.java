package br.com.desafio_votacao.controller;

import br.com.desafio_votacao.client.CpfValidator;
import br.com.desafio_votacao.dto.AssociadoDTO;
import br.com.desafio_votacao.dto.PageResponse;
import br.com.desafio_votacao.model.Associado;
import br.com.desafio_votacao.service.AssociadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/associados")
@CrossOrigin(origins = "*")
@Tag(name = "Associados", description = "Endpoints para gerenciamento e validação de associados")
@AllArgsConstructor
public class AssociadoController {

    private static final Logger logger = LoggerFactory.getLogger(AssociadoController.class);

    private final CpfValidator cpfValidator;
    
    private final AssociadoService associadoService;

    private static final String ABLE_TO_VOTE = "ABLE_TO_VOTE";
    private static final String UNABLE_TO_VOTE = "UNABLE_TO_VOTE";

    @GetMapping
    @Operation(summary = "Lista todos os associados", 
               description = "Retorna uma lista com todos os associados cadastrados no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Associados listados com sucesso")
    })
    public Mono<PageResponse<Associado>> listarTodosAssociados(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        logger.info("[listarTodosAssociados()] Listando todos os associados");

        return associadoService.listarTodosAssociadosPaginado(page, size);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Busca um associado pelo ID",
               description = "Retorna os dados de um associado específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Associado encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Associado não encontrado")
    })
    public Mono<ResponseEntity<Associado>> buscarAssociadoPorId(@PathVariable String id) {

        logger.info("[buscarAssociadoPorId()] Buscando associado com ID: {}", id);

        return associadoService.buscarAssociadoPorId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Busca um associado pelo CPF",
               description = "Retorna os dados de um associado específico pelo seu CPF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Associado encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Associado não encontrado")
    })
    public Mono<ResponseEntity<Associado>> buscarAssociadoPorCpf(@PathVariable String cpf) {

        logger.info("[buscarAssociadoPorCpf()] Buscando associado com CPF: {}", cpf);

        return associadoService.buscarAssociadoPorCpf(cpf)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cpf/{cpf}/status")
    @Operation(summary = "Verifica se um CPF pode votar", 
            description = "Verifica, através do CPF, se o associado está habilitado a votar (ABLE_TO_VOTE) ou não (UNABLE_TO_VOTE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status do CPF verificado com sucesso"),
        @ApiResponse(responseCode = "404", description = "CPF não habilitado para votar ou inválido"),
        @ApiResponse(responseCode = "500", description = "Erro ao verificar status do CPF")
    })
    public Mono<ResponseEntity<CpfValidator.CpfStatus>> verificarStatusCpf(@PathVariable String cpf) {

        logger.info("[verificarStatusCpf()] Verificando status do CPF: {}", cpf);
        
        return cpfValidator.validarCpf(cpf)
                .flatMap(cpfStatus -> {
                    if (ABLE_TO_VOTE.equals(cpfStatus.status())) {
                        return Mono.just(ResponseEntity.ok(cpfStatus));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CpfValidator.CpfStatus(UNABLE_TO_VOTE)));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Erro ao validar CPF: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CpfValidator.CpfStatus(UNABLE_TO_VOTE)));
                });
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo associado",
               description = "Cadastra um novo associado no sistema, validando se o CPF é válido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Associado criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "CPF inválido ou dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Já existe um associado com este CPF")
    })
    public Mono<Associado> criarAssociado(@Valid @RequestBody AssociadoDTO associadoDTO) {

        logger.info("[criarAssociado()] Cadastrando novo associado: {}", associadoDTO);

        return associadoService.criarAssociado(associadoDTO);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um associado",
               description = "Atualiza os dados de um associado existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Associado atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Associado não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe outro associado com este CPF")
    })
    public Mono<Associado> atualizarAssociado(@PathVariable String id, @Valid @RequestBody AssociadoDTO associadoDTO) {

        logger.info("[atualizarAssociado()] Atualizando associado com ID: {}", id);

        return associadoService.atualizarAssociado(id, associadoDTO);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status de um associado",
               description = "Ativa ou inativa um associado existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status do associado alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Associado não encontrado")
    })
    public Mono<Associado> alterarStatusAssociado(@PathVariable String id, @RequestParam Boolean ativo) {

        logger.info("[alterarStatusAssociado()] Alterando status do associado com ID: {} para: {}", id, ativo ? "ativo" : "inativo");

        return associadoService.alterarStatusAssociado(id, ativo);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um associado",
               description = "Remove um associado existente do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Associado removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Associado não encontrado")
    })
    public Mono<Void> excluirAssociado(@PathVariable String id) {

        logger.info("[excluirAssociado()] Excluindo associado com ID: {}", id);

        return associadoService.excluirAssociado(id);
    }
}
