package com.example.demo;

import java.util.List;

public class ReportVO {

    String customerName;
    List<ReportObj> reportVOList;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<ReportObj> getReportVOList() {
        return reportVOList;
    }

    public void setReportVOList(List<ReportObj> reportVOList) {
        this.reportVOList = reportVOList;
    }
}
