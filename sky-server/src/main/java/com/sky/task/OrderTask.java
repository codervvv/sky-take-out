package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;
    @Scheduled(cron = "0 15 * * * ? ")
    public void processTimeOutOrder(){
        log.info("处理超时订单：{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.outTimeOrderAndDeliveryOrder(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (ordersList!=null && ordersList.size()>0){
            for (Orders order: ordersList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时未支付！");
                orderMapper.update(order);
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * ? ")
    public void processDeliveryOrder(){
        log.info("处理派送订单：{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.outTimeOrderAndDeliveryOrder(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().minusHours(4));
        if (ordersList!=null && ordersList.size()>0){
            for (Orders order: ordersList){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
