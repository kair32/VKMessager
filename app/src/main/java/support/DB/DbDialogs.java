package support.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import support.DB.DBContract.DialogsEntry;


public class DbDialogs extends SQLiteOpenHelper {

    public static final String LOG_TAG = DbHelper.class.getSimpleName();

    //Имя файла базы данных
    private static final String DATABASE_NAME = "Dialogs.db";

    //Версия базы данных. При изменении схемы увеличить на единицу
    private static final int DATABASE_VERSION = 1;

    /**
     * Конструктор {@link DbHelper}.
     *
     * @param context Контекст приложения
     */
    public DbDialogs(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Вызывается при создании базы данных
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Строка для создания таблицы
        String SQL_CREATE_GUESTS_TABLE = "CREATE TABLE " + DialogsEntry.TABLE_NAME + " ("
                + DialogsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DialogsEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + DialogsEntry.COLUMN_ID_VK + " TEXT NOT NULL, "
                + DialogsEntry.COLUMN_LAST_MSG + " TEXT NOT NULL, "
                + DialogsEntry.COLUMN_PHOTO + " TEXT NOT NULL);";
        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_GUESTS_TABLE);
    }
    //Вызывается при обновлении схемы базы даннных
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
