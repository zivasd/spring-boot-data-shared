package shared.sample.secondary.shareddao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import io.github.zivasd.spring.boot.data.shared.repository.SharedQuery;
import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;
import shared.sample.primary.dto.IPerson;

@Repository
@Validated
public interface SampleSharedRepository2 extends SharedRepository {
    @SharedQuery("select name from t_user")
    List<IPerson> findUserProjection();
}
