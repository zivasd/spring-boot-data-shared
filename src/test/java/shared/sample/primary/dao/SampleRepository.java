package shared.sample.primary.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import shared.sample.primary.dto.IPerson;
import shared.sample.primary.entity.PlaceHolderEntity;

public interface SampleRepository extends Repository<PlaceHolderEntity, Long> {
    @Query(value = "SELECT name from person", nativeQuery = true)
    List<IPerson> findPerson();
}
