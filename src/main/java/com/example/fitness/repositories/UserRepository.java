package com.example.fitness.repositories;
import com.example.fitness.entitties.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    @Query(value="SELECT * FROM Users a WHERE a.email=?1", nativeQuery=true)
    Optional<User> findByEmail(String email);
    @Query(value="SELECT * FROM Users a WHERE a.user_id=?1", nativeQuery=true)
    List<User> findByUser_id(Integer id);
    @Query(value = "SELECT * FROM Users" , nativeQuery = true)
    public List<User> getAllUsers();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Users WHERE user_id =?1", nativeQuery = true)
    public void deleteUsersById(Integer id);
    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO Users (email,firstname,gender,height,lastname,password,weight,role,age,smoke) " +
                            "values (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10)",
            nativeQuery = true)
    void addUser(String email,String firstname,String gender,Float height
    ,String lastname,String password,Float weight,String role,Integer age,Integer smoke);
    @Modifying
    @Transactional
    @Query(value = "UPDATE Users SET blood_pressure_data_source = ?2 WHERE user_id = ?1", nativeQuery = true)
    void updateBloodPressureDataSourceId(Integer userId, String dataSourceId);
    @Modifying
    @Transactional
    @Query(value = "UPDATE Users SET heart_Rate_Data_Source = ?2 WHERE user_id = ?1", nativeQuery = true)
    void updateHeartRateDataSource(Integer userId, String dataSourceId);

}
