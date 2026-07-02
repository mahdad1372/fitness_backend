package com.example.fitness.services;
import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.FoodsRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FoodsService {
    private final FoodsRepository foodsRepository;
    public FoodsService(FoodsRepository foodsRepository) {
        this.foodsRepository = foodsRepository;
    }
    public List<Foods> allFoods() {
        List<Foods> foods = new ArrayList<>();
        foodsRepository.findAll().forEach(foods::add);
        return foods;
    }
    public List<Foods> fetchAll(){
        return foodsRepository.getAllFoods();
    }
    public void addFoods(Integer user_id, String food_name, String category, Float calories, Float protein
            , Float carbohydrates , Float fats, String notes,String meal_time) {
        foodsRepository.addfoods(user_id,food_name,category,calories,protein,carbohydrates,fats,notes,meal_time);
        System.out.println("This is the id :");
    }
    public List<Foods> findfoodsbyid(Integer id) {
        return foodsRepository.findByFood_id(id);
    }
    public List<Foods> findfoodsbyuserid(Integer id){
        return  foodsRepository.findByUsers_id(id);
    }
    public void deleteFoodsbyId(Integer id){
        foodsRepository.deleteFoodsByFood_id(id);
    }
    public void updateFood(Integer food_id, String food_name, String category, Float calories,
            Float protein, Float carbohydrates, Float fats, String notes, String meal_time
    ) {
        foodsRepository.updateFood(food_id, food_name, category, calories, protein,
                carbohydrates, fats, notes, meal_time
        );
    }
}
