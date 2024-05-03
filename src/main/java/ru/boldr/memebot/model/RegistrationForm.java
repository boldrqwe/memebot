package ru.boldr.memebot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.boldr.memebot.model.entity.UserDetail;

@Data
@NoArgsConstructor
public class RegistrationForm {
    private String username;
    private String password;
    private String phoneNumber;

    public UserDetail toUser(PasswordEncoder passwordEncoder) {
        return new UserDetail(null, username,
                passwordEncoder.encode(password), phoneNumber);
    }
}
