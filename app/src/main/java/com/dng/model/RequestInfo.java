package com.dng.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RequestInfo implements Serializable{


    /**
     * status : success
     * message : OK
     * data : {"deliveryId":"8","invoiceOrderId":"1023401180709/9/8","order":"6","subOrderId":"9","totalShift":"4","completeShift":"1","requestStatus":"1","deliveryStatus":"1","orderInfo":{"customerName":"Vaibhav sharma","customerPhoneNumber":"(465) 467-5765","customerEmail":"vaibhav.mindiii@gmail.com","billingAddressType":"1","billingAddress":"Indore, Madhya Pradesh, India","poBoxNumber":"","city":"","zipcode":"","deliveryAddress":"Gita Bhawan Square, Manorama Ganj, Indore, Madhya Pradesh, India","deliveryLatitude":"22.7183761","deliveryLongitude":"75.88430089999997","paymentMode":"COD","totalAmount":"2768.15","customerPay":{"totalAmount":"2768.15","dueAmount":2758.15,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10","receipt":"392efeaa45b5830cfdbc39f392799c54.jpg","description":"","createDate":"2018-01-10 20:02:45"}]},"customerPaymentStatus":"0","deliveryDistance":"3.85"},"pitInfo":{"pitName":"Mindiii","pitContact":"(546) 546-5675","pitAddress":"Bengali Square, Indore, Madhya Pradesh, India","pitLatitude":"22.708145","pitLongitude":"75.922958","pitPay":{"totalAmount":864,"dueAmount":365.77,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"}]},"pitPayStatus":"0"},"productInfo":{"productName":"Affordable Garden Soil","quantity":"72","unitType":"Ton"},"shiftReport":[{"beforeDelivery":{"image":"http://165.227.190.43/sandnsoil/uploads/report/89cb13856b77eb3f7c3f0b41e099444d.jpg","crd":"2018-01-10 20:08:17"},"afterDelivery":{"image":"http://165.227.190.43/sandnsoil/uploads/report/41576ac0284918b17fd177b99c5bc49d.jpg","crd":"2018-01-10 20:08:24"},"deliveryReceipt":{"image":"http://165.227.190.43/sandnsoil/uploads/report/b8321109fc4ac5f49b4c50e1c890ad1d.jpg","crd":"2018-01-10 20:08:30"},"status":"Complete"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"}]}
     */

    public String status = "";
    public String message = "";
    public DataBean data = new DataBean();
    public String isInCustomerArea = "noInCustomerArea";

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
         * deliveryId : 8
         * invoiceOrderId : 1023401180709/9/8
         * order : 6
         * subOrderId : 9
         * totalShift : 4
         * completeShift : 1
         * requestStatus : 1
         * deliveryStatus : 1
         * orderInfo : {"customerName":"Vaibhav sharma","customerPhoneNumber":"(465) 467-5765","customerEmail":"vaibhav.mindiii@gmail.com","billingAddressType":"1","billingAddress":"Indore, Madhya Pradesh, India","poBoxNumber":"","city":"","zipcode":"","deliveryAddress":"Gita Bhawan Square, Manorama Ganj, Indore, Madhya Pradesh, India","deliveryLatitude":"22.7183761","deliveryLongitude":"75.88430089999997","paymentMode":"COD","totalAmount":"2768.15","customerPay":{"totalAmount":"2768.15","dueAmount":2758.15,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10","receipt":"392efeaa45b5830cfdbc39f392799c54.jpg","description":"","createDate":"2018-01-10 20:02:45"}]},"customerPaymentStatus":"0","deliveryDistance":"3.85"}
         * pitInfo : {"pitName":"Mindiii","pitContact":"(546) 546-5675","pitAddress":"Bengali Square, Indore, Madhya Pradesh, India","pitLatitude":"22.708145","pitLongitude":"75.922958","pitPay":{"totalAmount":864,"dueAmount":365.77,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"}]},"pitPayStatus":"0"}
         * productInfo : {"productName":"Affordable Garden Soil","quantity":"72","unitType":"Ton"}
         * shiftReport : [{"beforeDelivery":{"image":"http://165.227.190.43/sandnsoil/uploads/report/89cb13856b77eb3f7c3f0b41e099444d.jpg","crd":"2018-01-10 20:08:17"},"afterDelivery":{"image":"http://165.227.190.43/sandnsoil/uploads/report/41576ac0284918b17fd177b99c5bc49d.jpg","crd":"2018-01-10 20:08:24"},"deliveryReceipt":{"image":"http://165.227.190.43/sandnsoil/uploads/report/b8321109fc4ac5f49b4c50e1c890ad1d.jpg","crd":"2018-01-10 20:08:30"},"status":"Complete"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"},{"beforeDelivery":{"image":"","crd":""},"afterDelivery":{"image":"","crd":""},"deliveryReceipt":{"image":"","crd":""},"status":"Pending"}]
         */

        public String deliveryId;
        public String invoiceOrderId;
        public String mainOrderId;
        public String order;
        public String subOrderId;
        public String totalShift;
        public String completeShift;
        public String requestStatus;
        public String deliveryStatus;
        public OrderInfoBean orderInfo = new OrderInfoBean();
        public PitInfoBean pitInfo = new PitInfoBean();
        public DriverInfoBean driverInfo = new DriverInfoBean();
        public ProductInfoBean productInfo = new ProductInfoBean();
        public List<ShiftReportBean> shiftReport = new ArrayList<>();


        public static class OrderInfoBean {
            /**
             * customerName : Vaibhav sharma
             * customerPhoneNumber : (465) 467-5765
             * customerEmail : vaibhav.mindiii@gmail.com
             * billingAddressType : 1
             * billingAddress : Indore, Madhya Pradesh, India
             * poBoxNumber :
             * city :
             * zipcode :
             * deliveryAddress : Gita Bhawan Square, Manorama Ganj, Indore, Madhya Pradesh, India
             * deliveryLatitude : 22.7183761
             * deliveryLongitude : 75.88430089999997
             * paymentMode : COD
             * totalAmount : 2768.15
             * customerPay : {"totalAmount":"2768.15","dueAmount":2758.15,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10","receipt":"392efeaa45b5830cfdbc39f392799c54.jpg","description":"","createDate":"2018-01-10 20:02:45"}]}
             * customerPaymentStatus : 0
             * deliveryDistance : 3.85
             */

            public String customerName;
            public String customerPhoneNumber;
            public String customerEmail;
            public String billingAddressType;
            public String billingAddress;
            public String poBoxNumber;
            public String city;
            public String zipcode;
            public String deliveryAddress;
            public String deliveryLatitude;
            public String deliveryLongitude;
            public String paymentMode;
            public String totalAmount;
            public CustomerPayBean customerPay = new CustomerPayBean();
            public String customerPaymentStatus;
            public String deliveryDistance;
            public String orderDescription;


            public static class CustomerPayBean {
                /**
                 * totalAmount : 2768.15
                 * dueAmount : 2758.15
                 * pay : [{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10","receipt":"392efeaa45b5830cfdbc39f392799c54.jpg","description":"","createDate":"2018-01-10 20:02:45"}]
                 */

                public double totalAmount;
                public double dueAmount;
                public List<PayBean> pay = new ArrayList<>();


                public static class PayBean {
                    /**
                     * driverId : 147
                     * driverName : anil
                     * paymentType : PARTIAL
                     * paymentMode : CHEQUE
                     * amount : 10
                     * receipt : 392efeaa45b5830cfdbc39f392799c54.jpg
                     * description :
                     * createDate : 2018-01-10 20:02:45
                     */

                    public String driverId;
                    public String driverName;
                    public String paymentType;
                    public String paymentMode;
                    public String amount;
                    public String receipt;
                    public String description;
                    public String createDate;


                }
            }
        }

        public static class PitInfoBean {
            /**
             * pitName : Mindiii
             * pitContact : (546) 546-5675
             * pitAddress : Bengali Square, Indore, Madhya Pradesh, India
             * pitLatitude : 22.708145
             * pitLongitude : 75.922958
             * pitPay : {"totalAmount":864,"dueAmount":365.77,"pay":[{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"}]}
             * pitPayStatus : 0
             */

            public String assignPitId;
            public String pitId;
            public String pitName;
            public String pitContact;
            public String pitAddress;
            public String pitLatitude;
            public String pitLongitude;
            public PitPayBean pitPay = new PitPayBean();
            public String pitPayStatus;


            public static class PitPayBean {
                /**
                 * totalAmount : 864
                 * dueAmount : 365.77
                 * pay : [{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/23e4aa25242f49ef78390303d10bcaaa.jpg","description":"tedt","createDate":"2018-01-10 19:30:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"ACCOUNT","amount":"200","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/97d119ff34b509eb78a97131affe8f32.jpg","description":"","createDate":"2018-01-10 19:47:57"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/5cb33e7f49bd707476d808e5f02ffb44.jpg","description":"","createDate":"2018-01-10 19:52:28"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"","description":"","createDate":"2018-01-10 19:52:31"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"64","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/0b73c2cb2dfe70bd0d0822d12db19582.jpg","description":"","createDate":"2018-01-10 19:55:10"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CASH","amount":"50","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/2d844386cf8567ecd4b97f4858be6a99.jpg","description":"","createDate":"2018-01-10 19:57:40"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CREDITCARD","amount":"10","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/eff6a84155b5db5f3eb559f894bb8c7d.jpg","description":"","createDate":"2018-01-10 20:15:14"},{"driverId":"147","driverName":"anil","paymentType":"PARTIAL","paymentMode":"CHEQUE","amount":"10.23","receipt":"http://165.227.190.43/sandnsoil/uploads/receipt/34b51e86d44b67bd775dea029da00d9a.jpg","description":"","createDate":"2018-01-10 20:17:32"}]
                 */

                public double totalAmount;
                public double dueAmount;
                public List<PayBeanX> pay = new ArrayList<>();


                public static class PayBeanX {
                    /**
                     * driverId : 147
                     * driverName : anil
                     * paymentType : PARTIAL
                     * paymentMode : CHEQUE
                     * amount : 64
                     * receipt : http://165.227.190.43/sandnsoil/uploads/receipt/23e4aa25242f49ef78390303d10bcaaa.jpg
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


                }
            }
        }

        public static class ProductInfoBean {
            /**
             * productName : Affordable Garden Soil
             * quantity : 72
             * unitType : Ton
             */

            public String productName;
            public String quantity;
            public String unitType;


        }

        public static class ShiftReportBean {
            /**
             * beforeDelivery : {"image":"http://165.227.190.43/sandnsoil/uploads/report/89cb13856b77eb3f7c3f0b41e099444d.jpg","crd":"2018-01-10 20:08:17"}
             * afterDelivery : {"image":"http://165.227.190.43/sandnsoil/uploads/report/41576ac0284918b17fd177b99c5bc49d.jpg","crd":"2018-01-10 20:08:24"}
             * deliveryReceipt : {"image":"http://165.227.190.43/sandnsoil/uploads/report/b8321109fc4ac5f49b4c50e1c890ad1d.jpg","crd":"2018-01-10 20:08:30"}
             * status : Complete
             */

            public BeforeDeliveryBean beforeDelivery = new BeforeDeliveryBean();
            public AfterDeliveryBean afterDelivery = new AfterDeliveryBean();
            public DeliveryReceiptBean deliveryReceipt = new DeliveryReceiptBean();
            public String status;


            public static class BeforeDeliveryBean {
                /**
                 * image : http://165.227.190.43/sandnsoil/uploads/report/89cb13856b77eb3f7c3f0b41e099444d.jpg
                 * crd : 2018-01-10 20:08:17
                 */

                public String image;
                public String crd;

                public String getImage() {
                    return image;
                }

                public void setImage(String image) {
                    this.image = image;
                }

                public String getCrd() {
                    return crd;
                }

                public void setCrd(String crd) {
                    this.crd = crd;
                }
            }

            public static class AfterDeliveryBean {
                /**
                 * image : http://165.227.190.43/sandnsoil/uploads/report/41576ac0284918b17fd177b99c5bc49d.jpg
                 * crd : 2018-01-10 20:08:24
                 */

                public String image;
                public String crd;


            }

            public static class DeliveryReceiptBean {
                /**
                 * image : http://165.227.190.43/sandnsoil/uploads/report/b8321109fc4ac5f49b4c50e1c890ad1d.jpg
                 * crd : 2018-01-10 20:08:30
                 */

                public String image;
                public String crd;


            }


        }

        public static class DriverInfoBean {

            public String onDuty;
            public String onRoute;


        }

    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
