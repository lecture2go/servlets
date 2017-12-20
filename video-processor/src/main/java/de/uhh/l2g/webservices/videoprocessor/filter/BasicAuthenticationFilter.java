package de.uhh.l2g.webservices.videoprocessor.filter;

import de.uhh.l2g.webservices.videoprocessor.filter.BasicAuthenticationFilter.Secured;
import de.uhh.l2g.webservices.videoprocessor.util.Config;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.NameBinding;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.internal.util.Base64;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The AuthenticationFilter provides a possibility to secure parts of the API
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthenticationFilter implements ContainerRequestFilter {

	private static Config config = Config.getInstance();
	private static String basicAuthUsername = config.getProperty("basicauth.user");
	private static String basicAuthPassword = config.getProperty("basicauth.pass");
	
    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Secured {}
    

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		// get the HTTP Authorization header from the request
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(authorizationHeader == null || authorizationHeader == "") {
        	throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        if (!authorizationHeader.startsWith("Basic ")) {
        	throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        // get only encoded username and password without the Prefix
        String encodedUserAndPassword = authorizationHeader.substring("Basic ".length());
        
        // decode username and password
        String usernameAndPassword = new String(Base64.decode(encodedUserAndPassword.getBytes()));;
         
        String[] usernameAndPasswordSplit = usernameAndPassword.split(":");
        if (usernameAndPasswordSplit.length < 2) {
        	throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        String username = usernameAndPasswordSplit[0];
        String password = usernameAndPasswordSplit[1];

        if ((username == null) || (password == null)) {
        	throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        if (username.equals(basicAuthUsername) && password.equals(basicAuthPassword)) {
        	// user successfully authenticated
        } else {
        	throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
	}
}
