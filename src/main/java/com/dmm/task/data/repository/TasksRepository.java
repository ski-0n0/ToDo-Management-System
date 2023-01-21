package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dmm.task.data.entity.Tasks;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Integer> {
	
	@Query(value = "select * from tasks a where a.DATE between DATE(:from) AND DATE(:to)", nativeQuery=true)
	List<Tasks> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
	
	@Query(value = "select * from tasks a where a.DATE between DATE(:from) AND DATE(:to) AND name = :name", nativeQuery=true)
	List<Tasks> findByDateBetweenAndName(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("name") String name);

}
