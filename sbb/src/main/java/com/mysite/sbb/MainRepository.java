package com.mysite.sbb;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MainRepository extends JpaRepository<Board, Integer>{

	List<Board> findAllByCategoryIdOrderByViewOrder(Integer categoryId);

}
