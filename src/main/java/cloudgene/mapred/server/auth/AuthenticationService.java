package cloudgene.mapred.server.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class AuthenticationService {
	private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

	private static final String MESSAGE_VALID_API_TOKEN = "API Token was created by %s and is valid.";

	private static final String MESSAGE_INVALID_API_TOKEN = "Invalid API Token.";

	@Inject
	protected Application application;

	@Inject
	protected JwtTokenGenerator generator;

	@Inject
	protected JwtTokenValidator validator;

	public static String ATTRIBUTE_TOKEN_TYPE = "token_type";

	public static String ATTRIBUTE_API_HASH = "api_hash";

	public User getUserByAuthentication(Authentication authentication) {
		return getUserByAuthentication(authentication, AuthenticationType.ACCESS_TOKEN);
	}

	public User getUserByAuthentication(Authentication authentication, AuthenticationType authenticationType) {
		log.warn("Database initialized: " + application.getDatabase());
		User user = null;
		log.warn("entered.");


		if (authentication == null) {
			log.warn("enterNull");
			UserDao userDao = new UserDao(application.getDatabase());
			user = userDao.findByUsername("Public");
			log.warn("authentifcate" + user.getUsername());
			return user ;
			    	}
		if (authentication != null) {
			UserDao userDao = new UserDao(application.getDatabase());
			user = userDao.findByUsername(authentication.getName());
			Map<String, Object> attributes = authentication.getAttributes();

			if (attributes.containsKey(ATTRIBUTE_TOKEN_TYPE)) {

				String tokenType = attributes.get(ATTRIBUTE_TOKEN_TYPE).toString();

				if (tokenType.equalsIgnoreCase(AuthenticationType.ACCESS_TOKEN.toString())) {

					if (authenticationType == AuthenticationType.ACCESS_TOKEN
							|| authenticationType == AuthenticationType.ALL_TOKENS) {
						return user;
					}

				}

			} else {

				if (authenticationType == AuthenticationType.ACCESS_TOKEN
						|| authenticationType == AuthenticationType.ALL_TOKENS) {
					return user;
				}

			}

			throw new AuthorizationException(authentication);

		} 
		throw new AuthenticationException();

	}

}
