package com.skala.springbootsample.service;

import com.skala.springbootsample.domain.User;
import com.skala.springbootsample.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 모든 사용자 조회 (이름 필터 옵션)
    public List<User> findAll(Optional<String> name) {
        if (name.isPresent()) {
            return userRepository.findByNameIgnoreCase(name.get());
        }
        return userRepository.findAll();
    }

    // ID로 사용자 조회
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // 사용자 생성
    @Transactional
    public User create(User user) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    // 사용자 수정
    @Transactional
    public Optional<User> update(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(updatedUser.getName());

                    // 이메일 변경 시 중복 체크 (자기 자신 제외)
                    if (!user.getEmail().equals(updatedUser.getEmail())) {
                        if (userRepository.existsByEmail(updatedUser.getEmail())) {
                            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + updatedUser.getEmail());
                        }
                        user.setEmail(updatedUser.getEmail());
                    }

                    return userRepository.save(user);
                });
    }

    // 사용자 삭제
    @Transactional
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
