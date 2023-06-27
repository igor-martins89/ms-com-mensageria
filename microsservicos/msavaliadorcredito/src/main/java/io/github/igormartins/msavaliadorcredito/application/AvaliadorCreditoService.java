package io.github.igormartins.msavaliadorcredito.application;

import feign.FeignException;
import io.github.igormartins.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.igormartins.msavaliadorcredito.application.ex.ErroComunicacaoMicrosservicesException;
import io.github.igormartins.msavaliadorcredito.domain.model.*;
import io.github.igormartins.msavaliadorcredito.infra.clients.CartaoResourceClient;
import io.github.igormartins.msavaliadorcredito.infra.clients.ClienteResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clientesClient;
    private final CartaoResourceClient cartoesCLient;

    public SituacaoCliente obterSituacaoCliente(String cpf) throws DadosClienteNotFoundException, ErroComunicacaoMicrosservicesException{

       try{
           ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosCliente(cpf);
           ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesCLient.getCartoesByCliente(cpf);

           return SituacaoCliente
                   .builder()
                   .cliente(dadosClienteResponse.getBody())
                   .cartoes(cartoesResponse.getBody())
                   .build();
       }
       catch(FeignException.FeignClientException e){
           int status = e.status();
           if(HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClienteNotFoundException();
           }
           throw new ErroComunicacaoMicrosservicesException(e.getMessage(), status);
       }
    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacaoMicrosservicesException{
       try{
           ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosCliente(cpf);
           ResponseEntity<List<Cartao>> cartoesResponse = cartoesCLient.getCartoesRendaAte(renda);

           List<Cartao> cartoes = cartoesResponse.getBody();
           var listaCartoesAprovados = cartoes.stream().map(cartao -> {

               DadosCliente dadosCliente = dadosClienteResponse.getBody();

               BigDecimal limiteBasico = cartao.getLimiteBasico();
               BigDecimal rendaBD = BigDecimal.valueOf(renda);
               BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
               var fator = idadeBD.divide(BigDecimal.valueOf(10));
               BigDecimal limiteAprovado = fator.multiply(limiteBasico);


               CartaoAprovado aprovado = new CartaoAprovado();
               aprovado.setCartao(cartao.getNome());
               aprovado.setBandeira(cartao.getBandeira());
               aprovado.setLimiteAprovado(limiteAprovado);

               return aprovado;
           }).collect(Collectors.toList());

           return new RetornoAvaliacaoCliente(listaCartoesAprovados);
       }
       catch(FeignException.FeignClientException e){
           int status = e.status();
           if(HttpStatus.NOT_FOUND.value() == status){
               throw new DadosClienteNotFoundException();
           }
           throw new ErroComunicacaoMicrosservicesException(e.getMessage(), status);
       }

    }
}
