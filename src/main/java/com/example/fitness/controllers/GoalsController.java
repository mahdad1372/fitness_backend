package com.example.fitness.controllers;

import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.Goals;
import com.example.fitness.services.GoalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RequestMapping("/goals")
@RestController
public class GoalsController {
    private final GoalService goalservice;
    public GoalsController(GoalService goalservice) {
        this.goalservice = goalservice;
    }
    @GetMapping("/all")
    public ResponseEntity<List<Goals>> allDailyActivities() {
        List<Goals> goals = goalservice.getAllGoals();
        return ResponseEntity.ok(goals);
    }
    @GetMapping("/mainpage")
    public ResponseEntity<List> diplaymainpage() {
        List goals = goalservice.displayinmainpage();
        return ResponseEntity.ok(goals);
    }
    @GetMapping("/before-image/{id}")
    public ResponseEntity<byte[]> getBeforeImage(
            @PathVariable Integer id
    ) {

        Goals goal = goalservice.getGoalsById(id);

        if (goal.getBeforeGoalImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(goal.getBeforeGoalImage());
    }
    @GetMapping("/after-image/{id}")
    public ResponseEntity<byte[]> getAfterImage(
            @PathVariable Integer id
    ) {

        Goals goal = goalservice.getGoalsById(id);

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(goal.getAfterGoalImage());
    }
    @PostMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("POST WORKS");
    }
    @PostMapping("/add")
    public void addGoal(
            @RequestParam Integer user_id,
            @RequestParam String goal_type,
            @RequestParam Float target_value,
            @RequestParam Float current_value,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date start_date,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date end_date,
            @RequestParam String status,
            @RequestParam(required = false)
            MultipartFile before_goal_image,
            @RequestParam(required = false)
            MultipartFile after_goal_image,
            @RequestParam(required = false)
            Integer display_in_main_page,
            @RequestParam(required = false)
            String description
    ) throws IOException {
        System.out.println("Goal controller");
        byte[] beforeImageBytes = before_goal_image.getBytes();
        byte[] afterImageBytes = after_goal_image.getBytes();

        goalservice.addGoals(
                user_id,
                goal_type,
                target_value,
                current_value,
                start_date,
                end_date,
                status,
                beforeImageBytes,
                afterImageBytes,
                display_in_main_page,
                description
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goals> getGoalById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(goalservice.getGoalsById(id));
    }
    @GetMapping("/findbyuserid/{id}")
    public ResponseEntity<List<Goals>> getUserById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(goalservice.getGoalsByUserId(id));
    }
    @DeleteMapping("/deletegoal/{id}")
    public void deletegoalsbyId(@PathVariable("id") Integer id) {
        goalservice.deleteActivitiesById(id);
    }
    @PutMapping("/updategoal/{id}")
    public ResponseEntity<String> updateGoal(
            @PathVariable Integer id,
            @RequestParam Integer user_id,
            @RequestParam String goal_type,
            @RequestParam Float target_value,
            @RequestParam Float current_value,

            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
            Date start_date,

            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
            Date end_date,

            @RequestParam String status,

            @RequestParam(required = false)
            MultipartFile before_goal_image,

            @RequestParam(required = false)
            MultipartFile after_goal_image
    ) throws IOException {

        byte[] beforeImage = null;
        byte[] afterImage = null;

        if (before_goal_image != null && !before_goal_image.isEmpty()) {
            beforeImage = before_goal_image.getBytes();
        }

        if (after_goal_image != null && !after_goal_image.isEmpty()) {
            afterImage = after_goal_image.getBytes();
        }
        System.out.println("before image = " + before_goal_image);
        System.out.println("after image = " + after_goal_image);
        System.out.println(before_goal_image.getOriginalFilename());
        System.out.println(after_goal_image.getOriginalFilename());
        goalservice.updateGoal(
                id,
                user_id,
                goal_type,
                target_value,
                current_value,
                start_date,
                end_date,
                status,
                beforeImage,
                afterImage
        );

        return ResponseEntity.ok("Goal updated successfully");
    }
}

