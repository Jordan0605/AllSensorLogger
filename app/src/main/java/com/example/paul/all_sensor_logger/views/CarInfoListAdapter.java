package com.example.paul.all_sensor_logger.views;

/**
 * Created by User on 2017/4/17.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.paul.all_sensor_logger.R;

import java.util.List;

/**
 * A custom adapter for our listview
 * <p/>
 * If you check http://developer.android.com/reference/android/widget/Adapter.html you'll notice
 * there are several types. BaseAdapter is a good generic adapter that should suit all your needs.
 * Just implement all what's abstract and add your collection of data
 * <p/>
 * Created by hanscappelle on 7/10/14.
 * https://github.com/hanscappelle/so-2250770
 */
public class CarInfoListAdapter extends BaseAdapter {
    private final static String TAG = CarInfoListAdapter.class.getSimpleName();
    /**
     * this is our own collection of data, can be anything we want it to be as long as we get the
     * abstract methods implemented using this data and work on this data (see getter) you should
     * be fine
     */
    private List<CarInfoItem> mData;

    /**
     * some context can be useful for getting colors and other resources for layout
     */
    private Context mContext;
    private int isMaster=0;
    /**
     * our ctor for this adapter, we'll accept all the things we need here
     *
     * @param mData
     */
    public CarInfoListAdapter(final Context context, final List<CarInfoItem> mData) {
        this.mData = mData;
        this.mContext = context;
    }

    public List<CarInfoItem> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mData != null ? mData.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        // just returning position as id here, could be the id of your model object instead
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // this is where we'll be creating our view, anything that needs to update according to
        // your model object will need a view to visualize the state of that propery
        View view = convertView;


        // the viewholder pattern for performance
        ViewHolder viewHolder = new ViewHolder();
        if (view == null) {

            // inflate the layout, see how we can use this context reference?
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            view = inflater.inflate(R.layout.carinfo_list_item, parent, false);
            Log.d(TAG, String.format("Get view %d", position));
            // we'll set up the ViewHolder
            viewHolder.CarType     = (TextView) view.findViewById(R.id.info_text);
            viewHolder.imgSelected = (ImageView) view.findViewById(R.id.img_select);
            // store the holder with the view.
            view.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource every time
            // just use the viewHolder instead
            viewHolder = (ViewHolder) view.getTag();
        }

        // object item based on the position
        CarInfoItem obj = mData.get(position);

        // assign values if the object is not null
        if (mData != null) {
            // get the TextView from the ViewHolder and then set the text (item name) and other values
            viewHolder.CarType.setText(obj.getCarType());

            if(obj.getIsSelected())
                viewHolder.imgSelected.setVisibility(View.VISIBLE);
            else
                viewHolder.imgSelected.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    private static class ViewHolder {
        public TextView CarType;
        public ImageView imgSelected;
    }
}
