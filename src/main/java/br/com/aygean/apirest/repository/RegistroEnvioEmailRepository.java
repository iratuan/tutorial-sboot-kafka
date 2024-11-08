package br.com.aygean.apirest.repository;

import br.com.aygean.apirest.model.RegistroEnvioEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroEnvioEmailRepository extends JpaRepository<RegistroEnvioEmail, Long> {
}
