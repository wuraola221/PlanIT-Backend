package PlanIT.PlanIT.controller;

import PlanIT.PlanIT.dto.DeveloperTaskCount;
import PlanIT.PlanIT.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor

public class TeamController {

    private final TeamService teamService;

    @PreAuthorize( "hasAuthority('LEAD_DEVELOPER')")
    @GetMapping("/{leadEmail}/developers")
    public ResponseEntity<List<DeveloperTaskCount>> getDevelopersAndTaskCount(@PathVariable String leadEmail) {
        return ResponseEntity.ok(teamService.getDevelopersAndTaskCount(leadEmail));
    }
}
