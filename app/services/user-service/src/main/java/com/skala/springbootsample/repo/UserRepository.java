package com.skala.springbootsample.repo;

import com.skala.springbootsample.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이름으로 사용자 검색 (대소문자 구분 없음)
    List<User> findByNameIgnoreCase(String name);

    // 이메일로 사용자 존재 여부 확인
    boolean existsByEmail(String email);

}
