package br.com.desafio_votacao.repository;

import br.com.desafio_votacao.model.Associado;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface AssociadoRepository extends ReactiveMongoRepository<Associado, String> {
    
    /**
     * Busca um associado por CPF
     * 
     * @param cpf CPF do associado
     * @return Mono com o associado, se existir
     */
    Mono<Associado> findByCpf(String cpf);
    
    /**
     * Verifica se existe um associado com o CPF informado
     * 
     * @param cpf CPF a ser verificado
     * @return Mono com true se existir, false caso contrário
     */
    Mono<Boolean> existsByCpf(String cpf);
}
