package com.example.venetatodorova.contacts.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.venetatodorova.contacts.models.DrawerElement;
import com.example.venetatodorova.contacts.R;

import java.util.ArrayList;


public class DrawerAdapter extends ArrayAdapter<DrawerElement> {
    private final Activity activity;
    private ArrayList<DrawerElement> drawerElements = new ArrayList<>();
    private FlashListener flashListener;
    private ZoomListener zoomListener;
    private ExposureListener exposureListener;
    private int type;

    public DrawerAdapter(Activity activity,
                         ArrayList<DrawerElement> drawerElementList,
                         FlashListener flashListener,
                         ZoomListener zoomListener,
                         ExposureListener exposureListener) {
        super(activity, 0, drawerElementList);
        this.activity = activity;
        this.drawerElements = drawerElementList;
        this.flashListener = flashListener;
        this.zoomListener = zoomListener;
        this.exposureListener = exposureListener;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        type = drawerElements.get(position).getType();

        switch (type) {
            case DrawerElement.FLASH_CHECKBOX:
                view = checkBoxSetup(view,parent);
                break;
            case DrawerElement.ZOOM_SEEKBAR:
            case DrawerElement.EXPOSURE_SEEKBAR:
                view = seekBarSetup(view,parent);
                break;
            default:
                break;
        }
        return view;
    }

    private SeekBar.OnSeekBarChangeListener exposureChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            exposureListener.onExposurePercentageChange(progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private SeekBar.OnSeekBarChangeListener zoomChangeListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            zoomListener.onZoomPercentageChange(progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private View seekBarSetup(View view, ViewGroup parent) {
        ViewHolderSeekbar viewHolder;
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            viewHolder = new ViewHolderSeekbar();
            view = inflater.inflate(R.layout.list_item_seekbar, parent, false);
            viewHolder.txtTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.seekBar = (SeekBar) view.findViewById(R.id.seekbar);
            if(type == DrawerElement.EXPOSURE_SEEKBAR){
                viewHolder.seekBar.setOnSeekBarChangeListener(exposureChangeListener);
            } else if(type == DrawerElement.ZOOM_SEEKBAR){
                viewHolder.seekBar.setOnSeekBarChangeListener(zoomChangeListener);
            }
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderSeekbar) view.getTag();
        }
        if (type == DrawerElement.ZOOM_SEEKBAR) {
            viewHolder.txtTitle.setText(R.string.zoom);
        } else if (type == DrawerElement.EXPOSURE_SEEKBAR) {
            viewHolder.txtTitle.setText(R.string.exposure);
        }
        return view;
    }

    private View checkBoxSetup(View view, ViewGroup parent) {
        ViewHolderCheckbox viewHolder;
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            viewHolder = new ViewHolderCheckbox();
            view = inflater.inflate(R.layout.list_item_checkbox, parent, false);
            viewHolder.txtTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.button = (CheckBox) view.findViewById(R.id.checkbox);
            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flashListener.onFlashToggle();
                }
            });
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderCheckbox) view.getTag();
        }
        viewHolder.txtTitle.setText(R.string.flash);
        return view;
    }

    private static class ViewHolderSeekbar {
        TextView txtTitle;
        SeekBar seekBar;
    }

    private static class ViewHolderCheckbox {
        TextView txtTitle;
        CheckBox button;
    }

    public interface ExposureListener {
        void onExposurePercentageChange(int percentage);
    }

    public interface ZoomListener {
        void onZoomPercentageChange(int percentage);
    }

    public interface FlashListener {
        void onFlashToggle();
    }
}
