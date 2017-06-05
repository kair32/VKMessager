package support.DB;

import android.provider.BaseColumns;

public final class DBContract {

    private DBContract() {
    }
    public static final class GuestEntry implements BaseColumns {
        public final static String TABLE_NAME = "guests";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_PHOTO = "URL";
        public final static String COLUMN_ID_VK = "id";
    }
    public static final class DialogsEntry implements BaseColumns{
        public final static String TABLE_NAME = "dialogs";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_PHOTO = "URL";
        public final static String COLUMN_ID_VK = "id";
        public final static String COLUMN_LAST_MSG = "msg";
    }
}
