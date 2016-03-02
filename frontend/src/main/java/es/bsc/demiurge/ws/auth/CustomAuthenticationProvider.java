package es.bsc.demiurge.ws.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	public CustomAuthenticationProvider() {
		
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// User / password
		String principal = authentication.getName();
		String credentials = (String)authentication.getCredentials();


		return null;
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return true;
	}
}
