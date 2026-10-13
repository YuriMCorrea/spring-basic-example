package one.digitalinnovation.gof.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import one.digitalinnovation.gof.dto.ClienteDTO;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 *
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {

	// Singleton: Injetar os componentes do Spring com @Autowired.
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private ViaCepService viaCepService;

	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		// Buscar todos os Clientes.
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		// Buscar Cliente por ID.
		Optional<Cliente> cliente = clienteRepository.findById(id);
		return cliente.isPresent() ? cliente.get() : null;
	}

	@Override
	public Cliente inserir(ClienteDTO cliente) {
		return salvarClienteComCep(cliente);
	}

	@Override
	public Cliente atualizar(Long id, Cliente cliente) {
		// Buscar Cliente por ID, caso exista:
		Optional<Cliente> clienteBd = Optional.of(clienteRepository.findById(id).orElseThrow());
		var toUpdate = clienteBd.get();
		Endereco endereco = buscarEnderecoViaCep(cliente.getEndereco().getCep());
		toUpdate.setEndereco(endereco);
		return clienteRepository.save(toUpdate);
	}

	@Override
	public void deletar(Long id) {
		// Deletar Cliente por ID.
		clienteRepository.deleteById(id);
	}

	@Override
	public List<Cliente> buscarPorEstado(String uf) {
		if (uf.length() != 2) throw new RuntimeException("UF invalido.");
		List<Endereco> enderecos =  enderecoRepository.findByUf(uf);
		List<Cliente> clientes = new ArrayList<>();
        for (Endereco e : enderecos) {
            clientes.addAll(clienteRepository.findByEndereco(e));
        }
		return clientes;
    }

	private Cliente salvarClienteComCep(ClienteDTO cliente) {
		// Verificar se o Endereco do Cliente já existe (pelo CEP).
		Endereco endereco = buscarEnderecoViaCep(cliente.cep());
		Cliente novoCliente = new Cliente();
		novoCliente.setNome(cliente.name());
		novoCliente.setEndereco(endereco);
		// Inserir Cliente, vinculando o Endereco (novo ou existente).
		return clienteRepository.save(novoCliente);
	}

	private Endereco buscarEnderecoViaCep(String cep) {
		return enderecoRepository.findById(cep).orElseGet(() -> {
			// Caso não exista, integrar com o ViaCEP e persistir o retorno.
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});
	}

}
