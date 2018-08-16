package com.dng.helper;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Calendar;


public class Constant {
    //http://103.231.44.154/dressroom/test1/
  //  public static String LOGINURL = "http://mindiii.com/aidshwift/index.php/service/";
    public static String LOGINURL = "http://aidswiftapp.com/index.php/service/";
    public static String URL = "http://aidswiftapp.com/index.php/service/";
    public static String FIREBASEURL = "https://aidswift-a7c7b.firebaseio.com/";

    // key for run time permissions
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_CODE_PICK_CONTACTS = 100;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    public static final int PERMISSION_REQUEST_CONTACT = 103;
    public static final int PERMISSION_READ_PHONE_STATE = 104;
    public static final int MY_PERMISSIONS_REQUESTACCESS_FINE_LOCATION = 106;
    public static final int REQUEST_CAMERA = 0;
    public static final int SELECT_FILE = 1;
    public static final int CALL_PHONE = 1;

    public static final int ProfileType = 1;
    public static final int LicenseType = 2;


    public static final int mapRequestCode = 1212;
    public static final int wazeRequestCode = 1213;



    public static final int REQUEST_CODE_PAYMENT = 1;

    public static int CALENDAR_DAY = 0;
    public static final String AUTHTOKEN = "authToken";
    public static final String CANCEL = "cancel";
    public static final String DRIVER = "driver";
    public static final String PASSENGER = "passenger";
    public static final String DEVICETOKEN = "deviceToken";
    public static final String DEVICETYPE = "deviceType";
    public static final String EMAIL = "email";

    public static final String FULLNAME = "fullName" ;
    public static final String FACEBOOKID = "facebookId" ;
    public static final String GENDER = "gender";
    public static final String MESSAGE = "message";
    public static final String NOTIFICATIONTYPE = "notificationType" ;
    public static final String OK = "OK";
    public static final String ONE = "1";
    public static final String SUCCESS = "success";
    public static final String ROLE = "role" ;
    public static final String STATUS = "status";
    public static final String SOCIALID = "socialId";
    public static final String SOCIALTYPE = "socialType";

    public static final String USERTYPE = "userType";
    public static final String USERID = "id";
    public static final String USEREMAIL = "userEmail";
    public static final String USERGENDER = "userGender";
    public static final String USERDETAILS = "data";

    public static final String TWITTER = "twitter";
    public static final String FACEBOOK = "fb";
    public static final String GOOGLE = "google";

    public static final String ID = "id";

    public static final String USERNAME = "userName" ;

    public static final String PROFILEIMAGE = "profileImage";

    public static final String LOGINTYPE = "loginType" ;
    public static final String PARAMETERS = "parameters";

   //FireBase....................................................
    public static final String ARG_USERS = "users";
    public static final String ARG_RECEIVER = "receiver";
    public static final String ARG_RECEIVER_UID = "receiver_uid";
    public static final String ARG_CHAT_ROOMS = "chat_rooms";
    public static final String ARG_FIREBASE_TOKEN = "firebaseToken";
    public static final String ARG_FRIENDS = "friends";
    public static final String ARG_UID = "uid";

    public static String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month1 = month + 1;
        return (day + "-" + month1 + "-" + year);

    }

    public static String getCurrentTime() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int munite = c.get(Calendar.MINUTE);
        return (hour + ":" + munite);

    }

    public static void delete_file(String filePath) {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, filePath);

        if (file.exists()) // check if file exist
        {
            file.delete();
        }


    }

    public static String getHashKey(String packageName, Activity context) {
        PackageInfo info;
        String something = "";
        try {
            info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                something = new String(Base64.encode(md.digest(), 0));

                Log.e("hash key", something);
            }
        } catch (Exception e1) {
            Log.e("name not found", e1.toString());
        }
        return something;
    }


    public static void writeFile(String fileUrl, String filepath) {
        int count;
        InputStream input = null;
        OutputStream output = null;
        try {
            java.net.URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            input = new BufferedInputStream(url.openStream(), 8192);
            output = null;

            File dir = Environment.getExternalStorageDirectory();

            output = new FileOutputStream(dir + filepath);

            byte data[] = new byte[4096];
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder readFile(String filename) {

        StringBuilder offlinetext = new StringBuilder();

        File dir = Environment.getExternalStorageDirectory();

        File filepath = new File(dir, filename);

        if (filepath.exists()) {

            try {
                BufferedReader br = new BufferedReader(new FileReader(filepath), 8192);
                String line;

                while ((line = br.readLine()) != null) {
                    offlinetext.append(line);
                }
                br.close();
            } catch (Exception e) {
            }

        } else {
            Log.v("File Doesn't Exists!!!", ">>>>>>>>>");
        }

        return offlinetext;

    }




}
