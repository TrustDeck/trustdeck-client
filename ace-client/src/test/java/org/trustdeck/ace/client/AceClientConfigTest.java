package com.connector;

import org.junit.jupiter.api.Test;
import org.trustdeck.ace.client.config.AceClientProperties;
import org.trustdeck.ace.client.config.AceClientConfig;
import org.trustdeck.ace.client.dto.DomainDto;
import org.trustdeck.ace.client.service.DomainConnector;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AceClientConfigTest {

    private DomainConnector domainConnector;

    private static final String EXISTING_TEST_DOMAIN = "TestStudie-KI";



    @BeforeEach
    void setUp() {
        AceClientProperties props = new AceClientProperties(
                "http://localhost:8080",
                "http://localhost:8081",
                "development",
                "ace",
                "1h6T3Dnx45hrd4pgv7YdcIfP9GRarbpN",
                "test",
                "test"
        );
        domainConnector = AceClientConfig.createDomainConnector(props);
        assertNotNull(domainConnector);

    }
    // Comment-  change var to specific type

    @Test
    void testGetAllDomains() {
        assertDoesNotThrow(() -> {
            var response = domainConnector.getAllDomains();
            assertNotNull(response);
            System.out.println(response);
        });

    }

//    @Test
//    void testGetAllDomains() {
//        assertDoesNotThrow(() -> {
//            ResponseEntity<DomainDto[]> response = domainConnector.getAllDomains();
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK, response.getStatusCode());
//            DomainDto[] domains = response.getBody();
//            assertNotNull(domains);
//            assertTrue(domains.length > 0, "Expected at least one domain");
//            System.out.println(Arrays.toString(domains)); // Print readable array
//        });
//    }

    @Test
    void testGetDomain() {
        assertDoesNotThrow(() -> {
            DomainDto domain = domainConnector.getDomain(EXISTING_TEST_DOMAIN);
            assertNotNull(domain);
            System.out.println("RESPONSE TEST_GET_DOMAIN : " + domain);
        });
    }

    @Test
    void testGetDomainAttribute() {
        assertDoesNotThrow(() -> {
            var domain = domainConnector.getDomainAttribute(EXISTING_TEST_DOMAIN, "id");
            assertNotNull(domain);
        });
    }

    @Test
    void testCreateUpdateDeleteDomain() {

        DomainDto newDomain = new DomainDto();
        String newDomainName = "TestDomain-Create" + System.currentTimeMillis();
        String updateTestDomainName = "TestDomain-Update" + System.currentTimeMillis();
        newDomain.setName(newDomainName);
        newDomain.setPrefix("TDT11-");
        newDomain.setSuperDomainName("TestStudie");


        DomainDto domainToUpdate = new DomainDto();
        domainToUpdate.setName(updateTestDomainName);


        assertDoesNotThrow(() -> {

            // Create domain
            var createdDomain = domainConnector.createDomain(newDomain);
            assertNotNull(createdDomain);


            // Update domain
            var updatedDomain = domainConnector.updateDomain(newDomainName, domainToUpdate);
            assertNotNull(updatedDomain);


            // delete domain
            domainConnector.deleteDomain(updateTestDomainName, true);

        });
    }

    @Test
    void testCreateUpdateDeleteDomainComplete() {
        String newDomainName = "TestDomain-Create-Complete" + System.currentTimeMillis();
        String updateTestDomainName = "TestDomain-Update-Complete" + System.currentTimeMillis();
        DomainDto newDomain = new DomainDto();
        newDomain.setName(newDomainName);
        newDomain.setPrefix("TD_CUDC-");
        newDomain.setSuperDomainName("TestStudie");

        DomainDto domainToUpdate = new DomainDto();
        domainToUpdate.setName(updateTestDomainName);

        assertDoesNotThrow(() -> {
            // Create Domain Complete
            var createdDomain = domainConnector.createDomainComplete(newDomain);
            assertNotNull(createdDomain);

            // Update Domain Complete
            var updatedDomain = domainConnector.updateDomainComplete(newDomainName, domainToUpdate ,false);
            assertNotNull(updatedDomain);

            // Delete domain
            domainConnector.deleteDomain(updateTestDomainName, true);
        });
    }



    @Test
    void testUpdateSalt() {
        String testSalt = "TestSalt" + System.currentTimeMillis();
        assertDoesNotThrow(() -> {

            var updatedDomain = domainConnector.updateSalt(EXISTING_TEST_DOMAIN, testSalt, false);
            assertNotNull(updatedDomain);
        });
    }
}