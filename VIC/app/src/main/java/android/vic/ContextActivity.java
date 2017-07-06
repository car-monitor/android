package android.vic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/7/2 0002.
 */

public class ContextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contex_display);

        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");
        String content = bundle.getString("content");
        TextView Context = (TextView)findViewById(R.id.context);
        TextView _Context = (TextView)findViewById(R.id._context);
        Context.setText(title);
        _Context.setText(content);
    }
}
