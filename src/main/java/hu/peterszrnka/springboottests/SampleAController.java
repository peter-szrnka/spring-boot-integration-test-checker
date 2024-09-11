package hu.peterszrnka.springboottests;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleAController implements BaseController {

    @GetMapping("/one")
    public ResponseEntity<String> one() {
        return ResponseEntity.ok("OK");
    }
}