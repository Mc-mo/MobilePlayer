package myself.exercise.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by lenovo on 2016/10/19.
 */
public class VitamioVideoView extends io.vov.vitamio.widget.VideoView{
    public VitamioVideoView(Context context) {
        this(context, null);
    }

    public VitamioVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VitamioVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置指定宽高
     * @param viewWidth 宽
     * @param viewHeight    高
     */
    public void setSize(int viewWidth,int viewHeight){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = viewWidth;
        params.height = viewHeight;
        setLayoutParams(params);

    }
}
