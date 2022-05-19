package csID_tokenized.controllers;

import csID_tokenized.wrappers.*;
import csID_tokenized.web3.WhiteListHandler;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

@Service
public class CSIDRestTemplate {

    @Autowired
    private final RestTemplate restTemplate;
    private final String baseURL;
    private final HttpHeaders httpHeaders;

    @Autowired
    private final WhiteListHandler whiteListHandler;


    public CSIDRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.whiteListHandler = new WhiteListHandler();

        baseURL = "usl";
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }


    public ResponseEntity<AuthResponse> authenticate(CSIDRequest authRequest) {
        System.out.println(authRequest.getEndUserIp() + authRequest.getPersonalNumber());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CSIDRequest> authenticationReq = new HttpEntity<>(authRequest, httpHeaders);

        try {
            return restTemplate.postForEntity(baseURL + "/auth", authenticationReq, AuthResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<CollectResponse> collect(CollectRequest orderRef, String address) {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CollectRequest> request = new HttpEntity<>(orderRef, httpHeaders);

        try {

            CollectResponse collectResponse = restTemplate.postForEntity(baseURL + "/collect", request, CollectResponse.class).getBody();

            assert collectResponse != null;
            if (collectResponse.getStatus().equals("complete")) {
                String hashedPersonalNumber = DigestUtils.sha512Hex(collectResponse.getCompletionData().getUser().getPersonalNumber());
                whiteListHandler.outputHash(address);

            }
            return new ResponseEntity<>(collectResponse,HttpStatus.OK);


        } catch (HttpClientErrorException hcee) {
            String errorBody = hcee.getResponseBodyAsString();
            System.err.println("FAIL");
            System.out.println();
            if (errorBody.contains("No such order"))
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




@Configuration
static
class RestTemplateClass extends RestTemplate {

    @Lazy
    @Bean
    public RestTemplate restTemplate () throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream("src/main/resources/config/tls/FPTestcert3_20200618.p12"), "qwerty123".toCharArray());

        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.useProtocol("TLS");
        sslContextBuilder.loadKeyMaterial(clientStore, "qwerty123".toCharArray());
        sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}

}
