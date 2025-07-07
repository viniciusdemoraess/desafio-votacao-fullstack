package br.com.desafio_votacao.scheduler;

import br.com.desafio_votacao.service.PautaService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SessaoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SessaoScheduler.class);
    private final PautaService pautaService;

    public SessaoScheduler(PautaService pautaService) {
        this.pautaService = pautaService;

        Flux.interval(Duration.ZERO, Duration.ofSeconds(30))
            .concatMap(tick -> fecharSessoes())
            .subscribe(
                success -> logger.info("Sessões expiradas fechadas com sucesso"),
                error -> logger.error("Erro ao fechar sessões expiradas", error)
            );
    }

    private Mono<Void> fecharSessoes() {
        logger.info("Verificando sessões para fechar...");
        return pautaService.fecharSessoesExpiradas();
    }
}

