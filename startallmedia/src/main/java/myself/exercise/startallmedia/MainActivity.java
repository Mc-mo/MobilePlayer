package myself.exercise.startallmedia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View view) {
        Intent intent = new Intent();
//        intent.setDataAndType(Uri.parse("http://192.168.191.1:8080/fun.mp4"), "video/*");
        intent.setDataAndType(Uri.parse("http://192.168.191.1:8080/oppo.wmv"), "video/*");
//        intent.setDataAndType(Uri.parse("http://vf2.mtime.cn/Video/2016/10/21/mp4/161021080707964346_480.mp4"), "video/*");

        startActivity(intent);
    }
}
