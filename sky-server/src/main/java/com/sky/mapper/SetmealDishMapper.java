package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishs);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getSetmealById(Long id);

    void deleteBatch(List<Long> ids);

    @Select("select sd.name,sd.copies,d.image,d.description from setmeal_dish sd,dish d where sd.dish_id=d.id and sd.setmeal_id = #{id}")
    List<DishItemVO> getDishItemById(Long id);
}
