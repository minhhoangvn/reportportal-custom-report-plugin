package com.toilatester.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/toilatester")
public class ExtendsReportController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    LaunchRepository launchRepository;

    @Autowired
    TestItemRepository testItemRepository;

    @GetMapping(value = "/query")
    @ApiOperation("Get all data by custom query dashboard resources for specified project")
    public List<TestItem> demo(
            @RequestParam(name = "projectName") String projectName,
            @RequestParam(name = "launchName") String launchName,
            @RequestParam(name = "tagsAttribute") String tagsAttribute,
            @AuthenticationPrincipal ReportPortalUser user) {
        Optional<Project> project = projectRepository.findByName(projectName);
        Optional<Launch> launch = launchRepository.findLatestByNameAndProjectId(launchName,
                project.orElseThrow().getId());
        List<TestItem> testItems = testItemRepository.findTestItemsByLaunchId(launch.orElseThrow().getId());
        return testItems;
    }

    @GetMapping(value = "/")
    @ApiOperation("Get all data by custom query dashboard resources for specified project")
    public ResponseEntity<?> ping(
            @AuthenticationPrincipal ReportPortalUser user) {
        return ResponseEntity.status(HttpStatus.OK)
                .body("User " + user.getUsername() + " call demo api extends");
    }
}
