package com.dng.model;

import java.util.List;

public class PitPaymentInfo {


    /**
     * status : success
     * message : Pit payment paid successfully done.
     * data : {"totalAmount":864,"dueAmount":314.9,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"24.5","receipt":"61c216c3800242525c4e50c8300f8b45.jpg","description":"","createDate":"2018-01-10 20:25:26"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"3.67","receipt":"eb6514f9e1953df7f861642f505220d9.jpg","description":"","createDate":"2018-01-10 20:26:42"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"12.6","receipt":"9b41a09d8aace6d6b5adf4568dbabbd3.jpg","description":"","createDate":"2018-01-10 20:28:02"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"2.7","receipt":"61806acabae0902fca85153ea7dd4bb3.jpg","description":"","createDate":"2018-01-10 20:30:09"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"1.8","receipt":"29f9dc13878a0a900cdf4c92979e2f72.jpg","description":"","createDate":"2018-01-10 20:32:11"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"5.6","receipt":"32a91077b8eb68b9cfd707c7557d7ac1.jpg","description":"","createDate":"2018-01-10 20:33:17"}]}
     */

    public String status;
    public String message;
    public DataBean data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * totalAmount : 864
         * dueAmount : 314.9
         * pay : [{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"24.5","receipt":"61c216c3800242525c4e50c8300f8b45.jpg","description":"","createDate":"2018-01-10 20:25:26"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"3.67","receipt":"eb6514f9e1953df7f861642f505220d9.jpg","description":"","createDate":"2018-01-10 20:26:42"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"12.6","receipt":"9b41a09d8aace6d6b5adf4568dbabbd3.jpg","description":"","createDate":"2018-01-10 20:28:02"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"2.7","receipt":"61806acabae0902fca85153ea7dd4bb3.jpg","description":"","createDate":"2018-01-10 20:30:09"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"1.8","receipt":"29f9dc13878a0a900cdf4c92979e2f72.jpg","description":"","createDate":"2018-01-10 20:32:11"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"5.6","receipt":"32a91077b8eb68b9cfd707c7557d7ac1.jpg","description":"","createDate":"2018-01-10 20:33:17"}]
         */

        public int totalAmount;
        public double dueAmount;
        public List<PayBean> pay;

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public double getDueAmount() {
            return dueAmount;
        }

        public void setDueAmount(double dueAmount) {
            this.dueAmount = dueAmount;
        }

        public List<PayBean> getPay() {
            return pay;
        }

        public void setPay(List<PayBean> pay) {
            this.pay = pay;
        }

        public static class PayBean {
            /**
             * driverId : 147
             * driverName : anil
             * paymentType : PARTIAL
             * paymentMode : CHEQUE
             * amount : 64
             * receipt : 23e4aa25242f49ef78390303d10bcaaa.jpg
             * description : tedt
             * createDate : 2018-01-10 19:30:57
             */

            public String driverId;
            public String driverName;
            public String paymentType;
            public String paymentMode;
            public String amount;
            public String receipt;
            public String description;
            public String createDate;

            public String getDriverId() {
                return driverId;
            }

            public void setDriverId(String driverId) {
                this.driverId = driverId;
            }

            public String getDriverName() {
                return driverName;
            }

            public void setDriverName(String driverName) {
                this.driverName = driverName;
            }

            public String getPaymentType() {
                return paymentType;
            }

            public void setPaymentType(String paymentType) {
                this.paymentType = paymentType;
            }

            public String getPaymentMode() {
                return paymentMode;
            }

            public void setPaymentMode(String paymentMode) {
                this.paymentMode = paymentMode;
            }

            public String getAmount() {
                return amount;
            }

            public void setAmount(String amount) {
                this.amount = amount;
            }

            public String getReceipt() {
                return receipt;
            }

            public void setReceipt(String receipt) {
                this.receipt = receipt;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getCreateDate() {
                return createDate;
            }

            public void setCreateDate(String createDate) {
                this.createDate = createDate;
            }
        }
    }
}
