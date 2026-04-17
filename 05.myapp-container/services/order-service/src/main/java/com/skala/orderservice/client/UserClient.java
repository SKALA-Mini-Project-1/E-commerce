package com.skala.orderservice.client;

import com.skala.orderservice.common.exception.ExternalServiceException;
import com.skala.orderservice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${clients.user-service.base-url}")
    private String userServiceBaseUrl;

    public void assertUserExists(Long userId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    userServiceBaseUrl + "/api/users/" + userId,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("user-service", "get-user", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("user-service", "get-user", e);
        }
    }
}
