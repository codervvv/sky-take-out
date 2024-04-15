package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {
    void saveWithFlavor(DishVO dishVO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void startOrStop(Integer status, Long id);

    void update(DishDTO dishDTO);

    DishVO getByIdWithFlavor(Long id);

    List<Dish> list(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
