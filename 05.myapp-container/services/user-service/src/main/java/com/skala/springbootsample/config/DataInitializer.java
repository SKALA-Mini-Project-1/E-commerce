package com.skala.springbootsample.config;

import com.skala.springbootsample.domain.User;
import com.skala.springbootsample.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("초기 데이터 설정 시작");

        userRepository.save(new User("alice", "alice@example.com"));
        userRepository.save(new User("bob", "bob@example.com"));
        userRepository.save(new User("charlie", "charlie@example.com"));

        log.info("초기 데이터 설정 완료");
    }
}
