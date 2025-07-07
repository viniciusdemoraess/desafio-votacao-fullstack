package br.com.desafio_votacao.service;

import br.com.desafio_votacao.client.CpfValidator;
import br.com.desafio_votacao.dto.VotoDTO;
import br.com.desafio_votacao.enums.VotoOpcao;
import br.com.desafio_votacao.exception.UnableToVoteException;
import br.com.desafio_votacao.model.Voto;
import br.com.desafio_votacao.repository.VotoRepository;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class VotoService {
    private static final Logger logger = LoggerFactory.getLogger(VotoService.class);

    
    private final VotoRepository votoRepository;

    
    private final PautaService pautaService;
    
    
    private final CpfValidator cpfValidator;
    
    
    private final AssociadoService associadoService;

    public Mono<Voto> registrarVoto(String pautaId, VotoDTO votoDTO) {
        return associadoService.buscarAssociadoPorId(votoDTO.getAssociadoId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Associado não encontrado com o ID: " + votoDTO.getAssociadoId())))
            .flatMap(associado -> {
                if (!associado.getAtivo()) {
                    logger.warn("Associado {} está inativo", votoDTO.getAssociadoId());
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Associado está inativo e não pode votar"));
                }
                
                return validarAssociadoPodeVotar(associado.getCpf())
                    .flatMap(podeVotar -> {
                        if (!podeVotar) {
                            logger.warn("Associado {} não está habilitado para votar", votoDTO.getAssociadoId());
                            return Mono.error(new UnableToVoteException(HttpStatus.NOT_FOUND));
                        }
                        
                        return pautaService.verificarSessaoAberta(pautaId)
                            .flatMap(sessaoAberta -> {
                                if (!sessaoAberta) {
                                    logger.error("Tentativa de voto em sessão fechada para pauta {}", pautaId);
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                        "Sessão de votação não está aberta para esta pauta"));
                                }

                                return votoRepository.existsByPautaIdAndAssociadoId(pautaId, associado.getId())
                                    .flatMap(jaVotou -> {
                                        if (jaVotou) {
                                            logger.warn("Associado {} tentou votar mais de uma vez na pauta {}", 
                                                associado.getId(), pautaId);
                                            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, 
                                                "Associado já votou nesta pauta"));
                                        }

                                        Voto voto = new Voto();
                                        voto.setPautaId(pautaId);
                                        voto.setAssociadoId(associado.getId());
                                        voto.setOpcaoVoto(votoDTO.getVoto());
                                        voto.setDataRegistro(LocalDateTime.now());

                                        logger.info("Registrando voto do associado {} na pauta {}", 
                                            associado.getId(), pautaId);
                                        return votoRepository.save(voto);
                                    });
                            });
                    });
            });
    }

    public Flux<Voto> listarVotosPorPauta(String pautaId) {
        logger.info("Listando votos para a pauta {}", pautaId);
        return votoRepository.findByPautaId(pautaId);
    }
    
    /**
     * Valida se um associado pode votar usando o serviço externo (fake)
     * Implementação da Tarefa Bônus 1
     * 
     * @param cpf CPF do associado
     * @return Mono<Boolean> - true se pode votar, false caso contrário
     */
    private Mono<Boolean> validarAssociadoPodeVotar(String cpf) {
        return cpfValidator.validarCpf(cpf)
            .flatMap(status -> {
                if ("ABLE_TO_VOTE".equals(status.status())) {
                    return Mono.just(true);
                } else if ("UNABLE_TO_VOTE".equals(status.status())) {
                    return Mono.error(new UnableToVoteException(HttpStatus.NOT_FOUND));
                } else {
                    return Mono.error(new UnableToVoteException(HttpStatus.NOT_FOUND));
                }
            })
            .onErrorResume(ex -> {
                if (ex instanceof UnableToVoteException) {
                    return Mono.error(ex);
                }
                // Se o client lançar 404, trate como CPF inválido
                return Mono.error(new UnableToVoteException(HttpStatus.NOT_FOUND));
            });
    }

    public Mono<Map<String, Long>> contabilizarVotosPorPauta(String pautaId) {
        Mono<Long> votosSim = votoRepository.countByPautaIdAndOpcaoVoto(pautaId, VotoOpcao.SIM);
        Mono<Long> votosNao = votoRepository.countByPautaIdAndOpcaoVoto(pautaId, VotoOpcao.NAO);

        return Mono.zip(votosSim, votosNao)
            .map(tuple -> {
                Map<String, Long> resultado = new HashMap<>();
                resultado.put("sim", tuple.getT1());
                resultado.put("nao", tuple.getT2());
                resultado.put("total", tuple.getT1() + tuple.getT2());
                return resultado;
            });
    }
}
