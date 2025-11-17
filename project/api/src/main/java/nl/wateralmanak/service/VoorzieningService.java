package nl.wateralmanak.service;

import nl.wateralmanak.model.Voorziening;
import nl.wateralmanak.repository.VoorzieningRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VoorzieningService {
    
    private final VoorzieningRepository repository = new VoorzieningRepository();

    public List<Voorziening> getAllVoorzieningen() throws SQLException {
        return repository.findAll();
    }

    public Optional<Voorziening> getVoorzieningById(UUID id) throws SQLException {
        return repository.findById(id);
    }

    public Voorziening createVoorziening(Voorziening voorziening) throws SQLException {
        validateVoorziening(voorziening);
        return repository.create(voorziening);
    }

    public Voorziening updateVoorziening(UUID id, Voorziening voorziening) throws SQLException {
        validateVoorziening(voorziening);
        
        if (!repository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Voorziening not found with id: " + id);
        }
        
        return repository.update(id, voorziening);
    }

    public boolean deleteVoorziening(UUID id) throws SQLException {
        return repository.delete(id);
    }

    private void validateVoorziening(Voorziening voorziening) {
        if (voorziening.getNaam() == null || voorziening.getNaam().trim().isEmpty()) {
            throw new IllegalArgumentException("Naam is required");
        }
        
        if (voorziening.getLongitude() == null || voorziening.getLatitude() == null) {
            throw new IllegalArgumentException("Location coordinates are required");
        }
        
        if (voorziening.getLongitude() < -180 || voorziening.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }
        
        if (voorziening.getLatitude() < -90 || voorziening.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }
    }
}
