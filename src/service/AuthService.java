package service;

import repository.AccountRepository;
import java.util.Optional;

public class AuthService {
    private final AccountRepository accountRepository;

    public AuthService() {
        this.accountRepository = new AccountRepository();
    }

    public Optional<String> login(String username, String password) {
        // A real system would hash the provided password here and compare it with the DB hash.
        return accountRepository.getRole(username, password);
    }
}
