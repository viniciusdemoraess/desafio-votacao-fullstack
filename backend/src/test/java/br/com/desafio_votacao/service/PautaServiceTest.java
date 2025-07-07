package br.com.desafio_votacao.service;

import br.com.desafio_votacao.dto.PautaDTO;
import br.com.desafio_votacao.model.Pauta;
import br.com.desafio_votacao.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarTodasPautas_DeveRetornarTodasPautasPaginadas() {
        Pauta pauta1 = new Pauta();
        pauta1.setId("1");
        pauta1.setTitulo("Pauta 1");

        Pauta pauta2 = new Pauta();
        pauta2.setId("2");
        pauta2.setTitulo("Pauta 2");

        when(pautaRepository.count()).thenReturn(Mono.just(2L));
        when(pautaRepository.findAll()).thenReturn(Flux.just(pauta1, pauta2));

        StepVerifier.create(pautaService.listarPautasPaginadas(0, 10))
            .assertNext(page -> {
                assertEquals(0, page.page());
                assertEquals(10, page.size());
                assertEquals(2L, page.totalElements());
                assertEquals(2, page.content().size());
                assertTrue(page.content().contains(pauta1));
                assertTrue(page.content().contains(pauta2));
            })
            .verifyComplete();
    }


    @Test
    void buscarPautaPorId_QuandoPautaExiste_DeveRetornarPauta() {
        String id = "1";
        Pauta pauta = new Pauta();
        pauta.setId(id);
        pauta.setTitulo("Pauta 1");
        
        when(pautaRepository.findById(id)).thenReturn(Mono.just(pauta));
        
        StepVerifier.create(pautaService.buscarPautaPorId(id))
            .expectNext(pauta)
            .verifyComplete();
    }

    @Test
    void buscarPautaPorId_QuandoPautaNaoExiste_DeveRetornarEmpty() {
        String id = "naoExiste";
        when(pautaRepository.findById(id)).thenReturn(Mono.empty());
        
        StepVerifier.create(pautaService.buscarPautaPorId(id))
            .verifyComplete();
    }

    @Test
    void criarPauta_DeveRetornarPautaCriada() {
        PautaDTO pautaDTO = new PautaDTO();
        pautaDTO.setTitulo("Nova Pauta");
        pautaDTO.setDescricao("Descrição da nova pauta");
        
        Pauta pautaSalva = new Pauta();
        pautaSalva.setId("1");
        pautaSalva.setTitulo(pautaDTO.getTitulo());
        pautaSalva.setDescricao(pautaDTO.getDescricao());
        pautaSalva.setDataCriacao(LocalDateTime.now());
        pautaSalva.setSessaoAberta(false);
        
        when(pautaRepository.save(any(Pauta.class))).thenReturn(Mono.just(pautaSalva));
        
        StepVerifier.create(pautaService.criarPauta(pautaDTO))
            .expectNextMatches(pauta -> 
                pauta.getId().equals("1") &&
                pauta.getTitulo().equals(pautaDTO.getTitulo()) &&
                pauta.getDescricao().equals(pautaDTO.getDescricao()) &&
                !pauta.getSessaoAberta())
            .verifyComplete();
    }

    @Test
    void abrirSessaoVotacao_QuandoPautaExisteESessaoFechada_DeveAbrirSessao() {
        String pautaId = "1";
        Integer duracao = 5;
        
        Pauta pautaExistente = new Pauta();
        pautaExistente.setId(pautaId);
        pautaExistente.setTitulo("Pauta Existente");
        pautaExistente.setSessaoAberta(false);
        
        Pauta pautaComSessaoAberta = new Pauta();
        pautaComSessaoAberta.setId(pautaId);
        pautaComSessaoAberta.setTitulo("Pauta Existente");
        pautaComSessaoAberta.setSessaoAberta(true);
        pautaComSessaoAberta.setInicioSessao(LocalDateTime.now());
        pautaComSessaoAberta.setFimSessao(LocalDateTime.now().plusMinutes(duracao));
        
        when(pautaRepository.findById(pautaId)).thenReturn(Mono.just(pautaExistente));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(Mono.just(pautaComSessaoAberta));
        
        StepVerifier.create(pautaService.abrirSessaoVotacao(pautaId, duracao))
            .expectNextMatches(pauta -> 
                pauta.getId().equals(pautaId) &&
                pauta.getSessaoAberta() &&
                pauta.getInicioSessao() != null &&
                pauta.getFimSessao() != null)
            .verifyComplete();
    }

    @Test
    void verificarSessaoAberta_QuandoSessaoEstaAberta_DeveRetornarTrue() {
        String pautaId = "1";
        
        Pauta pautaComSessaoAberta = new Pauta();
        pautaComSessaoAberta.setId(pautaId);
        pautaComSessaoAberta.setSessaoAberta(true);
        pautaComSessaoAberta.setInicioSessao(LocalDateTime.now().minusMinutes(1));
        pautaComSessaoAberta.setFimSessao(LocalDateTime.now().plusMinutes(4));
        
        when(pautaRepository.findById(pautaId)).thenReturn(Mono.just(pautaComSessaoAberta));
        
        StepVerifier.create(pautaService.verificarSessaoAberta(pautaId))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void verificarSessaoAberta_QuandoSessaoJaFechou_DeveRetornarFalse() {
        String pautaId = "1";
        
        Pauta pautaComSessaoExpirada = new Pauta();
        pautaComSessaoExpirada.setId(pautaId);
        pautaComSessaoExpirada.setSessaoAberta(true); // Ainda está marcada como aberta
        pautaComSessaoExpirada.setInicioSessao(LocalDateTime.now().minusMinutes(10));
        pautaComSessaoExpirada.setFimSessao(LocalDateTime.now().minusMinutes(5)); // Mas já passou o tempo
        
        when(pautaRepository.findById(pautaId)).thenReturn(Mono.just(pautaComSessaoExpirada));
        
        StepVerifier.create(pautaService.verificarSessaoAberta(pautaId))
            .expectNext(false)
            .verifyComplete();
    }
}
