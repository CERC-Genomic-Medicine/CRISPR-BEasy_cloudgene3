package cloudgene.mapred.util.auth;

import java.util.Date;

import org.reactivestreams.Publisher;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

	public static final int MAX_LOGIN_ATTEMMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Inject
	protected Application application;

	@Override
	public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest,
			AuthenticationRequest<?, ?> authenticationRequest) {

		return Mono.<AuthenticationResponse>create(emitter -> {

			String loginUsername = authenticationRequest.getIdentity().toString();
			String loginPassword = authenticationRequest.getSecret().toString();

			UserDao dao = new UserDao(application.getDatabase());
			User user = dao.findByUsername(loginUsername);

			if (user != null) {

				if (!user.isActive()) {
					emitter.error(AuthenticationResponse.exception());
					return;
				}

				if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMMPTS) {
					if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {

						emitter.error(AuthenticationResponse.exception());
						return;

					} else {
						// penalty time is over. set to zero
						user.setLoginAttempts(0);
					}
				}

				if (HashUtil.checkPassword(loginPassword, user.getPassword())) {

					user.setLoginAttempts(0);
					user.setLastLogin(new Date());
					dao.update(user);

					emitter.success(AuthenticationResponse.success("admin"));

				} else {

					// count failed logins
					int attempts = user.getLoginAttempts();
					attempts++;
					user.setLoginAttempts(attempts);

					// too many, lock user
					if (attempts >= MAX_LOGIN_ATTEMMPTS) {
						user.setLockedUntil(new Date(System.currentTimeMillis() + (LOCKING_TIME_MIN * 60 * 1000)));
					}
					dao.update(user);

					emitter.error(AuthenticationResponse.exception());
					return;
				}
			} else {
				emitter.error(AuthenticationResponse.exception());
				return;
			}

		});
	}
}