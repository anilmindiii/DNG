package com.dng.model;

import java.util.List;

public class DailyReportInfo {
    

    public String status;
    public String message;
    public DataBean data;
    
    public static class DataBean {

        public String moneyAddManager;
        public String moneyReturnDriver;
        public String numberOfDelivery;
        public String numberofLoading;
        public String dateOfReport;
        public String fuelCost;
        public String moneyFromManager;
        public List<ReportBean> report;
        

        public static class ReportBean {
          
            public String moneyAddManager;
            public String date;
            public String moneyReturnDriver;
            
        }
    }
}
