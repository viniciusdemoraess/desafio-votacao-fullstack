package br.com.desafio_votacao.service;

import br.com.desafio_votacao.client.CpfValidator;
import br.com.desafio_votacao.dto.VotoDTO;
import br.com.desafio_votacao.enums.VotoOpcao;
import br.com.desafio_votacao.model.Associado;
import br.com.desafio_votacao.model.Voto;
import br.com.desafio_votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private CpfValidator cpfValidator;

    @Mock
    private AssociadoService associadoService;

    @InjectMocks
    private VotoService votoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrarVoto_QuandoAssociadoHabilitadoSessaoAbertaENaoVotouAinda_DeveRegistrarVoto() {
        // Arrange
        String pautaId = "1";
        String associadoId = "assoc123";
        VotoDTO votoDTO = new VotoDTO();
        votoDTO.setAssociadoId("12345678901");
        votoDTO.setVoto(VotoOpcao.SIM);
        
        Associado associado = new Associado();
        associado.setId(associadoId);
        associado.setCpf("12345678901");
        associado.setAtivo(true);
        
        CpfValidator.CpfStatus statusOk = new CpfValidator.CpfStatus("ABLE_TO_VOTE");
        
        when(associadoService.buscarAssociadoPorId(votoDTO.getAssociadoId())).thenReturn(Mono.just(associado));
        when(cpfValidator.validarCpf(associado.getCpf())).thenReturn(Mono.just(statusOk));
        when(pautaService.verificarSessaoAberta(pautaId)).thenReturn(Mono.just(true));
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, associado.getId())).thenReturn(Mono.just(false));
        
        Voto votoSalvo = new Voto();
        votoSalvo.setId("123");
        votoSalvo.setPautaId(pautaId);
        votoSalvo.setAssociadoId(associado.getId());
        votoSalvo.setOpcaoVoto(votoDTO.getVoto());
        
        when(votoRepository.save(any(Voto.class))).thenReturn(Mono.just(votoSalvo));
        
        // Act & Assert
        StepVerifier.create(votoService.registrarVoto(pautaId, votoDTO))
            .expectNextMatches(voto -> 
                voto.getId().equals(votoSalvo.getId()) && 
                voto.getPautaId().equals(pautaId) && 
                voto.getAssociadoId().equals(associado.getId()) &&
                voto.getOpcaoVoto().equals(votoDTO.getVoto()))
            .verifyComplete();
    }

    @Test
    void registrarVoto_QuandoAssociadoNaoHabilitado_DeveRetornarErro() {
        // Arrange
        String pautaId = "1";
        VotoDTO votoDTO = new VotoDTO();
        votoDTO.setAssociadoId("12345678901");
        votoDTO.setVoto(VotoOpcao.SIM);
        
        Associado associado = new Associado();
        associado.setId("assoc123");
        associado.setCpf("12345678901");
        associado.setAtivo(true);
        
        CpfValidator.CpfStatus statusNaoOk = new CpfValidator.CpfStatus("UNABLE_TO_VOTE");
        
        when(associadoService.buscarAssociadoPorId(votoDTO.getAssociadoId())).thenReturn(Mono.just(associado));
        when(cpfValidator.validarCpf(associado.getCpf())).thenReturn(Mono.just(statusNaoOk));
        
        // Act & Assert
        StepVerifier.create(votoService.registrarVoto(pautaId, votoDTO))
            .expectError()
            .verify();
    }

    @Test
    void registrarVoto_QuandoSessaoFechada_DeveRetornarErro() {
        // Arrange
        String pautaId = "1";
        VotoDTO votoDTO = new VotoDTO();
        votoDTO.setAssociadoId("12345678901");
        votoDTO.setVoto(VotoOpcao.SIM);
        
        Associado associado = new Associado();
        associado.setId("assoc123");
        associado.setCpf("12345678901");
        associado.setAtivo(true);
        
        CpfValidator.CpfStatus statusOk = new CpfValidator.CpfStatus("ABLE_TO_VOTE");
        
        when(associadoService.buscarAssociadoPorId(votoDTO.getAssociadoId())).thenReturn(Mono.just(associado));
        when(cpfValidator.validarCpf(associado.getCpf())).thenReturn(Mono.just(statusOk));
        when(pautaService.verificarSessaoAberta(pautaId)).thenReturn(Mono.just(false));
        
        // Act & Assert
        StepVerifier.create(votoService.registrarVoto(pautaId, votoDTO))
            .expectError()
            .verify();
    }

    @Test
    void registrarVoto_QuandoAssociadoJaVotou_DeveRetornarErro() {
        // Arrange
        String pautaId = "1";
        String associadoId = "assoc123";
        VotoDTO votoDTO = new VotoDTO();
        votoDTO.setAssociadoId("12345678901");
        votoDTO.setVoto(VotoOpcao.SIM);
        
        Associado associado = new Associado();
        associado.setId(associadoId);
        associado.setCpf("12345678901");
        associado.setAtivo(true);
        
        CpfValidator.CpfStatus statusOk = new CpfValidator.CpfStatus("ABLE_TO_VOTE");
        
        when(associadoService.buscarAssociadoPorId(votoDTO.getAssociadoId())).thenReturn(Mono.just(associado));
        when(cpfValidator.validarCpf(associado.getCpf())).thenReturn(Mono.just(statusOk));
        when(pautaService.verificarSessaoAberta(pautaId)).thenReturn(Mono.just(true));
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, associado.getId())).thenReturn(Mono.just(true));
        
        // Act & Assert
        StepVerifier.create(votoService.registrarVoto(pautaId, votoDTO))
            .expectError()
            .verify();
    }

    @Test
    void listarVotosPorPauta_DeveRetornarTodosOsVotos() {
        // Arrange
        String pautaId = "1";
        
        Voto voto1 = new Voto();
        voto1.setId("1");
        voto1.setPautaId(pautaId);
        voto1.setAssociadoId("12345678901");
        voto1.setOpcaoVoto(VotoOpcao.SIM);
        
        Voto voto2 = new Voto();
        voto2.setId("2");
        voto2.setPautaId(pautaId);
        voto2.setAssociadoId("98765432109");
        voto2.setOpcaoVoto(VotoOpcao.NAO);
        
        when(votoRepository.findByPautaId(pautaId)).thenReturn(Flux.just(voto1, voto2));
        
        // Act & Assert
        StepVerifier.create(votoService.listarVotosPorPauta(pautaId))
            .expectNext(voto1)
            .expectNext(voto2)
            .verifyComplete();
    }

    @Test
    void contabilizarVotosPorPauta_DeveRetornarResultadoCorreto() {
        // Arrange
        String pautaId = "1";
        
        when(votoRepository.countByPautaIdAndOpcaoVoto(eq(pautaId), eq(VotoOpcao.SIM))).thenReturn(Mono.just(3L));
        when(votoRepository.countByPautaIdAndOpcaoVoto(eq(pautaId), eq(VotoOpcao.NAO))).thenReturn(Mono.just(2L));
        
        Map<String, Long> resultadoEsperado = new HashMap<>();
        resultadoEsperado.put("sim", 3L);
        resultadoEsperado.put("nao", 2L);
        resultadoEsperado.put("total", 5L);
        
        // Act & Assert
        StepVerifier.create(votoService.contabilizarVotosPorPauta(pautaId))
            .expectNextMatches(resultado -> 
                resultado.get("sim").equals(3L) &&
                resultado.get("nao").equals(2L) &&
                resultado.get("total").equals(5L))
            .verifyComplete();
    }
}
