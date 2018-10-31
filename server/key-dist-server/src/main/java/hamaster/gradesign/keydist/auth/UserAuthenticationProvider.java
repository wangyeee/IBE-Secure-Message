package hamaster.gradesign.keydist.auth;

import static java.util.Objects.requireNonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import hamaster.gradesign.keydist.service.UserService;

@Component
public class UserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserService userService;

    public UserAuthenticationProvider(UserService userService) {
        this.userService = requireNonNull(userService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        UserDetails user = userService.loginWithEmail(username, authentication.getCredentials().toString());
        if (user == null)
            throw new AuthenticationException(String.format("Login failed for %s", username)) { private static final long serialVersionUID = 7839727064867305402L; };
        return user;
    }
}
