package shared.sample.primary.shareddao;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Arrays;

import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;

public class NameDecider implements TableNameDecider {

    @Override
    public List<String> decideNames(Map<String, Object> paramMap) {
        int type = (int) paramMap.get("0");
        if (type == 1)
            return Collections.singletonList("person_1");
        else if (type == 2)
            return Collections.singletonList("person_2");
        else if (type == 3)
            return Arrays.nonNullElementsIn(Arrays.array("person_1", "person_2"));
        else
            return Collections.emptyList();
    }

}
