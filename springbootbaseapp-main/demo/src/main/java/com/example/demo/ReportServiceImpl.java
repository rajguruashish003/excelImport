package com.example.demo;

import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements  ReportService {
    synchronized private JasperPrint getCoverLetter(String donorAddress) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("donor_address", donorAddress);
        return ReportUtil.generateReport(getJasperConfig(), map, new File(getClass().getResource("/templates/report.jrxml").toURI().getPath()).getAbsolutePath());
    }

    private List<ReportVO> getJasperConfig() {
        List<ReportObj> objList = new ArrayList<>();
        ReportVO reportVO = new ReportVO();
        reportVO.setReportVOList(objList);
        List<ReportVO> reportVOS = new ArrayList<>();
        reportVOS.add(reportVO);
        return reportVOS;
    }
    synchronized private JasperPrint downloadAndPrintMethod() throws Exception {
        JasperPrint downloadPrint = null;
        downloadPrint = addPages(downloadPrint, getCoverLetter("hi jhon"));
        return  downloadPrint;
    }

    synchronized private JasperPrint addPages(JasperPrint parent, JasperPrint child) {

        if (parent == null) {
            parent = child;
        } else if (child != null) {
            for (JRPrintPage jrPrintPage : child.getPages())
                parent.addPage(jrPrintPage);
        }
        return parent;
    }

    @Override
    public JasperPrint reportGeneration() throws Exception {

        return downloadAndPrintMethod();
    }
}

