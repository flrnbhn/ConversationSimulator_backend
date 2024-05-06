package org.flbohn.conversationsimulator_backend.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "Sendet einen Teststring")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreiche Operation")
    })
    @GetMapping("")
    public String sendTestString() {
        return "Das ist ein Test";
    }
}
