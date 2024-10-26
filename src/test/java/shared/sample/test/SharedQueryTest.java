package shared.sample.test;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;

import shared.sample.primary.dto.IPerson;
import shared.sample.primary.shareddao.SampleSharedRepository;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class SharedQueryTest {

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcOperations operations;

    @Autowired
    private SampleSharedRepository shared;

    @BeforeAll
    void init() {
        operations.execute("create table person (id varchar(64), name varchar(64))");
        operations.execute("insert into person (id, name) values('1','bob')");
    }

    @Test
    void projectionListTest() {
        List<IPerson> persons = shared.findPersonProjection();
        System.out.println(persons.size());
    }
}
