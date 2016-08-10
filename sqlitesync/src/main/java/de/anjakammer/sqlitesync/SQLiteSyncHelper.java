package de.anjakammer.sqlitesync;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class SQLiteSyncHelper {

    private boolean isMaster;
    private String dbID;
    private SQLiteDatabase db;
    private static final String LOG_TAG = SQLiteSyncHelper.class.getSimpleName();
    private static final String TABLE_SETTINGS = "Settings";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";
    private static final String SETTINGS_CREATE =
            "CREATE TABLE " + TABLE_SETTINGS +
                    "( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_KEY + " TEXT NOT NULL, " +
                    COLUMN_VALUE + " TEXT NOT NULL );";
    public static final String SETTINGS_DROP = "DROP TABLE IF EXISTS " + TABLE_SETTINGS;
    private static final String KEY_IS_MASTER = "isMaster";
    private static final String KEY_DB_ID = "dbId";
    private static final String KEY_INSTANCE_NAME = "name";
    private static final String KEY_TABLES = "tables";
    private static final String KEY_MESSAGE = "message";
    public static final String KEY_LAST_SYNC_TIME = "lastSyncTime";
    private static final String VALUE_DELTA = "DELTA";
    private static final String COLUMN_IS_DELETED = "isDeleted";
    private static final String COLUMN_TIMESTAMP = "timestamp";


    public SQLiteSyncHelper(SQLiteDatabase db, boolean isThisDBMaster, String dbID) {
        this.db = db;
        this.dbID = dbID;
        this.isMaster = isThisDBMaster;

        if (!isTableExisting(TABLE_SETTINGS)) {
            prepareSyncableDB(this.isMaster, dbID);
            return;
        }

        if (isDbMaster() != this.isMaster) {
            setMaster(this.isMaster);
        }

        if (!getDbId().equals(dbID)) {
            setDbID(dbID);
        }
    }

    private void prepareSyncableDB(boolean isThisBDMaster, String dbID) {
        try {
            this.db.execSQL(SETTINGS_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "creating failed for table: " + TABLE_SETTINGS + e.getMessage());
            Log.e(LOG_TAG, "QUERY: " + SETTINGS_CREATE);
        }

        // Insert isMaster Key-Value pair
        insertValue(KEY_IS_MASTER, (isThisBDMaster) ? 1 : 0);

        // Insert dbID Key-Value pair
        insertValue(KEY_DB_ID, dbID);

        // Insert instanceName Key-Value Pair
        insertValue(KEY_INSTANCE_NAME, android.os.Build.MODEL);

        // Insert last sync time Key-Value Pair
        insertValue(KEY_LAST_SYNC_TIME, new Timestamp(0).toString());
    }

    public void tearDownSyncableDB() {
        db.execSQL(SETTINGS_DROP);
    }

    public void delete(String table, long _id) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, getTimestamp());
        values.put(COLUMN_IS_DELETED, 1);

        this.db.update(table,
                values,
                COLUMN_ID + " = " + _id,
                null);

        Log.d(LOG_TAG, "Object marked as deleted in table: " + table + ", _id: " + _id);
    }

    public boolean update(String table, long _id, ContentValues values) {
        if(values.get(COLUMN_TIMESTAMP) == null){
            values.put(COLUMN_TIMESTAMP, getTimestamp());
        }
        int update_id = this.db.update(table,
                values,
                COLUMN_ID + " = " + _id,
                null);
        if(update_id < 0){
            Log.d(LOG_TAG, "updating an Object failed for table: " + table + ", _id: " + _id);
            return false;
        }
        Log.d(LOG_TAG, "Object updated in table: " + table + ", _id: " + _id);

        return true;
    }

    public long insert(String table, ContentValues values) {
        values.put(COLUMN_TIMESTAMP, getTimestamp());

        long _id = -1;
        try {
            _id = db.insert(table, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "inserting failed: " + e.getMessage());
        }
        Log.d(LOG_TAG, "Object updated in table: " + table + ", _id: " + _id);
        return _id;
    }

    public Cursor select(boolean distinct, String table, String[] columns,
                         String selection, String[] selectionArgs, String groupBy,
                         String having, String orderBy, String limit) {

        if (selection != null) {
            selection += " AND " + COLUMN_IS_DELETED + " = 0";
        } else {
            selection = COLUMN_IS_DELETED + " = 0";
        }

        return db.query(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    public Cursor selectDeleted(boolean distinct, String table, String[] columns,
                                String selection, String[] selectionArgs, String groupBy,
                                String having, String orderBy, String limit) {

        if (selection != null) {
            selection += " AND " + COLUMN_IS_DELETED + " = 1";
        } else {
            selection = COLUMN_IS_DELETED + " = 1";
        }

        return db.query(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }


    public boolean updateDB(JSONObject delta) throws JSONException, SyncableDatabaseException {
        boolean updated;

        Timestamp time =  new Timestamp(System.currentTimeMillis());
        Log.d(LOG_TAG, "Started updating DB at: " + time.toString() );

        if (delta.getString(KEY_DB_ID).equals(this.dbID) &&
                delta.getString(KEY_MESSAGE).equals(VALUE_DELTA)) {

            JSONObject tables = delta.getJSONObject(KEY_TABLES);

            // Iterates through all tables
            JSONArray tableNames = tables.names();
            for (int i = 0; i < tableNames.length(); i++) {
                String tableName = tables.names().getString(i);
                JSONArray table = tables.getJSONArray(tableName);

                // Iterates through all tuples
                for (int ii = 0; ii < table.length(); ii++) {
                    JSONObject tuple = table.getJSONObject(ii);

                    ContentValues values = new ContentValues();
                    // Iterates through tuple
                    for (int iii = 0; iii < tuple.length(); iii++) {
                        String key = tuple.names().getString(iii);

                        values.put(key, tuple.getString(key));
                    }
                    // it is needed, that the tuple do exist. It just updates, no inserting
                    updated = update(tableName, values.getAsLong("_id"), values);
                    if (!updated) {
                        return false;
                    }

                }
            }
        }else{
            throw new SyncableDatabaseException("The provided data are no Database-delta");
        }
        Timestamp endtime =  new Timestamp(System.currentTimeMillis());
        Log.d(LOG_TAG, "Ended updating DB at " + endtime.toString() );
        return true;
    }

    public JSONObject getDelta(String lastSyncTime) throws JSONException {

        JSONObject delta = prepareDeltaObject(lastSyncTime, getDbId());

        List<String> tableNames = getSyncableTableNames();
        JSONObject tables = new JSONObject();

        for (String tableName : tableNames) {

            Cursor cursor = this.db.query(
                    tableName, null, COLUMN_TIMESTAMP + " >= ?",
                    new String[]{lastSyncTime}
                    , null, null, null
            );

            String[] columnNames = cursor.getColumnNames();
            JSONArray table = new JSONArray();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                JSONObject tuple = new JSONObject();
                for (String columnName : columnNames) {
                    try {
                        tuple.put(columnName, cursor.getString(cursor.getColumnIndex(columnName)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "JSONObject error for writing delta JSON for columnName: " +
                                columnName + " in table " + tableName + ": \n" + e.getMessage());
                    }
                }
                table.put(tuple);
                cursor.moveToNext();
            }
            cursor.close();
            try {
                tables.put(tableName, table);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "JSONObject error for writing delta JSON for table: " +
                        tableName + ": \n" + e.getMessage());
            }
        }

        try {
            delta.put("tables", tables);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error while adding tables to delta: " + e.getMessage());
        }
        return delta;
    }

    private JSONObject prepareDeltaObject(String lastSyncTime, String dbId) {
        JSONObject delta = new JSONObject();
        try {
            delta.put(KEY_MESSAGE, VALUE_DELTA);
            delta.put(KEY_INSTANCE_NAME, getInstanceName());
            delta.put(KEY_DB_ID, dbId);
            delta.put(KEY_IS_MASTER, String.valueOf(this.isMaster));
            delta.put(KEY_LAST_SYNC_TIME, lastSyncTime);
            delta.put(KEY_TABLES, new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error while preparing delta: " + e.getMessage());
        }
        return delta;
    }

    public List<String> getSyncableTableNames() {

        List<String> tableNames = new ArrayList<>();
        Cursor cursor = this.db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            tableNames.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();


        List<String> result = new ArrayList<>();
        for (String tableName : tableNames) {

            Cursor columns = db.query(false, tableName
                    , null, null, null,
                    null, null, null, "1");
            columns.moveToFirst();
            int isDeleted = columns.getColumnIndex(COLUMN_IS_DELETED);
            int timestamp = columns.getColumnIndex(COLUMN_TIMESTAMP);
            columns.close();

            if (isDeleted >= 0 && timestamp >= 0) {
                result.add(tableName);
            }
        }

        result.remove(TABLE_SETTINGS);
        return result;
    }

    public void makeTableSyncable(String table) {
        try {
            addIsDeletedColumn(table);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addTimestampColumn(table);
    }

    private void addIsDeletedColumn(String table) {
        List<String> columns = getAllColumns(table);
        if (!columns.contains(COLUMN_IS_DELETED)) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_IS_DELETED + " INTEGER DEFAULT 0");
        }
    }

    private void addTimestampColumn(String table) {
        List<String> columns = getAllColumns(table);
        if (!columns.contains(COLUMN_TIMESTAMP)) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_TIMESTAMP + " TEXT");
        }
    }

    boolean isTableExisting(String tableName) {
        if (tableName == null || this.db == null || !this.db.isOpen()) {
            return false;
        }
        Cursor cursor = this.db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
                new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public boolean isDbMaster() {
        Cursor cursor = this.db.query(
                TABLE_SETTINGS, new String[]{COLUMN_VALUE}, COLUMN_KEY + " = ?", new String[]{KEY_IS_MASTER}
                , null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
        boolean isThisBDMaster = cursor.getInt(valueIndex) == 1;
        cursor.close();
        return isThisBDMaster;
    }

    public void setMaster(boolean isThisBDMaster) {
        this.isMaster = isThisBDMaster;
        int intValueIsMaster = (this.isMaster) ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, intValueIsMaster);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + " = ?"  ,
                new String[]{KEY_IS_MASTER});
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;

        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, this.dbID);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + " = ?"  ,
                new String[]{KEY_DB_ID});
    }

    private String getDbId() {
        return getValue(KEY_DB_ID);
    }

    private String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public List<String> getAllColumns(String table) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + table, null);
        cursor.moveToFirst();
        List<String> columns = Arrays.asList(cursor.getColumnNames());
        cursor.close();
        return columns;
    }

    public void setInstanceName(String newName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, newName);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + " = ?"  ,
                new String[]{KEY_INSTANCE_NAME});
    }

    public String getInstanceName() {
        return getValue(KEY_INSTANCE_NAME);
    }

    private String getValue(String key){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS, new String[]{COLUMN_VALUE}, COLUMN_KEY + " = ?", new String[]{key}
                , null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
        String value = cursor.getString(valueIndex);
        cursor.close();
        return value;
    }

    private boolean insertValue(String key, long value){
        ContentValues keyValue = new ContentValues();
        keyValue.put(COLUMN_KEY, key);
        keyValue.put(COLUMN_VALUE, value);
        long _id = this.db.insert(TABLE_SETTINGS, null, keyValue);
        return _id > -1;
    }

    private boolean insertValue(String key, String value){
        ContentValues keyValue = new ContentValues();
        keyValue.put(COLUMN_KEY, key);
        keyValue.put(COLUMN_VALUE, value);
        long _id = this.db.insert(TABLE_SETTINGS, null, keyValue);
        return _id > -1;
    }

    public long insertIfNotExists(String table, ContentValues values, String[] uniqueColumns) throws SyncableDatabaseException {
        for (String column: uniqueColumns) {
            if(!values.containsKey(column)){
                throw new SyncableDatabaseException(
                        "Column "+ column + "does not exists in KeySet. Unable to insert item.");
            }
        }
        return this.db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
