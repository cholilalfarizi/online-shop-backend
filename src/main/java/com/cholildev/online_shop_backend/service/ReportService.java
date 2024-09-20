package com.cholildev.online_shop_backend.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.cholildev.online_shop_backend.model.Orders;
import com.cholildev.online_shop_backend.repository.OrdersRepository;
import com.cholildev.online_shop_backend.specification.OrderSpecification;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportService {
    private final OrdersRepository ordersRepository;

    public ByteArrayOutputStream generateReport(String reportFormat, String keyword, Pageable page)
            throws FileNotFoundException, JRException {

        Specification<Orders> orderSpec = OrderSpecification.orderFilter(keyword);
        Page<Orders> orderPage = ordersRepository.findAll(orderSpec, page);
        List<Orders> orders = orderPage.getContent();

        File file = ResourceUtils.getFile("classpath:report/OrderReport.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orders);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "System");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (reportFormat.equalsIgnoreCase("pdf")) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
        }

        return byteArrayOutputStream;
    }

}
