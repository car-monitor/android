package android.vic;

/**
 * 消息类
 * Created by 朱彦儒 on 2017/7/3 0003.
 */

public class Contexts {
    private int type;
    private String title;
    private String content;
    public Contexts(int _type, String _title, String _content) {
        type = _type;
        title = _title;
        content = _content;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
