package br.com.desafio_votacao.service;

import br.com.desafio_votacao.client.CpfValidator;
import br.com.desafio_votacao.dto.AssociadoDTO;
import br.com.desafio_votacao.model.Associado;
import br.com.desafio_votacao.repository.AssociadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssociadoServiceTest {

    @Mock
    private AssociadoRepository associadoRepository;

    @Mock
    private CpfValidator cpfValidator;

    @InjectMocks
    private AssociadoService associadoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarTodosAssociadosPaginado_DeveRetornarAssociadosPaginados() {
        Associado associado1 = new Associado("12345678901");
        associado1.setId("1");
        Associado associado2 = new Associado("10987654321");
        associado2.setId("2");

        when(associadoRepository.count()).thenReturn(Mono.just(2L));
        when(associadoRepository.findAll()).thenReturn(Flux.just(associado1, associado2));

        StepVerifier.create(associadoService.listarTodosAssociadosPaginado(0, 10))
            .assertNext(page -> {
                assertEquals(0, page.page());
                assertEquals(10, page.size());
                assertEquals(2L, page.totalElements());
                assertTrue(page.content().contains(associado1));
                assertTrue(page.content().contains(associado2));
            })
            .verifyComplete();
    }

    @Test
    void criarAssociado_ComCpfValido_DeveSalvarAssociado() {
        AssociadoDTO dto = new AssociadoDTO();
        dto.setCpf("58086250172");
        
        when(associadoRepository.existsByCpf(dto.getCpf())).thenReturn(Mono.just(false));
        when(associadoRepository.save(any(Associado.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(associadoService.criarAssociado(dto))
            .assertNext(associado -> {
                assertEquals(dto.getCpf(), associado.getCpf());
            })
            .verifyComplete();

        verify(associadoRepository).save(any(Associado.class));
    }

    @Test
    void criarAssociado_ComCpfFormatoInvalido_DeveRetornarErro() {
        AssociadoDTO dto = new AssociadoDTO();
        dto.setCpf("11111111111"); 
        
        StepVerifier.create(associadoService.criarAssociado(dto))
            .expectErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof ResponseStatusException);
                assertEquals("400 BAD_REQUEST \"CPF inválido - formato incorreto\"", throwable.getMessage());
            })
            .verify();
        
        verify(associadoRepository, never()).save(any());
    }

}
