package br.com.desafio_votacao.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * Cliente fake para validação de CPF
 * Retorna aleatoriamente se um CPF é válido (ABLE_TO_VOTE) ou inválido (UNABLE_TO_VOTE).
 * Implementa a tarefa bônus 1 do desafio.
 */
@Component
public class CpfValidator {
    private static final Logger logger = LoggerFactory.getLogger(CpfValidator.class);
    private static final String CPF_REGEX = "\\d{11}";

    private final Random random = new Random();

    public record CpfStatus(String status) {}

    /**
     * Valida o CPF de forma aleatória.
     * Retorna HTTP 404 se o CPF for inválido (não possuir 11 dígitos numéricos).
     *
     * @param cpf CPF a ser validado
     * @return Mono com o status do CPF (ABLE_TO_VOTE ou UNABLE_TO_VOTE)
     */
    public Mono<CpfStatus> validarCpf(String cpf) {
        logger.info("Validando CPF: {}", cpf);

        if (cpf == null || !cpf.matches(CPF_REGEX)) {
            logger.warn("CPF inválido: {}", cpf);
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "CPF inválido"));
        }

        return Mono.defer(() -> {
            boolean ableToVote = random.nextInt(10) < 7;
            String status = ableToVote ? "ABLE_TO_VOTE" : "UNABLE_TO_VOTE";

            logger.info("CPF {} status: {}", cpf, status);
            return Mono.just(new CpfStatus(status));
        });
    }
}
