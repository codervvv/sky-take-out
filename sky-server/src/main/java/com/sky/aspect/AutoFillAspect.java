package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段填充...");
        //获取方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取方法上的注解对象
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        //获取数据库操作类型
        OperationType operationType = annotation.value();
        //获取被拦截方法的参数
        Object[] args = joinPoint.getArgs();
        if (args==null)
            return;
        Object entity = args[0];
        if (operationType==OperationType.INSERT||operationType==OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
                if (operationType==OperationType.INSERT){
                    Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime .class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    setCreateTime.invoke(entity, LocalDateTime.now());
                    setCreateUser.invoke(entity, BaseContext.getCurrentId());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
