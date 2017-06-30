package android.vic;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 保存用户信息的单例类
 * Created by Guobao Xu on 2017/6/30.
 */

class CurrentUser {
    // IP地址，理论上这个东西最后应该要被删除的
    static String IP = "192.168.1.1";

    // 获取单例，需要给Context，直接调用getApplicationContext()
    static CurrentUser getInstance(Context context) {
        if (currentUser == null)
            currentUser = new CurrentUser(context);
        return currentUser;
    }

    // 获取各类信息
    public String getSessionID()  { return sessionID; }
    public String getUsername()   { return username; }
    public int    getId()         { return id; }
    public String getAuthority()  { return authority; }
    public String getSex()        { return sex; }
    public String getDriverType() { return driverType; }
    public String getIdentify()   { return identify; }
    public String getPhone()      { return phone; }
    public String getPhotoURL()   { return photoURL; }
    public String getAddress()    { return address; }
    public String getCompany()    { return company; }
    public String getApartment()  { return apartment; }
    public int    getJobNo()      { return jobNo; }

    // 判断是否已经登录
    boolean isLogan() { return sessionID != null; }

    // 登录后保存用户信息
    // 返回值：保存是否成功
    boolean saveLoginInfo(String username, String sessionID, int id, String authority, int sex,
                          String driverType, String identify, String phone, String photoURL,
                          String address, String company, String apartment, int jobNo,
                          Context context) {
        this.username     = username;
        this.sessionID    = sessionID;
        this.id           = id;
        this.authority    = authority;
        this.sex          = sex == 1 ? "男" : "女";
        this.driverType   = driverType;
        this.identify     = identify;
        this.phone        = phone;
        this.photoURL     = photoURL;
        this.address      = address;
        this.company      = company;
        this.apartment    = apartment;
        this.jobNo        = jobNo;
        // 写入文件
        SharedPreferences preferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username",   username);
        editor.putString("sessionID",  sessionID);
        editor.putInt   ("id",         id);
        editor.putString("authority",  authority);
        editor.putString("sex",        sex == 1 ? "男" : "女");
        editor.putString("driverType", driverType);
        editor.putString("identify",   identify);
        editor.putString("phone",      phone);
        editor.putString("photoURL",   photoURL);
        editor.putString("address",    address);
        editor.putString("company",    company);
        editor.putString("apartment",  apartment);
        editor.putInt   ("jobNo",      jobNo);
        return editor.commit();
    }

    boolean clearLoginInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        username   = null;
        sessionID  = null;
        id         = -1;
        authority  = null;
        sex        = null;
        driverType = null;
        identify   = null;
        phone      = null;
        photoURL   = null;
        address    = null;
        company    = null;
        apartment  = null;
        jobNo      = -1;
        return editor.commit();
    }

    private CurrentUser(Context context) {
        // 读取文件
        SharedPreferences preferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username   = preferences.getString ("username"  , null);
        sessionID  = preferences.getString ("sessionID" , null);
        id         = preferences.getInt    ("id"        , -1);
        authority  = preferences.getString ("authority" , null);
        sex        = preferences.getString ("sex"       , null);
        driverType = preferences.getString ("driverType", null);
        identify   = preferences.getString ("identify"  , null);
        phone      = preferences.getString ("phone"     , null);
        photoURL   = preferences.getString ("photoURL"  , null);
        address    = preferences.getString ("address"   , null);
        company    = preferences.getString ("company"   , null);
        apartment  = preferences.getString ("apartment" , null);
        jobNo      = preferences.getInt    ("jobNo"     , -1);
    }

    // 单例对象
    private static CurrentUser currentUser = null;
    // 保存的参数列表
    private String username   = null;
    private String sessionID  = null;
    private int    id         = -1;
    private String authority  = null;
    private String sex        = null;
    private String driverType = null;
    private String identify   = null;
    private String phone      = null;
    private String photoURL   = null;
    private String address    = null;
    private String company    = null;
    private String apartment  = null;
    private int    jobNo      = -1;
}
