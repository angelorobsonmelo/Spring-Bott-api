package com.algaworks.algamoneyapi.algamoneyapi.resource;

import com.algaworks.algamoneyapi.algamoneyapi.event.RecursoCriadoEvent;
import com.algaworks.algamoneyapi.algamoneyapi.exceptionhandler.AlgaMoneyExceptionHandler;
import com.algaworks.algamoneyapi.algamoneyapi.model.Lancamento;
import com.algaworks.algamoneyapi.algamoneyapi.service.LancamentoService;
import com.algaworks.algamoneyapi.algamoneyapi.service.exception.PessoaInexistenteOuInativaException;
import jdk.net.SocketFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

    @Autowired
    LancamentoService lancamentoService;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<Lancamento> salvar(@RequestBody @Valid Lancamento lancamento, HttpServletResponse response){
        Lancamento lancamentoSalvo = lancamentoService.salvar(lancamento);

        publisher.publishEvent(new RecursoCriadoEvent(this, response, lancamentoSalvo.getCodigo()));
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoSalvo);
    }

    @GetMapping
    public ResponseEntity<List<Lancamento>> listar(){
        List<Lancamento> lancamentos = lancamentoService.listar();
        return ResponseEntity.ok(lancamentos);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Lancamento> buscarLancamentoPeloCodigo(@PathVariable Long codigo){
        Lancamento lancamento = lancamentoService.buscarLancamentoPeloCodigo(codigo);
        return lancamento != null ? ResponseEntity.ok(lancamento) : ResponseEntity.notFound().build();
    }

    @ExceptionHandler({ PessoaInexistenteOuInativaException.class })
    public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex) {
        String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null, LocaleContextHolder.getLocale());
        String mensagemDesenvolvedor = ex.toString();
        List<AlgaMoneyExceptionHandler.Erro> erros = Arrays.asList(new AlgaMoneyExceptionHandler.Erro(mensagemUsuario, mensagemDesenvolvedor));
        return ResponseEntity.badRequest().body(erros);
    }

}