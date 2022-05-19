package csID_tokenized.controllers;

import csID_tokenized.wrappers.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("csID")
public class CSIDController {

    private final CSIDRestTemplate CSIDRestTemplate;

    public CSIDController(CSIDRestTemplate CSIDRestTemplate) {
        this.CSIDRestTemplate = CSIDRestTemplate;
    }

    @RequestMapping(value = "/authenticate",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity <AuthResponse> csIDAuth(@RequestBody AuthRequest authRequest) {
        CSIDRequest CSIDRequest = new CSIDRequest("80.217.149.82", authRequest.getPersonalNumber());
        return CSIDRestTemplate.authenticate(CSIDRequest);
    }

    @RequestMapping (value = "/collect",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectResponse> csIDResultCollect(@RequestParam ("address") String address, @RequestBody CollectRequest collectRequest) {
        return CSIDRestTemplate.collect(collectRequest, address);
    }

}
