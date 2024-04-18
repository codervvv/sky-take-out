package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "营业额统计")
public class ReportController {
    @Autowired
    ReportService reportService;
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverReport(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        TurnoverReportVO turnoverReportVO = reportService.turnouverStatistic(begin,end);
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                              @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        UserReportVO userReportVO = reportService.userStatistic(begin,end);
        return Result.success(userReportVO);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("用户统计")
    public Result<OrderReportVO> orderStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        OrderReportVO orderReportVO = reportService.orderStatistic(begin,end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量统计")
    public Result<SalesTop10ReportVO> saleStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        SalesTop10ReportVO salesTop10ReportVO = reportService.saleStatistic(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    @ApiOperation("导出营业报表")
    public void export(HttpServletResponse response){
        reportService.exportBusinessdData(response);
    }
}
