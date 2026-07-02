package com.example.fitness.services;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    public List<User> finduserbyid(Integer id) {
        List<User> users = new ArrayList<>();
        userRepository.findByUser_id(id).forEach(users::add);
        return users;
    }
    public Optional<User> finduserbyemail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user;
    }


    public List<User> fetchAll(){
        return userRepository.getAllUsers();
    }
    // Find user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public void adduser(String email,String firstname,String gender,Float height
            ,String lastname,String password,Float weight,String role,Integer age ,Integer smoke) {
        userRepository.addUser(email,firstname,gender,height,lastname,password,weight,role,age,smoke);

    }
    public void deleteuserbyId(Integer id){
        userRepository.deleteUsersById(id);
    }
}
