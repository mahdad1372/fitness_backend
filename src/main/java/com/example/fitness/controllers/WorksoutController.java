package com.example.fitness.controllers;
import com.example.fitness.entitties.Goals;
import com.example.fitness.entitties.Workouts;
import com.example.fitness.services.Complexlogic;
import com.example.fitness.services.WorkoutsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RequestMapping("/worksout")
@RestController
public class WorksoutController {
    private final WorkoutsService workoutsService;
    private Complexlogic complexlogic;
    public WorksoutController(WorkoutsService workoutsService, Complexlogic complexlogic) {
        this.workoutsService = workoutsService;
        this.complexlogic = complexlogic;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
        public ResponseEntity<List<Workouts>> allDailyActivities() {
        List<Workouts> dailyworksout = workoutsService.allDailyActivities();
        return ResponseEntity.ok(dailyworksout);
    }

    @GetMapping("/performance")
//    @PreAuthorize("#id == authentication.principal.user_id")
    public ResponseEntity<Map<String, Object>> DailyActivities(@RequestBody Workouts workouts) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        List<Workouts> dailyworksout = workoutsService.fetchWorkoutsByday(
                formatter.format(workouts.getCreatedAt()),workouts.getUser_id());
        List<Workouts> weekworksout = workoutsService.fetchWorkoutsByweek(
                formatter.format(workouts.getCreatedAt()),workouts.getUser_id());
        Integer totalWorkouts = dailyworksout.size();
        Integer workoutsThisWeek = weekworksout.size();
        float totalDuration = 0;
        float totalCalories = 0;
        for (Workouts w : dailyworksout) {
            totalCalories += w.getCalories_burned();
        }
        for (Workouts w : dailyworksout) {
            totalDuration += w.getDuration();
        }
        String performancescore_massage = complexlogic.performance(totalDuration,totalWorkouts,totalCalories,workoutsThisWeek);
        Map<String, Object> response = Map.of(
                "message", performancescore_massage
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/dynamicrest")
    public Map<String, Object> dynamicRest(
            @RequestParam int repsCompleted,
            @RequestParam int targetReps,
            @RequestParam double weight,
            @RequestParam(required = false) Integer rpe,
            @RequestParam(required = false) Double estimated1RM,
            @RequestParam(required = false) Integer baseRest,
            @RequestParam int worksout_id
    ) {

        int restSeconds = workoutsService.calculateRest(
                repsCompleted, targetReps, weight, estimated1RM, rpe, baseRest,worksout_id
        );

        return Map.of(
                "recommendedRest", restSeconds,
                "unit", "seconds"
        );
    }

    @PostMapping("/addworksout")
    public void addworksout(@RequestBody Workouts workouts){
        workoutsService.addDailyWorkouts(workouts.getUser_id(),workouts.getType(),workouts.getDuration(),
                workouts.getCalories_burned(),workouts.getRest_seconds(),workouts.getRpe(),workouts.getIntensity_percent());
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteactivity/{id}")
    public void deleteworksoutById(@PathVariable("id") Integer id) {
        workoutsService.deleteWorksOutsById(id);
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<Workouts>> getWorksOutsById(@PathVariable("id") Integer id) {
        List<Workouts> DailyWorksOutsById = workoutsService.getWorksOutsById(id);
        return ResponseEntity.ok(DailyWorksOutsById);
    }

    @GetMapping("/getworkouts/{id}")
    public ResponseEntity<List<Workouts>> getWorksOutsByUserId(@PathVariable("id") Integer id) {
        List<Workouts> DailyWorksOutsById = workoutsService.getWorksOutsByWorkoutsId(id);
        return ResponseEntity.ok(DailyWorksOutsById);
    }

    @PutMapping("/updateworkout/{id}")
    public ResponseEntity<String> updateFood(
            @PathVariable("id") Integer id, @RequestBody Workouts workouts
    ) {
        workoutsService.update_workout(
                id, workouts.getType(),workouts.getDuration(),workouts.getCalories_burned(),workouts.getRest_seconds(), workouts.getRpe(), workouts.getIntensity_percent());

        return ResponseEntity.ok("Food updated successfully");
    }
}
