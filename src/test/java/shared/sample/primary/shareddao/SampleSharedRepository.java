package shared.sample.primary.shareddao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import io.github.zivasd.spring.boot.data.shared.repository.SharedQuery;
import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;
import shared.sample.primary.dto.IPerson;

@Repository
@Validated
public interface SampleSharedRepository extends SharedRepository {
    @SharedQuery(value = "select name from $TABLE$", tableNameDecider = NameDecider.class)
    List<IPerson> findPersonProjection(int type, Pageable pageable);
}
