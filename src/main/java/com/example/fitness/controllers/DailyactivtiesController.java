package com.example.fitness.controllers;
import com.example.fitness.entitties.Daily_activities;
import com.example.fitness.services.DailyActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RequestMapping("/activity")
@RestController
public class DailyactivtiesController {
    private final DailyActivityService dailyactivityservice;
    public DailyactivtiesController(DailyActivityService dailyActivityService) {
        this.dailyactivityservice = dailyActivityService;
    }
    @GetMapping("/all")
    public ResponseEntity<List<Daily_activities>> allDailyActivities() {
        List<Daily_activities> daily_activities = dailyactivityservice.fetchAll();
        return ResponseEntity.ok(daily_activities);
    }
    @PostMapping("/addactivity")
    public void addactivity(@RequestBody Daily_activities daily_activities){
        dailyactivityservice.addDailyActivities(daily_activities.getUser_id(),
                daily_activities.getSteps(), daily_activities.getSleep_hours(),
                daily_activities.getMood(),daily_activities.getNotes()
                ,daily_activities.getWater_intake(),daily_activities.getCalories_burned());
    }

    @DeleteMapping("/deleteactivity/{id}")
    public void deleteadctivitybyId(@PathVariable("id") Integer id) {
        dailyactivityservice.deleteActivitiesById(id);
    }
}
