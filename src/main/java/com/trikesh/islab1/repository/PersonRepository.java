package com.trikesh.islab1.repository;

import com.trikesh.islab1.model.Color;
import com.trikesh.islab1.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Page<Person> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Person> findAllByOrderByNameAsc(Pageable pageable);

    @Query("SELECT SUM(p.height) FROM Person p WHERE p.height IS NOT NULL")
    Long sumHeight();

    @Query("SELECT COUNT(p) FROM Person p WHERE p.weight < :weight")
    Long countByWeightLessThan(@Param("weight") Integer weight);

    @Query("SELECT p FROM Person p WHERE p.birthday < :birthday")
    List<Person> findByBirthdayBefore(@Param("birthday") ZonedDateTime birthday);

    @Query("SELECT COUNT(p) FROM Person p WHERE p.hairColor = :hairColor")
    Long countByHairColor(@Param("hairColor") Color hairColor);

    @Query("SELECT COUNT(p) FROM Person p WHERE p.eyeColor = :eyeColor")
    Long countByEyeColor(@Param("eyeColor") Color eyeColor);

    @Query("SELECT COUNT(p) FROM Person p")
    Long countAll();
}
