package myself.exercise.mobileplayer.pager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import myself.exercise.mobileplayer.base.BasePager;
import myself.exercise.mobileplayer.utils.LogUtil;

/**
 * Created by lenovo on 2016/10/13.
 */
public class NetAudioPager extends BasePager {
    public TextView tv;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        tv = new TextView(context);
        tv.setTextSize(30);
        tv.setTextColor(Color.RED);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    @Override
    public void initDate() {
        super.initDate();
        LogUtil.e("网络视频初始化了");
        tv.setText("网络视频的内容");
    }
}
