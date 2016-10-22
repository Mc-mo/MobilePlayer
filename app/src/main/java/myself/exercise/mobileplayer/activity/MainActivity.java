package myself.exercise.mobileplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import java.util.ArrayList;

import myself.exercise.mobileplayer.R;
import myself.exercise.mobileplayer.base.BasePager;
import myself.exercise.mobileplayer.pager.AudioPager;
import myself.exercise.mobileplayer.pager.NetAudioPager;
import myself.exercise.mobileplayer.pager.NetVideoPager;
import myself.exercise.mobileplayer.pager.VideoPager;

public class MainActivity extends FragmentActivity {
    private FrameLayout fl_main_content;
    private RadioGroup rg_bottom_tag;

    /**
     * 页面的集合
     */
    private ArrayList<BasePager> mBasePagers;

    /**
     * 选中的位置
     */
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        rg_bottom_tag.check(R.id.rb_video);

    }

    private void init() {
        fl_main_content = (FrameLayout) findViewById(R.id.fl_main_content);
        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);
        mBasePagers = new ArrayList<>();
        mBasePagers.add(new VideoPager(this));//添加本地视频
        mBasePagers.add(new AudioPager(this));//添加本地音频
        mBasePagers.add(new NetAudioPager(this));//添加网络视频
        mBasePagers.add(new NetVideoPager(this));//添加网络音频
        rg_bottom_tag.setOnCheckedChangeListener(new myOnCheckedChangeListener());
    }

    class myOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_netaudio:
                    position = 3;
                    break;
            }
            setFragment();
        }
    }

    /**
     * 吧页面添加到Fragment
     */
    private void setFragment() {
        //1、得到FragmentManagment
        FragmentManager manager = getSupportFragmentManager();
        //2、开始事物
        FragmentTransaction ft = manager.beginTransaction();
        //3、替换
        ft.replace(R.id.fl_main_content,new Fragment(){
            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                     @Nullable Bundle savedInstanceState) {
                BasePager basePager = getBasePager();
                if (basePager != null) {
                    //各个页面的视图
                    return basePager.rootView;
                }
                return null;
            }
        });
        //4、提交
        ft.commit();
    }

    /**
     * 根据位置得到对应的页面
     *
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = mBasePagers.get(position);
        if (basePager != null&&!basePager.isInitDate) {
            basePager.initDate();//连网请求或绑定数据
            basePager.isInitDate = true;
        }
        return basePager;
    }
}
