package hu.peterszrnka.springboottests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestedClass(SampleAController.class)
class SampleAIntegrationTest implements BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @TestedMethod("one")
    void testOne() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:"+port+"/one", String.class);

        assertEquals("OK", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}