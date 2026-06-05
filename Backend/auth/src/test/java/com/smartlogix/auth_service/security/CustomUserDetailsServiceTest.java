package com.smartlogix.auth_service.security;

import com.smartlogix.auth_service.model.UserCredential;
import com.smartlogix.auth_service.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserCredentialRepository repository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_whenUserExists_returnsSpringSecurityUser() {
        var credential = UserCredential.builder()
                .email("admin@smartlogix.cl")
                .password("encoded-password")
                .roles(Set.of("ADMIN", "OPERATOR"))
                .build();
        when(repository.findByEmail("admin@smartlogix.cl")).thenReturn(Optional.of(credential));

        var details = service.loadUserByUsername("admin@smartlogix.cl");

        assertThat(details.getUsername()).isEqualTo("admin@smartlogix.cl");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsUsernameNotFoundException() {
        when(repository.findByEmail("missing@smartlogix.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@smartlogix.cl"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@smartlogix.cl");
    }
}
