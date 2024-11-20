package shared.sample.primary.shareddao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import io.github.zivasd.spring.boot.data.shared.query.DeciderParam;
import io.github.zivasd.spring.boot.data.shared.repository.SharedQuery;
import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;
import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;
import shared.sample.primary.dto.IPerson;

@Repository
@Validated
public interface SampleSharedRepository extends SharedRepository {
    @SharedQuery(value = "select name from $TABLE$", tableNameDecider = NameDecider.class)
    List<IPerson> findPersonProjection(@DeciderParam("type") int type, Pageable pageable);

    @SharedQuery(value = "select count(*) as c from $TABLE$")
    Long findPersonProjection(@DeciderParam("type") int type, TableNameDecider decider);
}
