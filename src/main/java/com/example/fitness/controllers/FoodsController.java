package com.example.fitness.controllers;
import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.User;
import com.example.fitness.services.FoodsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/foods")
@RestController
public class FoodsController {
    private final FoodsService foodsService;
    public FoodsController(FoodsService foodsService) {
        this.foodsService = foodsService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Foods>> allFoods() {
        List<Foods> foods = foodsService.fetchAll();
        return ResponseEntity.ok(foods);
    }
    @PostMapping("/addfoods")
    public void addfood(@RequestBody Foods foods){
        foodsService.addFoods(foods.getUser_id(),foods.getFood_name(),foods.getCategory(),
                foods.getCalories(),foods.getProtein(),foods.getCarbohydrates(),foods.getFats(),foods.getNotes()
                ,foods.getMeal_time());

    }
    @GetMapping("/{id}")
    public ResponseEntity<List<Foods>> getUserById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(foodsService.findfoodsbyid(id));
    }
    @GetMapping("/food_user_id/{id}")
    public ResponseEntity<List<Foods>> getfoodById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(foodsService.findfoodsbyuserid(id));
    }
    @DeleteMapping("/deletefoods/{id}")
    public void deletefoodsbyId(@PathVariable("id") Integer id) {
        foodsService.deleteFoodsbyId(id);
    }
    @PutMapping("/updatefoods/{id}")
    public ResponseEntity<String> updateFood(
            @PathVariable("id") Integer id, @RequestBody Foods foods
    ) {
        foodsService.updateFood(
                id, foods.getFood_name(), foods.getCategory(), foods.getCalories(), foods.getProtein(),
                foods.getCarbohydrates(), foods.getFats(), foods.getNotes(), foods.getMeal_time()
        );

        return ResponseEntity.ok("Food updated successfully");
    }
}
