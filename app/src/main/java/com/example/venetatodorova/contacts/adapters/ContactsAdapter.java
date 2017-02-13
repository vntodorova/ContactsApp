package com.example.venetatodorova.contacts.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.venetatodorova.contacts.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ContactsAdapter extends CursorAdapter {

    private class ViewHolder{
        TextView textView;
        ImageView imageView;
    }

    private LayoutInflater layoutInflater;
    private HashMap<Uri,Bitmap> map;

    public ContactsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        layoutInflater = LayoutInflater.from(context);
        map = new HashMap<>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = layoutInflater.inflate(R.layout.contacts_list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.textView = (TextView) v.findViewById(R.id.contact_name);
        holder.imageView = (ImageView) v.findViewById(R.id.contact_image);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.textView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        Bitmap image = null;

        if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) != null) {
            Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)));
            if (map.get(imageUri) == null) {
                try {
                    InputStream is = context.getContentResolver().openInputStream(imageUri);
                    image = BitmapFactory.decodeStream(is);
                    map.put(imageUri, image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                image = map.get(imageUri);
            }
            holder.imageView.setImageBitmap(getCircleBitmap(image));
        } else {
            holder.imageView.setImageResource(R.drawable.contact);
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        if(bitmap != null){
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);

            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
            return circleBitmap;
        }
        return bitmap;
    }
}
