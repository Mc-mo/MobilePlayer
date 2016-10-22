package myself.exercise.mobileplayer.pager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import myself.exercise.mobileplayer.R;
import myself.exercise.mobileplayer.activity.SystemVideoPlayer;
import myself.exercise.mobileplayer.adapter.VideoPagerAdapter;
import myself.exercise.mobileplayer.base.BasePager;
import myself.exercise.mobileplayer.domain.MediaItem;

/**
 * Created by lenovo on 2016/10/13.
 */
public class VideoPager extends BasePager {
    private ListView mListView;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private VideoPagerAdapter videoPagerAdapter;
    private Handler mhander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (medaiItems != null && medaiItems.size() > 0) {
                //有数据
                //设置适配器
                videoPagerAdapter = new VideoPagerAdapter(context, medaiItems);
                mListView.setAdapter(videoPagerAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                //没数据
                //显示文本
                tv_nomedia.setVisibility(View.VISIBLE);
            }

            //隐藏progressbar
            pb_loading.setVisibility(View.GONE);
        }
    };
    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> medaiItems;

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager, null);
        mListView = (ListView) view.findViewById(R.id.listview);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mListView.setOnItemClickListener(new MyItemClickListener());
        return view;
    }

    @Override
    public void initDate() {
        super.initDate();
        getDataFromLocal();
    }

    /**
     * 从本地sdcard中获取数据
     */
    private void getDataFromLocal() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                isGrantExernalRw((Activity) context);
                medaiItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件名
                        MediaStore.Video.Media.DURATION,//视频长度
                        MediaStore.Video.Media.SIZE,//视频大小
                        MediaStore.Video.Media.DATA,//视频的绝对路径
                        MediaStore.Video.Media.ARTIST//艺术家
                };

                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        medaiItems.add(mediaItem);

                        String name = cursor.getString(0);
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
                //发消息
                mhander.sendEmptyMessage(0);
            }
        }.start();
    }

    class MyItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Toast.makeText(context, medaiItems.get(position).toString(), Toast.LENGTH_SHORT)
// .show();
//            Intent intent = new Intent(context, SystemVideoPlayer.class);
//            Uri uri = Uri.parse(medaiItems.get(position).getData());
//            intent.setDataAndType(uri,"video/*");
//            context.startActivity(intent);

            //序列化传递对象
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist",medaiItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }

    /**
     * 解决6.0以上版本不能读取外部存储权限的问题
     * @param activity
     * @return
     */
    public static boolean isGrantExernalRw(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }
}
