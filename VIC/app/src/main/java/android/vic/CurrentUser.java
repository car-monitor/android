package android.vic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存用户信息的单例类
 * Created by Guobao Xu on 2017/6/30.
 */

class CurrentUser {
    // IP地址，理论上这个东西最后应该要被删除的
    static String IP = "192.168.1.1:8080/";

    // 获取单例，需要给Context，直接调用getApplicationContext()
    static CurrentUser getInstance(Context context) {
        if (currentUser == null) {
            currentUser = new CurrentUser(context);
            AuthorityDict = new ArrayMap<>();
            AuthorityDict.put(0, "普通用户");
            AuthorityDict.put(1, "司机");
            AuthorityDict.put(2, "管理员");
            AuthorityDict.put(3, "超级管理员");
        }
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

    // 设置单个变量，返回值意味着是否登录，未登录不能修改，且返回值会为false
    public boolean setSessionID(String sessionID_)   { return sessionID != null && (sessionID  = sessionID_)  != null; }
    public boolean setUsername(String username_)     { return sessionID != null && (username   = username_)   != null; }
    public boolean setId(int id_)                    { return sessionID != null && (id         = id_)         != -1; }
    // 网络接口提供权限和性别给的是int，但是这个类里保存为string
    public boolean setAuthority(int authority_) {
        if (sessionID != null && authority_ < 4 && authority_ >= 0) {
            authority = AuthorityDict.get(authority_);
            return true;
        }
        return false;
    }
    public boolean setSex(int sex_) {
        if (sessionID != null && (sex_ == 1 || sex_ == 0)) {
            sex = sex_ == 1 ? "男" : "女";
            return true;
        } else {
            return false;
        }
    }
    public boolean setDriverType(String driverType_) { return sessionID != null && (driverType = driverType_) != null; }
    public boolean setIdentify(String identify_)     { return sessionID != null && (identify   = identify_)   != null; }
    public boolean setPhone(String phone_)           { return sessionID != null && (phone      = phone_)      != null; }
    public boolean setPhotoURL(String photoURL_)     { return sessionID != null && (photoURL   = photoURL_)   != null; }
    public boolean setAddress(String address_)       { return sessionID != null && (address    = address_)    != null; }
    public boolean setCompany(String company_)       { return sessionID != null && (company    = company_)    != null; }
    public boolean setApartment(String apartment_)   { return sessionID != null && (apartment  = apartment_)  != null; }
    public boolean setJobNo(int jobNo_)              { return sessionID != null && (jobNo      = jobNo_)      == jobNo; }

    // 判断是否已经登录
    boolean isLogan() { return sessionID != null; }

    // 登录后保存用户信息
    // 返回值：保存是否成功
    boolean saveLoginInfo(String username, String sessionID, int id, int authority, int sex,
                          String driverType, String identify, String phone, String photoURL,
                          String address, String company, String apartment, int jobNo,
                          Context context) {
        this.username     = username;
        this.sessionID    = sessionID;
        this.id           = id;
        if (authority >= 0 && authority <= 3) this.authority = AuthorityDict.get(authority);
        if (authority == 0 || authority == 1) this.sex       = sex == 1 ? "男" : "女";
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
        editor.putString("username",   this.username);
        editor.putString("sessionID",  this.sessionID);
        editor.putInt   ("id",         this.id);
        editor.putString("authority",  this.authority);
        editor.putString("sex",        this.sex);
        editor.putString("driverType", this.driverType);
        editor.putString("identify",   this.identify);
        editor.putString("phone",      this.phone);
        editor.putString("photoURL",   this.photoURL);
        editor.putString("address",    this.address);
        editor.putString("company",    this.company);
        editor.putString("apartment",  this.apartment);
        editor.putInt   ("jobNo",      this.jobNo);
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
    // 权限的枚举
    private static ArrayMap<Integer, String> AuthorityDict;
}
