package one.digitalinnovation.gof.service;

import one.digitalinnovation.gof.dto.ClienteDTO;
import one.digitalinnovation.gof.model.Cliente;

import java.util.List;

/**
 * Interface que define o padrão <b>Strategy</b> no domínio de cliente. Com
 * isso, se necessário, podemos ter multiplas implementações dessa mesma
 * interface.
 * 
 * @author falvojr
 */
public interface ClienteService {

	Iterable<Cliente> buscarTodos();

	Cliente buscarPorId(Long id);

	Cliente inserir(ClienteDTO cliente);

	Cliente atualizar(Long id, Cliente cliente);

	void deletar(Long id);

	List<Cliente> buscarPorEstado(String uf);
}
