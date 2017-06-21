package de.uhh.l2g.webservices.videoprocessor.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;

import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;

@Logged
@Provider
public class LoggingFilter implements ContainerRequestFilter {

	@NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Logged {}
	
	private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		//ContainerRequest request = (ContainerRequest) requestContext;
		String method = requestContext.getMethod();
		String path = requestContext.getUriInfo().getPath();
		logger.info("HTTP Request: {} {}",method, path);
		
		if (requestContext instanceof ContainerRequest) {
            ContainerRequest request = (ContainerRequest) requestContext;

            if (requestContext.hasEntity()) {
                request.bufferEntity();
                String entity = request.readEntity(String.class);
        		logger.info("Entity: {}", entity);

            }
        }
		
		
		//String entity = IOUtils.toString(requestContext.getEntityStream(), StandardCharsets.UTF_8.name());
		
	}
	
}
