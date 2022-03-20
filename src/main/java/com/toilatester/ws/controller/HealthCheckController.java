package com.toilatester.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/toilatester/health")
public class HealthCheckController {

    @GetMapping(value = "/query")
    @ApiOperation("Get all data by custom query dashboard resources for specified project")
    public ResponseEntity<?> demo(@RequestParam(name = "launchId") String launchId,
                                  @RequestParam(name = "tagsAttribute") String tagsAttribute,
                                  @AuthenticationPrincipal ReportPortalUser user) {
        return ResponseEntity.status(HttpStatus.OK)
                .body("User " + user.getUsername() + " call demo api extends: " + launchId + " " + tagsAttribute);
    }

    @GetMapping(value = "/")
    @ApiOperation("Get all data by custom query dashboard resources for specified project")
    public ResponseEntity<?> ping(
            @AuthenticationPrincipal ReportPortalUser user) {
        return ResponseEntity.status(HttpStatus.OK)
                .body("User " + user.getUsername() + " call demo api extends");
    }
}
