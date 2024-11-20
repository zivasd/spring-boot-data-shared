package shared.sample.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;

import shared.sample.primary.shareddao.SampleSharedRepository;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class DeciderParamTest {
    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcOperations primaryOperations;

    @Autowired
    private SampleSharedRepository shared;

    @BeforeAll
    void init() {
        primaryOperations.execute("create table person_1 (id varchar(64), name varchar(64))");
        primaryOperations.execute("insert into person_1 (id, name) values('1','bob')");

        primaryOperations.execute("create table person_2 (id varchar(64), name varchar(64))");
        primaryOperations.execute("insert into person_2 (id, name) values('1','tom')");
    }

    @Test
    void testNoParam() {
        List<Long> ids = shared.findPersonIds();
        assertEquals(0, ids.size());
    }

    @Test
    void testDeciderParam() {
        List<Long> ids = shared.findPersonIdsWithCondition(1, 1, p->{
            return Collections.singletonList("person_1");
        });
        assertEquals(1, ids.size());
    }

    @Test
    void testDeciderParamMultipleTable() {
        List<Long> ids = shared.findPersonIdsWithCondition(1, 1, p->{
            return Arrays.asList("person_1", "person_2");
        });
        assertEquals(2, ids.size());
    }

    @Test
    void testDeciderParamBindable() {
        List<Long> ids = shared.findPersonIdsWithBindable(1L, p->{
            return Arrays.asList("person_1", "person_2");
        });
        assertEquals(2, ids.size());
    }
}
