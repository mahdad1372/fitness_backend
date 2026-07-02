package com.example.fitness.services;
import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.Goals;
import com.example.fitness.entitties.User;
import com.example.fitness.entitties.Workouts;
import com.example.fitness.repositories.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GoalService {
    private final GoalRepository Goalrepository;
    public GoalService(GoalRepository Goalrepository) {
        this.Goalrepository = Goalrepository;
    }
    public List<Goals> getAllGoals() {
        List<Goals> goals = new ArrayList<>();
        Goalrepository.getAllGoals().forEach(goals::add);
        return goals;
    }
    public List<Goals> fetchAll(){
        return Goalrepository.getAllGoals();
    }
    public List<Goals> displayinmainpage(){
        return Goalrepository.diplayinmainpage();
    }
    public Goals getGoalsById(Integer id){
        return Goalrepository.findGoalsByGoal_id(id);
    }
    public List<Goals> getGoalsByUserId(Integer id){
        return Goalrepository.findGoalsByuser_ids(id);
    }
    public void addGoals(
            Integer user_id, String goal_type, Float target_value, Float current_value, Date start_date,
            Date end_date, String status, byte[] before_goal_image, byte[] after_goal_image, Integer display_in_main_page
    ,String description) {
        System.out.println("Goal reach");
        Goalrepository.addgoals(
                user_id, goal_type, target_value, current_value, start_date, end_date, status,
                before_goal_image, after_goal_image,display_in_main_page,description
        );
    }
    public void deleteActivitiesById(Integer id){
        Goalrepository.deleteGoalsByGoal_id(id);
    }
    public void updateGoal(
            Integer goal_id,Integer user_id,String goal_type, Float target_value, Float current_value, Date start_date,
            Date end_date, String status, byte[] before_goal_image, byte[] after_goal_image) {

        Goalrepository.updateGoal(
                goal_id, user_id,goal_type, target_value, current_value, start_date, end_date, status,
                before_goal_image,
                after_goal_image
        );
    }
}
