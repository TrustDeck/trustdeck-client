package org.trustdeck.client.service;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.util.TrustDeckRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.trustdeck.client.model.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Slf4j
public class DBMaintenance {
    /** Enables access to the configuration variables. */

    private TrustDeckClientConfig trustDeckClientConfig;

    /** Enables access to utility methods. */
    private TrustDeckRequestUtil util;
    /**
     * Clear all tables and vacuum them.
     *
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */
    public DBMaintenance(TrustDeckClientConfig config, TrustDeckRequestUtil util) {
        this.trustDeckClientConfig = config;
        this.util = util;
    }

    public void clearTables()  {


        String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url;

        url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/").pathSegment("api", "pseudonymization","table","pseudonym").toUriString();

        try {
            ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Clearing Pseudonym table failed: " + e.getMessage());
        }

        url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/").pathSegment("api", "pseudonymization","table", "domain").toUriString();
        try {
            ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Clearing Domain table failed: " + e.getMessage());
        }

        url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/").pathSegment("api", "pseudonymization","table", "auditevent").toUriString();
        try {
            ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            throw new TrustDeckClientLibraryException("Clearing Auditevent table failed: " + e.getMessage());
        }
    }

    /**
     * Remove roles that are not needed anymore.
     *
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     */

    public void deleteDomainRightsAndRoles(Domain domain) {
        String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/").pathSegment("api", "pseudonymization","roles", domain.getName()).toUriString();
        try{
            ResponseEntity<Void> respone = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch(Exception e){
            throw new TrustDeckClientLibraryException("Deleting roles failed: " + e.getMessage());
        }
    }


    /**
     * Get table storage usage.
     *
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */

    public String getStorage( String tableName) {
        String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/")? serviceUrl: serviceUrl + "/")
                .pathSegment("api", "pseudonymization","table", tableName, "storage").toUriString();
        try{
            ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), String.class);
            return response.getBody();
        } catch(Exception e){
            throw new TrustDeckClientLibraryException("Getting storage failed: " + e.getMessage());
        }
    }

}