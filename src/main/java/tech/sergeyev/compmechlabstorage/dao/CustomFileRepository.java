package tech.sergeyev.compmechlabstorage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sergeyev.compmechlabstorage.model.CustomFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomFileRepository extends JpaRepository<CustomFile, UUID> {
    Optional<CustomFile> getCustomFileById(UUID id);
    Boolean existsByName(String name);
    Boolean existsByLocation(String location);
    @Query("select count (c) from CustomFile c")
    int countAll();
}
