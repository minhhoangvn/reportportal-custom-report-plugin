package com.toilatester.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    public ResponseEntity<String> handleRequests(@AuthenticationPrincipal ReportPortalUser user) {

        return ResponseEntity.status(HttpStatus.OK)
                .body("Call dummy api extends from user " + user.getUsername());
    }

}
