package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnouverStatistic(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate now = begin; now.compareTo(end.plusDays(1)) < 0; now = now.plusDays(1)) {
            dateList.add(now);
            LocalDateTime orderStartTime = LocalDateTime.of(now, LocalTime.MIN);
            LocalDateTime orderEndTime = LocalDateTime.of(now, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", orderStartTime);
            map.put("end", orderEndTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ",")).build();
    }

    @Override
    public UserReportVO userStatistic(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> userList = new ArrayList<>();
        for (LocalDate now = begin; now.compareTo(end.plusDays(1)) < 0; now = now.plusDays(1)) {
            LocalDateTime orderStartTime = LocalDateTime.of(now, LocalTime.MIN);
            LocalDateTime orderEndTime = LocalDateTime.of(now, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", orderEndTime);
            Integer totalUsers = userMapper.sumByMap(map);
            map.put("begin", orderStartTime);
            Integer newUsers = userMapper.sumByMap(map);
            totalUsers = totalUsers == null ? 0 : totalUsers;
            newUsers = newUsers == null ? 0 : newUsers;
            dateList.add(now);
            totalUserList.add(totalUsers);
            userList.add(newUsers);
        }
        return UserReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(userList, ","))
                .totalUserList(StringUtils.join(totalUserList, ",")).build();
    }

    @Override
    public OrderReportVO orderStatistic(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        for (LocalDate now = begin; now.compareTo(end.plusDays(1)) < 0; now = now.plusDays(1)) {
            LocalDateTime orderStartTime = LocalDateTime.of(now, LocalTime.MIN);
            LocalDateTime orderEndTime = LocalDateTime.of(now, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", orderStartTime);
            map.put("end", orderEndTime);
            Integer orderCount = orderMapper.sumOrderByMap(map);
            map.put("status", Orders.COMPLETED);
            Integer completedOrders = orderMapper.sumOrderByMap(map);
            orderCount = orderCount == null ? 0 : orderCount;
            completedOrders = completedOrders == null ? 0 : completedOrders;
            totalOrderCount += orderCount;
            validOrderCount += completedOrders;
            dateList.add(now);
            orderCountList.add(orderCount);
            validOrderCountList.add(completedOrders);
        }
        Double orderCompletionRate = totalOrderCount == 0 ? 0 : validOrderCount * 1.0 / totalOrderCount;
        return OrderReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .validOrderCount(validOrderCount).totalOrderCount(totalOrderCount).
                orderCompletionRate(orderCompletionRate)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCountList(StringUtils.join(orderCountList, ",")).build();
    }

    @Override
    public SalesTop10ReportVO saleStatistic(LocalDate begin, LocalDate end) {
        LocalDateTime start = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime last = LocalDateTime.of(end,LocalTime.MAX);
        Map map = new HashMap();
        map.put("begin", start);
        map.put("end", last);
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> sale = orderMapper.sale(map);
        List<String> names = sale.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = sale.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,",")).build();
    }

    @Override
    public void exportBusinessdData(HttpServletResponse response) {
        LocalDate beginDay = LocalDate.now().minusDays(30);
        LocalDate endDay = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(beginDay,LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDay,LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin,end);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("日期"+begin+"——"+end);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate now = beginDay.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(now,LocalTime.MIN),LocalDateTime.of(now,LocalTime.MAX));
                sheet.getRow(7+i).getCell(1).setCellValue(String.valueOf(now));
                sheet.getRow(7+i).getCell(2).setCellValue(businessData.getTurnover());
                sheet.getRow(7+i).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet.getRow(7+i).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet.getRow(7+i).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet.getRow(7+i).getCell(6).setCellValue(businessData.getNewUsers());

            }
            
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
