package myself.exercise.mobileplayer.pager;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import myself.exercise.mobileplayer.R;
import myself.exercise.mobileplayer.base.BasePager;
import myself.exercise.mobileplayer.domain.Constant;
import myself.exercise.mobileplayer.utils.LogUtil;

/**
 * Created by lenovo on 2016/10/13.
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView listview;

    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager, null);
        //第一个参数为NetVideoPager.this
        x.view().inject(this, view);
        return view;
    }

    @Override
    public void initDate() {
        super.initDate();
        RequestParams params = new RequestParams(Constant.NET_URI);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.d("onSuccess="+result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.d("onError="+ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.d("onCancelled="+cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.d("onFinished");
            }
        });
    }

}
