package io.github.igormartins.msavaliadorcredito.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DadosSolicitacaoEmissaoCartao {
    private Long idCartao;
    private String cpg;
    private String endereco;
    private BigDecimal limiteLiberado;
}
