package android.vic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 展示用户个人信息页面
 * Created by wujy on 2017/7/1.
 */

public class ShowUserInfo extends AppCompatActivity {
    private Button returnBack;
    private TextView username,authority,company,department,jobNo,phone,address;
    private Context context;
    private ImageView portrait;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo);
        context = ShowUserInfo.this;
        initView();
        if (CurrentUser.getInstance(this).isLogan()) setInfo();
        addListenner();
    }
    private void initView() {
        portrait = (ImageView)findViewById(R.id.iv_logo);
        returnBack = (Button)findViewById(R.id.info_return);
        username = (TextView)findViewById(R.id.info_username);
        authority = (TextView)findViewById(R.id.info_authority);
        company = (TextView)findViewById(R.id.info_company);
        department = (TextView)findViewById(R.id.info_department);
        jobNo = (TextView)findViewById(R.id.info_jobNo);
        phone = (TextView)findViewById(R.id.info_phone);
        address = (TextView)findViewById(R.id.info_address);
    }

    private void addListenner() {
        returnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowUserInfo.this,MainActivity.class);
                startActivity(intent);
            }

        });

    }
    //填充用户信息
    private void setInfo() {
        username.setText(CurrentUser.getInstance(context).getUsername());
        authority.setText(CurrentUser.getInstance(context).getAuthority());
        company.setText(CurrentUser.getInstance(context).getCompany());
        department.setText(CurrentUser.getInstance(context).getApartment());
        jobNo.setText(CurrentUser.getInstance(context).getJobNo());
        phone.setText(CurrentUser.getInstance(context).getPhone());
        address.setText(CurrentUser.getInstance(context).getAddress());
    }
    //// TODO: 2017/7/1 获取头像
    private void getPortrait(String URL) {

    }
}