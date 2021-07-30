package com.example.demo;

import net.sf.jasperreports.engine.JasperPrint;

public interface ReportService {
    JasperPrint reportGeneration() throws Exception;
}
