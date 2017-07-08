package android.vic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * 广播
 * Created by wsq on 2017/7/2 0002.
 */

public class BrocastRec extends BroadcastReceiver {
    public static BrocastRec brocastRec = null;
    private static final String DYNAMIC = "android.vic.brocastrec";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(DYNAMIC)) {
            Bundle bundle = intent.getExtras();
            ArrayList<String> str = bundle.getStringArrayList("message");

            NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(context);
            //Bitmap bm = BitmapFactory.decodeResource(context.getResources(),R.mipmap.dynamic);
            //这里有点小修改，跳转界面变为消息界面
            Intent notificationIntent = new Intent(context, ContextActivity.class); // 点击该通知后要跳转的Activity
            //增加bundle、并改变了传递数据的pendingintent的flag参数
            Bundle mbundle = new Bundle();
            mbundle.putString("title", str.get(0));
            mbundle.putString("content", str.get(1));
            notificationIntent.putExtras(mbundle);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //将广播内容改为消息标题
            builder.setContentTitle("警报广播").setContentText(str.get(0)).setTicker("警报WARNING").setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true).setContentIntent(contentIntent);
            Notification notification = builder.build();
            // notification.setLatestEventInfo(this, "内容提示：", "我就是一个测试文件", pendingIntent);
            nm.notify(0, notification);
        }

    }
}