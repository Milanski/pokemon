package com.mbls.challenge.pokemon.trainer.adapter.out.persistence;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import com.mbls.challenge.pokemon.trainer.domain.exception.EmailAlreadyRegisteredException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.domain.model.Email;
import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.domain.model.Username;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TrainerRepositoryAdapter implements TrainerRepository {

    private final SpringDataTrainerRepository springDataRepository;

    TrainerRepositoryAdapter(SpringDataTrainerRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Trainer save(Trainer trainer) {
        TrainerJpaEntity entity = new TrainerJpaEntity(
                trainer.id().value(),
                trainer.username().value(),
                trainer.email().value(),
                trainer.hashedPassword().value(),
                trainer.registeredAt()
        );

        try {
            springDataRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // Two registrations for the same username/email can both pass the
            // application-level pre-check and race to this insert; the unique
            // constraint is the actual authority here. Re-check to find out
            // which one collided and translate it back into the same domain
            // exception the pre-check would have thrown, instead of letting a
            // raw persistence exception surface as a 500.
            if (springDataRepository.existsByUsername(trainer.username().value())) {
                throw new UsernameAlreadyTakenException(trainer.username().value());
            }
            if (springDataRepository.existsByEmail(trainer.email().value())) {
                throw new EmailAlreadyRegisteredException(trainer.email().value());
            }
            throw e;
        }

        return trainer;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return springDataRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<Trainer> findById(TrainerId id) {
        return springDataRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    private Trainer toDomain(TrainerJpaEntity entity) {
        return Trainer.reconstitute(
                new TrainerId(entity.getId()),
                new Username(entity.getUsername()),
                new Email(entity.getEmail()),
                new HashedPassword(entity.getPasswordHash()),
                entity.getRegisteredAt()
        );
    }
}
