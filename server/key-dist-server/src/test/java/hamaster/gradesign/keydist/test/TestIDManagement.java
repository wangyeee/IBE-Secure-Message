package hamaster.gradesign.keydist.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class TestIDManagement {

    @Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        assertNotNull(restTemplate);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> allSystemParameters = restTemplate.getForObject(String.format("%s/system/allparam", "http://localhost:8081"), Map.class);
        for (String key : allSystemParameters.keySet()) {
            System.out.println(String.format("Key[%s] = %s", key, allSystemParameters.get(key)));
            Map<String, String> base64EncodedParameter = allSystemParameters.get(key);
            for (String key1 : base64EncodedParameter.keySet()) {
                System.out.println(String.format("Key1[%s] = %s", key1, base64EncodedParameter.get(key1)));
            }
        }
    }
}
