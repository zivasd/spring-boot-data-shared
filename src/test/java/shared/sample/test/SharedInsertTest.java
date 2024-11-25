package shared.sample.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import shared.sample.primary.dto.IPerson;
import shared.sample.primary.shareddao.SampleSharedRepository;
import shared.sample.secondary.shareddao.SampleSharedRepository2;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class SharedInsertTest {
    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcOperations primaryOperations;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcOperations secondaryJdbcOperations;

    @Autowired
    private SampleSharedRepository shared;

    @Autowired
    private SampleSharedRepository2 shared2;

    @BeforeAll
    void init() {
        primaryOperations.execute("create table person_1 (id varchar(64), name varchar(64))");
        primaryOperations.execute("insert into person_1 (id, name) values('1','bob')");

        primaryOperations.execute("create table person_2 (id varchar(64), name varchar(64))");
        primaryOperations.execute("insert into person_2 (id, name) values('1','tom')");

        secondaryJdbcOperations.execute("insert into t_user (id, name) values('1','李明')");

        secondaryJdbcOperations.execute("create table t_user_1 (id varchar(64), name varchar(64))");
        secondaryJdbcOperations.execute("insert into t_user_1 (id, name) values('1','李明')");
    }

    @AfterAll
    void clear() {
        primaryOperations.execute("drop table person_1");
        primaryOperations.execute("drop table person_2");
        secondaryJdbcOperations.execute("drop table t_user");
    }

    @Test
    @Transactional
    void insertPersonTest() {
        shared.savePerson(10, "ziva", properties -> {
            return Collections.singletonList("person_2");
        });

        List<IPerson> persons = shared.findPersonProjection(3, (Pageable) null);
        List<String> names = persons.stream().map(IPerson::getName).collect(Collectors.toList());
        assertTrue(names.contains("ziva"));

    }
}
