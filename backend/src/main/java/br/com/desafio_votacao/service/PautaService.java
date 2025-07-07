package br.com.desafio_votacao.service;

import br.com.desafio_votacao.dto.PageResponse;
import br.com.desafio_votacao.dto.PautaDTO;
import br.com.desafio_votacao.model.Pauta;
import br.com.desafio_votacao.repository.PautaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PautaService {

    private static final Logger logger = LoggerFactory.getLogger(PautaService.class);

    @Autowired
    private PautaRepository pautaRepository;

    public Mono<PageResponse<Pauta>> listarPautasPaginadas(int page, int size) {
        long skip = (long) page * size;
    
        return pautaRepository.count()
            .flatMap(totalElements -> 
                pautaRepository.findAll()
                    .skip(skip)
                    .take(size)
                    .collectList()
                    .map(content -> {
                        return new PageResponse<>(content, page, size, totalElements);
                    })
            );
    }
    


    public Mono<Pauta> buscarPautaPorId(String id) {
        return pautaRepository.findById(id);
    }

    public Mono<Pauta> criarPauta(PautaDTO pautaDTO) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(pautaDTO.getTitulo());
        pauta.setDescricao(pautaDTO.getDescricao());
        pauta.setDataCriacao(LocalDateTime.now());
        pauta.setSessaoAberta(false);
        
        return pautaRepository.save(pauta);
    }

    public Mono<Pauta> abrirSessaoVotacao(String pautaId, Integer duracaoMinutos) {
        // Se não especificado, usa 1 minuto por default
        if (duracaoMinutos == null || duracaoMinutos <= 0) {
            duracaoMinutos = 1;
        }

        final LocalDateTime agora = LocalDateTime.now();
        final LocalDateTime fimSessao = agora.plusMinutes(duracaoMinutos);
        
        return pautaRepository.findById(pautaId)
            .switchIfEmpty(Mono.error(new RuntimeException("Pauta não encontrada")))
            .flatMap(pauta -> {
                if (Boolean.TRUE.equals(pauta.getSessaoAberta())) {
                    return Mono.error(new RuntimeException("Sessão de votação já está aberta para esta pauta"));
                }
                
                pauta.setSessaoAberta(true);
                pauta.setInicioSessao(agora);
                pauta.setFimSessao(fimSessao);
                
                return pautaRepository.save(pauta);
            });
    }

    public Mono<Boolean> verificarSessaoAberta(String pautaId) {
        return pautaRepository.findById(pautaId)
            .map(pauta -> {
                if (Boolean.FALSE.equals(pauta.getSessaoAberta())) {
                    return false;
                }
                
                LocalDateTime agora = LocalDateTime.now();
                return agora.isBefore(pauta.getFimSessao());
            })
            .defaultIfEmpty(false);
    }

    public Mono<Void> fecharSessoesExpiradas() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Verificando pautas com sessão expirada para fechar às {}", now);
    
        return pautaRepository.findBySessaoAbertaIsTrueAndFimSessaoBefore(now)
            .collectList()
            .flatMapMany(pautas -> {
                if (pautas.isEmpty()) {
                    logger.info("Nenhuma pauta com sessão expirada encontrada para fechar.");
                    return Flux.empty();
                } else {
                    return Flux.fromIterable(pautas);
                }
            })
            .doOnNext(pauta -> 
                logger.info("Encontrou pauta ID={} com sessão expirada, fechando...", pauta.getId()))
            .flatMap(pauta -> {
                pauta.setSessaoAberta(false);
                return pautaRepository.save(pauta)
                    .doOnSuccess(saved -> 
                        logger.info("Sessão da pauta ID={} foi fechada com sucesso.", saved.getId()));
            })
            .then();
    }
    
}
