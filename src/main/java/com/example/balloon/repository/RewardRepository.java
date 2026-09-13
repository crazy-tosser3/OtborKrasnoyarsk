package com.example.balloon.repository;

import com.example.balloon.model.entity.RewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<RewardEntity, String> {

    List<RewardEntity> findByUserNameIgnoreCase(String username);

    long countByUserName(String userName);
}
