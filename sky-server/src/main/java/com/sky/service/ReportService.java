package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO turnouverStatistic(LocalDate begin, LocalDate end);

    UserReportVO userStatistic(LocalDate begin, LocalDate end);

    OrderReportVO orderStatistic(LocalDate begin, LocalDate end);

    SalesTop10ReportVO saleStatistic(LocalDate begin, LocalDate end);

    void exportBusinessdData(HttpServletResponse response);
}
