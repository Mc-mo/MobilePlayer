package myself.exercise.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import myself.exercise.mobileplayer.R;

/**
 * Created by lenovo on 2016/10/13.
 */
public class TitleBar extends LinearLayout implements View.OnClickListener {
    private View iv_search, rl_game, iv_record;
    private final Context context;

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        iv_search = getChildAt(1);
        rl_game = getChildAt(2);
        iv_record = getChildAt(3);
        iv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_record:
                 Toast.makeText(context,"点击了记录",Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_game:
                Toast.makeText(context,"点击了游戏",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_search:
                Toast.makeText(context,"点击了搜索框",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
