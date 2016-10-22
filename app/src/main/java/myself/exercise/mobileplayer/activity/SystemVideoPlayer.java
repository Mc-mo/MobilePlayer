package myself.exercise.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import myself.exercise.mobileplayer.R;
import myself.exercise.mobileplayer.domain.MediaItem;
import myself.exercise.mobileplayer.utils.LogUtil;
import myself.exercise.mobileplayer.utils.Utils;
import myself.exercise.mobileplayer.view.VideoView;

/**
 * Created by lenovo on 2016/10/17.
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    /**
     * 更新进度条
     */
    private static final int PROGRESS = 1;
    /**
     * 隐藏媒体控制面吧
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 网络速度
     */
    private static final int NET_SPEED = 3;
    /**
     * 默认显示
     */
    private static final int DEFAULT_SCREEN = 0;
    /**
     * 全屏显示
     */
    private static final int FULL_SCREEN = 1;

    private VideoView mVideoView;
    private Uri uri;

    private TextView tv_name;
    private ImageView iv_battery;
    private TextView tv_system_time;
    private Button btn_voice;
    private SeekBar seekbar_voice;
    private RelativeLayout media_controller;
    private Button btn_switch_player;
    private LinearLayout ll_top;
    private TextView tv_current_time;
    private SeekBar seekbar_video;
    private TextView tv_duration;
    private Button btn_exit;
    private Button btn_video_pre;
    private Button btn_video_start_pause;
    private Button btn_video_next;
    private Button btn_video_switch_screen;
    private LinearLayout ll_bottom;
    private LinearLayout ll_buffer;
    private TextView tv_netspeed;
    private Utils mUtils;
    private MyReceiver receiver;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    /**
     * 手势
     */
    private GestureDetector mDetector;

    private Boolean isShowMediaController = false;
    /**
     * 是否全屏状态
     */
    private boolean isFullScreen = false;
    /**
     * 屏幕宽
     */
    private int screenWidth;
    /**
     * 屏幕高
     */
    private int screenHeignt;
    private int videoHeight;
    private int videoWidth;

    private AudioManager am;
    private int currentVoice;
    private int maxVoice;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    private float startY;
    private int vol;
    private int touchRang;
    /**
     * 判断是否为网络视频
     */
    private boolean isNetUri = false;

    /**
     * 是否应用系统卡
     */
    private boolean isSystemBuff = true;

    /**
     * 上一秒进度
     */
    private int prePosition;

    private LinearLayout ll_loading;

    private TextView tv_loading_netspeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system_vider_player);

        initView();

        initData();

        setListener();

        getData();

        setData();


        // mVideoView.setMediaController(new MediaController(this));
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tv_name.setText(mediaItem.getName());
            isNetUri = mUtils.isNetUri(mediaItem.getData());
            mVideoView.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tv_name.setText(uri.toString());
            isNetUri = mUtils.isNetUri(uri.toString());
            mVideoView.setVideoURI(uri);
        }

    }

    private void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void initData() {
        mUtils = new Utils();

        mHandler.sendEmptyMessage(NET_SPEED);

        //动态注册电量广播监听
        receiver = new MyReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        mHandler.sendEmptyMessage(HIDE_MEDIACONTROLLER);

        registerReceiver(receiver, intentfilter);

        /**
         * 实现手势识别器 并重写长按 单击和双击
         */
        mDetector = new GestureDetector(SystemVideoPlayer.this, new GestureDetector
                .SimpleOnGestureListener() {

            /**
             * 双击
             * @param e
             * @return
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setFullAndDefault();
                return super.onDoubleTap(e);
            }

            /**
             * 长按
             * @param e
             */
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            /**
             * 单击
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    hideMediaController();
                    mHandler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    showMediaController();
                    mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeignt = metrics.heightPixels;

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        seekbar_voice.setMax(maxVoice);
        seekbar_voice.setProgress(currentVoice);
    }

    private void setFullAndDefault() {
        if (isFullScreen) {
            //全屏显示
            setVideoType(FULL_SCREEN);
        } else {
            //默认显示
            setVideoType(DEFAULT_SCREEN);
        }
    }

    private void setVideoType(int fullScreen) {
        switch (fullScreen) {
            case FULL_SCREEN:
                //全屏显示
                mVideoView.setSize(screenWidth, screenHeignt);
                //修改按钮为默认显示
                btn_video_switch_screen.setBackgroundResource(R.drawable
                        .btn_video_switch_screen_default_selector);
                isFullScreen = false;
                break;
            case DEFAULT_SCREEN:
                //默认显示
                //视频的真实宽高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽高
                int height = screenHeignt;
                int width = screenWidth;
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                mVideoView.setSize(width, height);
                //修改按钮为全屏显示
                btn_video_switch_screen.setBackgroundResource(R.drawable
                        .btn_video_switch_screen_full_selector);
                isFullScreen = true;
                break;
        }

    }

    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isShowMediaController = false;

    }

    private void setListener() {
        mVideoView.setOnCompletionListener(new myOnCompletionListener());

        mVideoView.setOnErrorListener(new myOnErrorListener());

        mVideoView.setOnPreparedListener(new myOnPreparedListener());

        seekbar_video.setOnSeekBarChangeListener(new videoOnSeekBarChangeListener());

        seekbar_voice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());


        if (isSystemBuff) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVideoView.setOnInfoListener(new MyOnInfoListener());
            }
        }
    }


    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.videoview);

        tv_name = (TextView) findViewById(R.id.tv_name);
        iv_battery = (ImageView) findViewById(R.id.iv_battery);
        tv_system_time = (TextView) findViewById(R.id.tv_system_time);
        btn_voice = (Button) findViewById(R.id.btn_voice);
        seekbar_voice = (SeekBar) findViewById(R.id.seekbar_voice);
        btn_switch_player = (Button) findViewById(R.id.btn_switch_player);
        ll_top = (LinearLayout) findViewById(R.id.ll_top);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        seekbar_video = (SeekBar) findViewById(R.id.seekbar_video);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_video_pre = (Button) findViewById(R.id.btn_video_pre);
        btn_video_start_pause = (Button) findViewById(R.id.btn_video_start_pause);
        btn_video_next = (Button) findViewById(R.id.btn_video_next);
        btn_video_switch_screen = (Button) findViewById(R.id.btn_video_switch_screen);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        media_controller = (RelativeLayout) findViewById(R.id.media_controller);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_netspeed = (TextView) findViewById(R.id.tv_netspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);

        btn_voice.setOnClickListener(this);
        btn_switch_player.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
        btn_video_pre.setOnClickListener(this);
        btn_video_start_pause.setOnClickListener(this);
        btn_video_next.setOnClickListener(this);
        btn_video_switch_screen.setOnClickListener(this);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case NET_SPEED:

                    String net_speed = mUtils.getNetSpeed(SystemVideoPlayer.this);
                    tv_loading_netspeed.setText(net_speed);
                    tv_netspeed.setText(net_speed);
                    //更新网速
                    mHandler.removeMessages(NET_SPEED);
                    mHandler.sendEmptyMessageDelayed(NET_SPEED, 2000);
                    break;

                case PROGRESS:
                    int current_time = mVideoView.getCurrentPosition();
                    seekbar_video.setProgress(current_time);

                    tv_current_time.setText(mUtils.stringForTime(current_time));

                    tv_system_time.setText(getSystemTime());

                    //缓冲更新
                    if (isNetUri) {
                        int buff = mVideoView.getBufferPercentage();//缓冲
                        int totalBuff = buff * seekbar_video.getMax();
                        int secondaryProgress = totalBuff / 100;
                        seekbar_video.setSecondaryProgress(secondaryProgress);
                    } else {
                        seekbar_video.setSecondaryProgress(0);
                    }

                    //自定义判断卡不卡
                    if (!isSystemBuff ) {
                        if (mVideoView.isPlaying()) {
                            int buff = current_time - prePosition;
                            if (buff < 500) {
                                //系统卡
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //不卡
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    prePosition = current_time;

                    mHandler.removeMessages(PROGRESS);
                    mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    break;
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
            }
        }
    };

    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice:
                isMute = !isMute;
                updateVoice(currentVoice, isMute);
                break;
            case R.id.btn_switch_player:
                showSwitchDialog();
                break;
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_video_pre:
                playPreVideo();
                break;
            case R.id.btn_video_start_pause:
                startAndPause();
                break;
            case R.id.btn_video_next:
                playNextVideo();
                break;
            case R.id.btn_video_switch_screen:
                setFullAndDefault();
                break;
        }
        mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
    }

    private void showSwitchDialog() {
        AlertDialog.Builder buidler = new AlertDialog.Builder(this);
        buidler.setTitle("提示");
        buidler.setMessage("当播放视频的时候出现有声音没画面时您可以选择切换万能播放器");
        buidler.setPositiveButton("确定", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();

            }
        });
        buidler.setNegativeButton("取消",null);
        buidler.show();
    }

    private void startAndPause() {
        if (mVideoView.isPlaying()) {
            //视频在播放-设置暂停
            mVideoView.pause();
            btn_video_start_pause.setBackgroundResource(R.drawable
                    .btn_video_start_selector);
        } else {
            //设置播放
            mVideoView.start();
            btn_video_start_pause.setBackgroundResource(R.drawable
                    .btn_video_pause_selector);
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tv_name.setText(mediaItem.getName());
                isNetUri = mUtils.isNetUri(mediaItem.getData());
                mVideoView.setVideoPath(mediaItem.getData());
                setButtonState();
            }
        } else if (uri != null) {
            setButtonState();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tv_name.setText(mediaItem.getName());

                isNetUri = mUtils.isNetUri(mediaItem.getData());
                mVideoView.setVideoPath(mediaItem.getData());
                setButtonState();
            }
        } else if (uri != null) {
            setButtonState();
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);
            } else {
                if (position == 0) {
                    btn_video_next.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btn_video_next.setEnabled(true);
                    btn_video_pre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btn_video_pre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {
                    btn_video_next.setBackgroundResource(R.drawable.btn_next_gray);
                    btn_video_next.setEnabled(false);
                    btn_video_pre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btn_video_pre.setEnabled(true);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            setEnable(false);
        }
    }

    private void setEnable(Boolean isEnable) {
        if (isEnable) {
            btn_video_next.setBackgroundResource(R.drawable.btn_video_next_selector);
            btn_video_next.setEnabled(true);
            btn_video_pre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btn_video_pre.setEnabled(true);
        } else {
            btn_video_next.setBackgroundResource(R.drawable.btn_next_gray);
            btn_video_next.setEnabled(false);
            btn_video_pre.setBackgroundResource(R.drawable.btn_pre_gray);
            btn_video_pre.setEnabled(false);
        }
    }


    class myOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            btn_video_start_pause.setBackgroundResource(R.drawable
                    .btn_video_start_selector);
            playNextVideo();
        }
    }

    class myOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //1、播放视频的格式不支持--跳转到万能播放器
            startVitamioPlayer();
            //2、播放网络视频的时候，网络中断--1、如果网络确实中断了，可以提示网络断了；2、网络断断续续，重新播放

            //3、播放的时候本地文件中间空白 --下载做完成
            return true;
        }
    }

    private void startVitamioPlayer() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        Intent intent = new Intent(SystemVideoPlayer.this, VitamioVideoPlayer.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            //序列化传递对象
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    class myOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            ll_loading.setVisibility(View.GONE);
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            btn_video_start_pause.setBackgroundResource(R.drawable
                    .btn_video_pause_selector);
            mVideoView.start();
            int duration_time = mVideoView.getDuration();
            //设置progressbar总时长
            seekbar_video.setMax(duration_time);
            //设置文本时长
            tv_duration.setText(mUtils.stringForTime(duration_time));

            mHandler.sendEmptyMessage(PROGRESS);


            setButtonState();
            setVideoType(DEFAULT_SCREEN);
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updateVoice(progress, isMute);

            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 更新音量
     *
     * @param progress
     * @param isMute
     */
    private void updateVoice(int progress, boolean isMute) {

        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbar_voice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbar_voice.setProgress(progress);
            currentVoice = progress;
        }
    }


    class videoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 视频拖动的时候回调该方法 播放时seekbar自动拖动也会回调该方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 如果是用户拖动则为true，否则为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                mVideoView.seekTo(progress);
            }
        }


        /**
         * 手指触碰时回调该方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }


        /**
         * 手指离开时回调该方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            LogUtil.e("level=" + level);
            setBattery(level);
        }
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                //开始卡
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                //卡完了
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }

            return true;
        }
    }

    private void setBattery(int level) {
        LogUtil.e("level--->" + level);
        if (level <= 0) {
            iv_battery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            iv_battery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            iv_battery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            iv_battery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            iv_battery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            iv_battery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        } else {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        }


    }


    @Override
    protected void onDestroy() {
        //释放资源时，先释放子类再到父类，初始化时调转、、
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeMessages(HIDE_MEDIACONTROLLER);
                startY = event.getY();
                vol = currentVoice;
                touchRang = Math.min(screenHeignt, screenWidth);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float distanceY = startY - endY;
                float delta = (distanceY / touchRang) * maxVoice;
                int voice = (int) Math.min(Math.max(vol + delta, 0), maxVoice);
                if (delta != 0) {
                    isMute = false;
                    updateVoice(voice, isMute);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
