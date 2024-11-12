package shared.sample.secondary.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import shared.sample.secondary.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
