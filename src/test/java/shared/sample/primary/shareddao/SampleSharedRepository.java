package shared.sample.primary.shareddao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
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
    List<IPerson> findPersonProjection(@DeciderParam(value = "type", bindable = false) int type, Pageable pageable);

    @SharedQuery(value = "select count(*) as c from $TABLE$")
    Long findPersonProjection(@DeciderParam(value = "type", bindable = false) int type, TableNameDecider decider);

    @SharedQuery(value = "select id as id from $TABLE$")
    List<Long> findPersonIds();

    @SharedQuery(value = "select id as id from $TABLE$ where id=:id")
    List<Long> findPersonIdsWithCondition(@Param("id") long id, @DeciderParam(bindable = false) int type,
            TableNameDecider decider);

    @SharedQuery(value = "select id as id from $TABLE$ where id=:id")
    List<Long> findPersonIdsWithBindable(@DeciderParam("id") long type, TableNameDecider decider);

    @Modifying
    @SharedQuery("insert into $TABLE$ (id, name) values(:id, :name)")
    int savePerson(@Param(value = "id") long id, @Param(value = "name") String name, TableNameDecider decider);

}
