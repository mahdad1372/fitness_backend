package com.example.fitness.services;
import com.example.fitness.entitties.Workouts;
import com.example.fitness.repositories.WorksoutsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class WorkoutsService {
    private final WorksoutsRepository worksoutsRepository;
    public WorkoutsService(WorksoutsRepository worksoutsRepository) {
        this.worksoutsRepository = worksoutsRepository;
    }
    public List<Workouts> allDailyActivities() {
        List<Workouts> workoutsList = new ArrayList<>();
        worksoutsRepository.getAllWorkouts().forEach(workoutsList::add);
        return workoutsList;
    }
    public List<Workouts> fetchAll(){
        return worksoutsRepository.getAllWorkouts();
    }
    public List<Workouts> getWorksOutsById(Integer id){
        return worksoutsRepository.findWorkoutsByUser_id(id);
    }
    public List<Workouts> getWorksOutsByWorkoutsId(Integer id){
        return worksoutsRepository.findWorkoutsByWorkouts_id(id);
    }

    public void addDailyWorkouts(Integer user_id, String type, Integer duration, Float calories_burned,
                                 Integer rest_seconds,Integer rpe,Float intensity_percent) {
        worksoutsRepository.addworkouts(user_id,type,duration,calories_burned,rest_seconds,rpe,intensity_percent);
    }
    public void deleteWorksOutsById(Integer id){
        worksoutsRepository.deleteWorkoutsByWorkout_id(id);
    }
    public List<Workouts> fetchWorkoutsByday(String created_at,Integer user_id) {
      return worksoutsRepository.findWorkoutsByday(created_at,user_id);
    };
    public List<Workouts> fetchWorkoutsByweek(String created_at,Integer user_id) {
        return worksoutsRepository.findWorkoutsByweek(created_at,user_id);
    };
    public int calculateRest(int repsCompleted, int targetReps, double weight, Double estimated1RM,
                             Integer rpe, Integer baseRest, Integer worksout_id) {
        // targetReps = the number of reps the user was supposed to do for that set.
        // repsCompleted = the number of reps the user actually performed in the set.
        // rpe: How hard the set of the exercise felt (1–10).
        // Estimated 1RM = the maximum weight the user can lift for 1 repetition for that specific exercise.
        // base rest: Default rest for the exercise
        // intensity_percent : It is the percentage of estimated 1RM (one-rep max) that the user lifted in a set. for example
        // if someone lifts 80 kg, and their estimated 1RM is 100 kg, then: 80%
        // fallback defaults
        if (estimated1RM == null || estimated1RM == 0) {
            estimated1RM = weight * (1 + repsCompleted / 30.0);
        }

        if (baseRest == null) {
            baseRest = 90; // default 90 seconds
        }

        int rest = baseRest;

        // calculate intensity
        double percent1RM = (weight / estimated1RM) * 100.0;

        // calculate performance
        double performanceRatio = targetReps > 0
                ? (double) repsCompleted / targetReps
                : 1.0;

        // intensity rules
        if (percent1RM >= 90) rest += 120;
        else if (percent1RM >= 80) rest += 60;
        else if (percent1RM >= 70) rest += 30;

        // performance rules
        if (performanceRatio < 0.7) rest += 30;

        // RPE rules
        if (rpe != null) {
            if (rpe >= 9) rest += 60;
            else if (rpe == 8) rest += 30;
            else if (rpe <= 6) rest -= 10;
        }
        double intensityPercent = (weight / estimated1RM) * 100.0;
        // enforce bounds
        rest = Math.max(30, Math.min(rest, 420));
//        worksoutsRepository.updateWorkoutsByWorkout_id(worksout_id,rest,rpe, (float) intensityPercent);
        return rest;
    }
    public void update_workout(Integer workout_id, String type, Integer duration, Float calories_burned,Integer rest_seconds,Integer rpe,Float intensity_percent)
    {
        worksoutsRepository.updateWorkoutsByWorkout_id(workout_id,type,duration,calories_burned,rest_seconds,rpe,intensity_percent);
    }
}
