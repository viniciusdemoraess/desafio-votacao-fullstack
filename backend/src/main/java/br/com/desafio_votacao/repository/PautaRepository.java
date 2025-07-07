package br.com.desafio_votacao.repository;

import br.com.desafio_votacao.model.Pauta;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PautaRepository extends ReactiveMongoRepository<Pauta, String> {

    Flux<Pauta> findBySessaoAbertaIsTrueAndFimSessaoBefore(LocalDateTime now);

    Flux<Pauta> findAllBy(Pageable pageable);

}
