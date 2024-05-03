package ru.boldr.memebot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.boldr.memebot.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true, // Активирует @PreAuthorize и @PostAuthorize
        securedEnabled = true, // Активирует @Secured
        jsr250Enabled = true   // Активирует @RolesAllowed
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http.authorizeRequests()
//                .antMatchers("/thread/**").hasRole("USER")
//                .antMatchers("**/graphiql/**").permitAll()
//                .antMatchers("/", "/**").permitAll()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .defaultSuccessUrl("/pictures", true)
//                .permitAll()
//                .and()
//                .oauth2Login()
//                .loginPage("/login")
//                .and()
//                .logout()
//                .permitAll()
//                .and()
//                .build();
        return http.authorizeRequests().anyRequest().permitAll().and().csrf().disable().build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("user with name %s not found".formatted(username)));
    }
}