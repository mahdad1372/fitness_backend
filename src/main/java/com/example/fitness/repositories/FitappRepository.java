package com.example.fitness.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FitappRepository {
    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO Workouts (code, grant_type, redirect_uri, client_id) " +
                    "VALUES (?1, ?2, ?3, ?4)",
            nativeQuery = true
    )
    void gettokenFitapi(String code, String grant_type, String redirect_uri, String client_id);
}
