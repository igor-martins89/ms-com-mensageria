package io.github.igormartins.msclientes.application;

import io.github.igormartins.msclientes.domain.Cliente;
import io.github.igormartins.msclientes.infra.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional
    public Cliente save(Cliente cliente){
        return repository.save(cliente);
    }

    public Optional<Cliente> getByCPF (String cpf){
        return repository.getByCpf(cpf);
    }
}
