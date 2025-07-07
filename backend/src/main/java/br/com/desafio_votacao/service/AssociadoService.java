package br.com.desafio_votacao.service;

import br.com.desafio_votacao.client.CpfValidator;
import br.com.desafio_votacao.dto.AssociadoDTO;
import br.com.desafio_votacao.dto.PageResponse;
import br.com.desafio_votacao.model.Associado;
import br.com.desafio_votacao.repository.AssociadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Serviço para gerenciamento de associados
 */
@Service
public class AssociadoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssociadoService.class);
    
    private final AssociadoRepository associadoRepository;
    private final CpfValidator cpfValidator;
    
    @Autowired
    public AssociadoService(AssociadoRepository associadoRepository, CpfValidator cpfValidator) {
        this.associadoRepository = associadoRepository;
        this.cpfValidator = cpfValidator;
    }
    
    /**
     * Lista todos os associados cadastrados
     * 
     * @return Flux com todos os associados
     */
    public Mono<PageResponse<Associado>> listarTodosAssociadosPaginado(int page, int size) {
        long skip = (long) page * size;

        return associadoRepository.count()
                .flatMap(total -> associadoRepository.findAll()
                    .skip(skip)
                    .take(size)
                    .collectList()
                    .map(content -> new PageResponse<>(content, page, size, total))
                );
    }
    
    /**
     * Busca um associado pelo ID
     * 
     * @param id ID do associado
     * @return Mono com o associado encontrado ou vazio se não existir
     */
    public Mono<Associado> buscarAssociadoPorId(String id) {
        logger.info("Buscando associado com ID: {}", id);
        return associadoRepository.findById(id);
    }
    
    /**
     * Busca um associado pelo CPF
     * 
     * @param cpf CPF do associado
     * @return Mono com o associado encontrado ou vazio se não existir
     */
    public Mono<Associado> buscarAssociadoPorCpf(String cpf) {
        logger.info("Buscando associado com CPF: {}", cpf);
        return associadoRepository.findByCpf(cpf);
    }
    
    /**
     * Cria um novo associado
     * 
     * @param associadoDTO DTO com os dados do associado
     * @return Mono com o associado criado
     */
    public Mono<Associado> criarAssociado(AssociadoDTO associadoDTO) {
        logger.info("Criando novo associado: {}", associadoDTO);
        
        // Primeiro valida o formato do CPF
        if (!isValidCpfFormat(associadoDTO.getCpf())) {
            logger.warn("CPF {} tem formato inválido", associadoDTO.getCpf());
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "CPF inválido - formato incorreto"));
        }
        
        // Verifica se já existe associado com o mesmo CPF
        return associadoRepository.existsByCpf(associadoDTO.getCpf())
            .flatMap(existe -> {
                if (existe) {
                    logger.warn("Já existe um associado com o CPF: {}", associadoDTO.getCpf());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.CONFLICT, "Já existe um associado com este CPF"));
                }
                
                // Cria o associado diretamente após validação de formato
                Associado associado = new Associado(associadoDTO.getCpf());
                return associadoRepository.save(associado);
            });
    }
    
    /**
     * Atualiza os dados de um associado existente
     * 
     * @param id ID do associado
     * @param associadoDTO DTO com os novos dados
     * @return Mono com o associado atualizado
     */
    public Mono<Associado> atualizarAssociado(String id, AssociadoDTO associadoDTO) {
        logger.info("Atualizando associado com ID: {}", id);
        
        return associadoRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Associado não encontrado com o ID: " + id)))
            .flatMap(associadoExistente -> {
                // Verifica se o CPF foi alterado e se já existe outro associado com o novo CPF
                if (!associadoExistente.getCpf().equals(associadoDTO.getCpf())) {
                    return associadoRepository.existsByCpf(associadoDTO.getCpf())
                        .flatMap(existe -> {
                            if (existe) {
                                logger.warn("Já existe outro associado com o CPF: {}", associadoDTO.getCpf());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.CONFLICT, "Já existe outro associado com este CPF"));
                            }
                            
                            // Atualiza os dados do associado
                            associadoExistente.setCpf(associadoDTO.getCpf());
                            
                            return associadoRepository.save(associadoExistente);
                        });
                } else {
                    // O CPF continua o mesmo, não há o que atualizar
                    return Mono.just(associadoExistente);
                }
            });
    }
    
    /**
     * Ativa ou inativa um associado
     * 
     * @param id ID do associado
     * @param ativo true para ativar, false para inativar
     * @return Mono com o associado atualizado
     */
    public Mono<Associado> alterarStatusAssociado(String id, Boolean ativo) {
        logger.info("Alterando status do associado com ID: {} para: {}", id, ativo ? "ativo" : "inativo");
        
        return associadoRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Associado não encontrado com o ID: " + id)))
            .flatMap(associado -> {
                associado.setAtivo(ativo);
                return associadoRepository.save(associado);
            });
    }
    
    /**
     * Exclui um associado pelo ID
     * 
     * @param id ID do associado
     * @return Mono vazio indicando conclusão
     */
    public Mono<Void> excluirAssociado(String id) {
        logger.info("Excluindo associado com ID: {}", id);
        
        return associadoRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Associado não encontrado com o ID: " + id)))
            .flatMap(associadoRepository::delete);
    }
    
    /**
     * Verifica se um associado está habilitado para votar
     * 
     * @param associadoId ID do associado
     * @return Mono com true se associado está habilitado, false caso contrário
     */
    public Mono<Boolean> verificarAssociadoHabilitado(String associadoId) {
        return associadoRepository.findById(associadoId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Associado não encontrado com o ID: " + associadoId)))
            .flatMap(associado -> {
                if (!associado.getAtivo()) {
                    return Mono.just(false);
                }
                
                return cpfValidator.validarCpf(associado.getCpf())
                    .map(status -> "ABLE_TO_VOTE".equals(status.status()));
            });
    }
    
    /**
     * Valida o formato do CPF
     * 
     * @param cpf CPF a ser validado
     * @return true se o formato é válido
     */
    private boolean isValidCpfFormat(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        
        // Remove pontos e traços
        cpf = cpf.replaceAll("[^0-9]", "");
        
        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }
        
        // Verifica se não são todos os dígitos iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int[] digits = cpf.chars().map(c -> c - '0').toArray();
            
            // Primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += digits[i] * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;
            
            // Segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += digits[i] * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;
            
            return digits[9] == firstDigit && digits[10] == secondDigit;
        } catch (Exception e) {
            return false;
        }
    }
}
