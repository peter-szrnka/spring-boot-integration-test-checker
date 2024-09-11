package hu.peterszrnka.springboottests;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleBController implements BaseController {

    @GetMapping("/two")
    public ResponseEntity<String> two() {
        return ResponseEntity.ok("NO");
    }
}