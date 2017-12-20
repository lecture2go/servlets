package de.uhh.l2g.webservices.videoprocessor.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * This response filter is necessary to allow Cross-Origin Resource Sharing
 */
@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, accept, authorization");
        response.getHeaders().add("Access-Control-Expose-Headers", "Location");
    }
}