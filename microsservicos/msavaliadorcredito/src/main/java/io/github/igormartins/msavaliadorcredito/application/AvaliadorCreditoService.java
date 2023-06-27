package io.github.igormartins.msavaliadorcredito.application;

import feign.FeignException;
import io.github.igormartins.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.igormartins.msavaliadorcredito.application.ex.ErroComunicacaoMicrosservicesException;
import io.github.igormartins.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.igormartins.msavaliadorcredito.domain.model.DadosCliente;
import io.github.igormartins.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.igormartins.msavaliadorcredito.infra.clients.CartaoResourceClient;
import io.github.igormartins.msavaliadorcredito.infra.clients.ClienteResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
