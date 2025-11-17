package nl.wateralmanak.repository;

import nl.wateralmanak.config.DatabaseConfig;
import nl.wateralmanak.model.Voorziening;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VoorzieningRepository {

    public List<Voorziening> findAll() throws SQLException {
        List<Voorziening> voorzieningen = new ArrayList<>();
        String sql = "SELECT id, naam, beschrijving, ST_X(locatie) as longitude, " +
                    "ST_Y(locatie) as latitude, created_at, updated_at " +
                    "FROM wateralmanak.voorzieningen ORDER BY naam";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                voorzieningen.add(mapResultSetToVoorziening(rs));
            }
        }
        
        return voorzieningen;
    }

    public Optional<Voorziening> findById(UUID id) throws SQLException {
        String sql = "SELECT id, naam, beschrijving, ST_X(locatie) as longitude, " +
                    "ST_Y(locatie) as latitude, created_at, updated_at " +
                    "FROM wateralmanak.voorzieningen WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVoorziening(rs));
                }
            }
        }
        
        return Optional.empty();
    }

    public Voorziening create(Voorziening voorziening) throws SQLException {
        String sql = "INSERT INTO wateralmanak.voorzieningen (naam, beschrijving, locatie) " +
                    "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)) RETURNING id, created_at, updated_at";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voorziening.getNaam());
            stmt.setString(2, voorziening.getBeschrijving());
            stmt.setDouble(3, voorziening.getLongitude());
            stmt.setDouble(4, voorziening.getLatitude());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    voorziening.setId((UUID) rs.getObject("id"));
                    voorziening.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    voorziening.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                }
            }
        }
        
        return voorziening;
    }

    public Voorziening update(UUID id, Voorziening voorziening) throws SQLException {
        String sql = "UPDATE wateralmanak.voorzieningen " +
                    "SET naam = ?, beschrijving = ?, locatie = ST_SetSRID(ST_MakePoint(?, ?), 4326), " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? RETURNING updated_at";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voorziening.getNaam());
            stmt.setString(2, voorziening.getBeschrijving());
            stmt.setDouble(3, voorziening.getLongitude());
            stmt.setDouble(4, voorziening.getLatitude());
            stmt.setObject(5, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    voorziening.setId(id);
                    voorziening.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                }
            }
        }
        
        return voorziening;
    }

    public boolean delete(UUID id) throws SQLException {
        String sql = "DELETE FROM wateralmanak.voorzieningen WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Voorziening mapResultSetToVoorziening(ResultSet rs) throws SQLException {
        return new Voorziening(
            (UUID) rs.getObject("id"),
            rs.getString("naam"),
            rs.getString("beschrijving"),
            rs.getDouble("longitude"),
            rs.getDouble("latitude"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
