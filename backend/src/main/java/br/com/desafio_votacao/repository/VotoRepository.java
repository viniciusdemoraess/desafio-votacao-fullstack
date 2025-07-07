package br.com.desafio_votacao.repository;

import br.com.desafio_votacao.enums.VotoOpcao;
import br.com.desafio_votacao.model.Voto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VotoRepository extends ReactiveMongoRepository<Voto, String> {
    Flux<Voto> findByPautaId(String pautaId);
    Mono<Boolean> existsByPautaIdAndAssociadoId(String pautaId, String associadoId);
    Mono<Long> countByPautaIdAndOpcaoVoto(String pautaId, VotoOpcao opcaoVoto);
}
