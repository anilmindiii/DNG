package com.dng.model;

public class User {

    public String status;
    public String message;
    public DataBean data;

    public static class DataBean {
        public String id;
        public String fullName;
        public String email;
        public String userName;
        public String contact;
        public String address;
        public String profileImage;
        public String status;
        public String authToken;
        public String isVerify;
        public String approval;
        public String userType;
        public String profile;
        public int onDuty;
        public int userSoicalStatus;
        public DriverDetailBean driverDetail;

        public static class DriverDetailBean {
            public String id;
            public String userId;
            public String moneyFromManager;
            public String driversLicense;
            public String licenseExpiryDate;
            public String licenseImage;
            public String duty;
            public String route;
            public String emergencyName;
            public String emergencyRelationship;
            public String emergencyPhoneNumber;
            public String upd;
            public String crd;
        }
    }
}
