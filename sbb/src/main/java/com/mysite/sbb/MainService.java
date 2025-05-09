package com.mysite.sbb;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MainService {
	
	private final MainRepository mainRepository;
	
	public List<Board> getSidebarItems(Integer categoryId) {
		return this.mainRepository.findAllByCategoryIdOrderByViewOrder(categoryId);
	}

}
