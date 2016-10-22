package myself.exercise.mobileplayer.base;

import android.content.Context;
import android.view.View;

/**
 * Created by lenovo on 2016/10/13.
 */
public abstract class BasePager {
    /**
     * 上下文
     */
    public final Context context;
    public  View rootView;
    public boolean isInitDate = false;

    public BasePager(Context context){
        this.context = context;
        rootView = initView();
    }

    /**
     * 初始化页面
     * @return
     */
    public abstract View initView();

    /**
     * 初始化网络数据或者绑定数据
     */
    public void  initDate(){

    }
}
