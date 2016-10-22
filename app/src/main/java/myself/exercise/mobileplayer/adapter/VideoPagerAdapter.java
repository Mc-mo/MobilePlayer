package myself.exercise.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


import myself.exercise.mobileplayer.R;
import myself.exercise.mobileplayer.domain.MediaItem;
import myself.exercise.mobileplayer.utils.Utils;

/**
 * Created by lenovo on 2016/10/15.
 */
public class VideoPagerAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<MediaItem> mediaItems;
    private Utils mUtils;

    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
        mUtils = new Utils();
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_video_pager, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            viewHoder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }

        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHoder.tv_time.setText(mUtils.stringForTime((int) mediaItem.getDuration()));


        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_size;
        TextView tv_time;
    }
}
