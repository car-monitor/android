package android.vic.RetrofitEntity;

/**
 * Created by wujy on 2017/7/1.
 */

public class LoginRetro {
        public int status;
        public UserInfo user;
        public class UserInfo {
            public int id;
            public int authority;
            public int sex;
            public String driverType;
            public String identify;
            public String phone;
            public String photoURL;
            public String address;
            public int companyID;
            public int appartmentID;
            public int jobNo;

        }
}
