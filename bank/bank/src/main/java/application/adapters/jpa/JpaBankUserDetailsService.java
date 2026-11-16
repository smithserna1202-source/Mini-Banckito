package application.adapters.jpa;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaBankUserDetailsService implements UserDetailsService {
    private final SpringDataBankUserRepository users;

    public JpaBankUserDetailsService(SpringDataBankUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findById(username)
            .filter(BankUserEntity::isEnabled)
            .orElseThrow(() -> new UsernameNotFoundException("Bank user not found or disabled"));
    }
}
