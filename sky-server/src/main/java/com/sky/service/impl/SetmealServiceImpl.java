package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.script.ScriptTemplateConfig;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();
        setmealDishs.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.insertBatch(setmealDishs);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBatch(ids);
    }

    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmeal,setmealDTO);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        Long id = setmeal.getId();
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBatch(Arrays.asList(id));
        if (setmealDishes!=null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setDishId(id);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
            //查询套餐内所有的菜品
            List<Dish> dishList = dishMapper.getSetmealById(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmeals =  setmealMapper.list(setmeal);
        return setmeals;
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmealVO,setmeal);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        List<DishItemVO> dishItemVOList = setmealDishMapper.getDishItemById(id);
        return dishItemVOList;
    }
}
