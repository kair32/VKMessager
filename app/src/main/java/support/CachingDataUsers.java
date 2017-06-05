package support;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kirill.vkmessager.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import support.DB.DBContract;
import support.DB.DbDialogs;
import support.DB.DbHelper;


/**
 * Created by Kirill on 09.04.2017.
 */

public class CachingDataUsers {
    //кэширование диалогов
    public VKUsersArray CheckDialogs(Context mContext){
        VKUsersArray usersArray = null;
        DbDialogs mDbDialogs = new DbDialogs(mContext);;
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbDialogs.getReadableDatabase();
        // Зададим условие для выборки - список столбцов
        String[] projection = {
                DBContract.DialogsEntry._ID,
                DBContract.DialogsEntry.COLUMN_NAME,
                DBContract.DialogsEntry.COLUMN_ID_VK,
                DBContract.DialogsEntry.COLUMN_LAST_MSG,
                DBContract.DialogsEntry.COLUMN_PHOTO};
        // Делаем запрос
        Cursor cursor = db.query(
                DBContract.DialogsEntry.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order
        try {
            // Узнаем индекс каждого столбца
            usersArray = new VKUsersArray();
            int position = cursor.getColumnIndex(DBContract.DialogsEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(DBContract.DialogsEntry.COLUMN_NAME);
            int photoColumnIndex = cursor.getColumnIndex(DBContract.DialogsEntry.COLUMN_PHOTO);
            int lastMsgColumnIndex = cursor.getColumnIndex(DBContract.DialogsEntry.COLUMN_LAST_MSG);
            int idColumnIndex = cursor.getColumnIndex(DBContract.DialogsEntry.COLUMN_ID_VK);
            // Проходим через все ряды
            while (cursor.moveToNext()) {
                VKApiUserFull full = new VKApiUserFull();
                full.photo_100 = cursor.getString(photoColumnIndex);
                full.last_name = cursor.getString(nameColumnIndex);
                full.first_name = "";
                full.id = cursor.getInt(idColumnIndex);
                full.books = cursor.getString(lastMsgColumnIndex);
                usersArray.add(position,full);
                position++;
                // Используем индекс для получения строки или числа
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }
        db.close();
        return usersArray;
    }
    public void StartCashDialog(String name, String photo, String lastMsg, int id, Context mContext){
        DbDialogs mDbDialogs = new DbDialogs(mContext);
        // Gets the database in write mode
        SQLiteDatabase db = mDbDialogs.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(DBContract.DialogsEntry.COLUMN_NAME, name);
        values.put(DBContract.DialogsEntry.COLUMN_ID_VK, id);
        values.put(DBContract.DialogsEntry.COLUMN_LAST_MSG, lastMsg);
        values.put(DBContract.DialogsEntry.COLUMN_PHOTO, photo);

        db.insert(DBContract.DialogsEntry.TABLE_NAME, null, values);
        db.close();
    }

    //кэширование имён
    public VKUsersArray CheckFileText(Context mContext){
        VKUsersArray usersArray = null;
        DbHelper mDbHelper = new DbHelper(mContext);;
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Зададим условие для выборки - список столбцов
        String[] projection = {
                DBContract.GuestEntry._ID,
                DBContract.GuestEntry.COLUMN_NAME,
                DBContract.GuestEntry.COLUMN_ID_VK,
                DBContract.GuestEntry.COLUMN_PHOTO};
        // Делаем запрос
        Cursor cursor = db.query(
                DBContract.GuestEntry.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order
        try {
            // Узнаем индекс каждого столбца
            usersArray = new VKUsersArray();
            int position = cursor.getColumnIndex(DBContract.GuestEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(DBContract.GuestEntry.COLUMN_NAME);
            int photoColumnIndex = cursor.getColumnIndex(DBContract.GuestEntry.COLUMN_PHOTO);
            int idColumnIndex = cursor.getColumnIndex(DBContract.GuestEntry.COLUMN_ID_VK);
            // Проходим через все ряды
            while (cursor.moveToNext()) {
                VKApiUserFull full = new VKApiUserFull();
                full.photo_100 = cursor.getString(photoColumnIndex);
                full.last_name = cursor.getString(nameColumnIndex);
                full.first_name = "";
                full.id = cursor.getInt(idColumnIndex);
                usersArray.add(position,full);
                position++;
                // Используем индекс для получения строки или числа
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }
        db.close();
        return usersArray;
    }
    public  void StartCachText(String name, String photo, int id, Context mContext){
        DbHelper mDbHelper = new DbHelper(mContext);
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(DBContract.GuestEntry.COLUMN_NAME, name);
        values.put(DBContract.GuestEntry.COLUMN_ID_VK, id);
        values.put(DBContract.GuestEntry.COLUMN_PHOTO, photo);

        db.insert(DBContract.GuestEntry.TABLE_NAME, null, values);
        db.close();
    }

    //кэширование фотографий
    public boolean  CheckFilePhoto(Context mContext, final String photo, final ImageView imageview) {
        ContextWrapper cw = new ContextWrapper(mContext);
        File file = cw.getDir("photo", Context.MODE_PRIVATE);
        cw.deleteDatabase("photo");
        File[] files = file.listFiles();
        for (int i =0; i < files.length; i++) {
            String str = photo.replace(".", "").replace("/", "").replace(":", "") + ".png";
            if (str.equals(files[i].getName()))
            {
                File fil = new File(files[i].getAbsolutePath());
                Picasso.with(mContext)
                        .load(fil)
                        .error(R.drawable.ic_error_outline_black)
                        .into(imageview);
                return false;}
        }
        return true;
    }
    public Target StartCach(final Context mContext, final String photo, final ImageView imageView) {
        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
                OutputStream fout = null;
                try {
                    ContextWrapper cw = new ContextWrapper(mContext);
                    File root = cw.getDir("photo", Context.MODE_PRIVATE);
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    String str = photo.replace(":", "").replace("/", "").replace(".", "");
                    File file = new File(root, str + ".png");
                    fout = new FileOutputStream(file);
                    Log.d("TAP END FILE", " " + file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
                    fout.flush();
                    fout.close();
                } catch (Exception e) {
                    Log.d("TAP", "ERROR" + e.getMessage());
                }
                // изображение успешно скачано
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                // при загрузке произошла ошибка
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        return mTarget;
    }
}
