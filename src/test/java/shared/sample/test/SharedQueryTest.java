package shared.sample.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcOperations;

import shared.sample.primary.dto.IPerson;
import shared.sample.primary.shareddao.NameDecider;
import shared.sample.primary.shareddao.SampleSharedRepository;
import shared.sample.secondary.shareddao.SampleSharedRepository2;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class SharedQueryTest {

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

        secondaryJdbcOperations.execute("create table t_user (id varchar(64), name varchar(64))");
        secondaryJdbcOperations.execute("insert into t_user (id, name) values('1','李明')");
    }

    @Test
    void projectionListTest() {
        Pageable page = PageRequest.of(0, 10);
        List<IPerson> persons = shared.findPersonProjection(1, page);
        assertEquals("bob", persons.get(0).getName());

        persons = shared.findPersonProjection(2, (Pageable) null);
        assertEquals("tom", persons.get(0).getName());

        persons = shared.findPersonProjection(3, (Pageable) null);
        assertEquals(2, persons.size());
        List<String> names = persons.stream().map(IPerson::getName).collect(Collectors.toList());
        assertTrue(names.contains("bob"));
        assertTrue(names.contains("tom"));
    }

    @Test
    void projectionListTest1() {
        Long count = shared.findPersonProjection(1, new NameDecider());
        assertEquals(1, count);
    }

    @Test
    void projectionListTest2() {
        List<IPerson> persons = shared2.findUserProjection();
        assertEquals("李明", persons.get(0).getName());
    }
}
