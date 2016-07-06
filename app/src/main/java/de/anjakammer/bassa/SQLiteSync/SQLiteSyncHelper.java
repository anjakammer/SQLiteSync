package de.anjakammer.bassa.SQLiteSync;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import de.anjakammer.bassa.Exceptions.SyncableDatabaseException;

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
    private static final String KEY_DB_ID = "DB_ID";
    private static final String KEY_TABLES = "tables";
    private static final String KEY_LASTSYNCTIME = "lastSyncTime";
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
        int intValueIsMaster = (isThisBDMaster) ? 1 : 0;
        ContentValues isMasterKeyValue = new ContentValues();
        isMasterKeyValue.put(COLUMN_KEY, KEY_IS_MASTER);
        isMasterKeyValue.put(COLUMN_VALUE, intValueIsMaster);
        this.db.insert(TABLE_SETTINGS, null, isMasterKeyValue);

        // Insert dbID Key-Value pair
        ContentValues dbIDKeyValue = new ContentValues();
        dbIDKeyValue.put(COLUMN_KEY, KEY_DB_ID);
        dbIDKeyValue.put(COLUMN_VALUE, dbID);
        this.db.insert(TABLE_SETTINGS, null, dbIDKeyValue);
    }

    public void tearDownSyncableDB() {
        db.execSQL(SETTINGS_DROP);
    }

    public void delete(String table, long _id) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, getTimestamp());
        values.put(COLUMN_IS_DELETED, 1);

        db.update(table,
                values,
                COLUMN_ID + " = " + _id,
                null);

        Log.d(LOG_TAG, "Object marked as deleted in table: " + table + ", _id: " + _id);
    }

    public boolean update(String table, long _id, ContentValues values) {
        if(values.get(COLUMN_TIMESTAMP) == null){
            values.put(COLUMN_TIMESTAMP, getTimestamp());
        }

        try {
            int update_id = db.update(table,
                    values,
                    COLUMN_ID + " = " + _id,
                    null);
            if(update_id < 0){
                Log.d(LOG_TAG, "updating an Object failed for table: " + table + ", _id: " + _id);
                return false;
            }
                Log.d(LOG_TAG, "Object updated in table: " + table + ", _id: " + _id);
        }catch(Exception e){
            Log.d(LOG_TAG, "updating an Object failed for table: " + table + ", _id: " + _id);
            return false;
        }
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


    public boolean updateDB(JSONObject peer) throws JSONException {
        boolean updated;

        if (peer.getString(KEY_DB_ID).equals(this.dbID)) {

            JSONArray tables = peer.getJSONArray(KEY_TABLES);


            // Iterates through all tables
            for (int i = 0; i < tables.length(); i++) {
                JSONObject table = tables.getJSONObject(i);
                String tableName = table.names().getString(0);

                // Iterates through all tuples
                for (int ii = 0; ii < table.length(); ii++) {
                    JSONArray tuples = table.getJSONArray(tableName);

                    // Iterates through tuple
                    for (int iii = 0; iii < tuples.length(); iii++) {
                        JSONObject tuple = tuples.getJSONObject(iii);
                        JSONArray tupleKeys = tuple.names();

                        ContentValues values = new ContentValues();

                        // Iterates through all keys
                        for (int iiii = 0; iiii < tupleKeys.length(); iiii++) {
                            String key = tupleKeys.getString(iiii);
                            values.put(key, tuple.getString(key));
                        }
                        // TODO kann nur geupdated werden, die Tupel müssen vorher existieren !
                        updated = update(tableName, values.getAsLong("_id"), values);
                        if (!updated) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public JSONObject getDelta(JSONObject peer) throws SyncableDatabaseException {
        String lastSyncTime = null;
        try {
            if (!peer.getString(KEY_DB_ID).equals(this.dbID)) {
                throw new SyncableDatabaseException(
                        "Database ID of Peer does not match this application Database ID");
            }
            lastSyncTime = peer.getString(KEY_LASTSYNCTIME);
        } catch (JSONException e) {
            throw new SyncableDatabaseException(
                    "JSONObject error for getting lastSyncTime from peer: "
                            + e.getMessage());
        }

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
            delta.put(KEY_DB_ID, dbId);
            delta.put(KEY_IS_MASTER, String.valueOf(this.isMaster));
            delta.put(KEY_LASTSYNCTIME, lastSyncTime);
            delta.put(KEY_TABLES, new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error while preparing delta: " + e.getMessage());
        }
        return delta;
    }

    private String getDbId() {
        Cursor cursor = this.db.query(
                TABLE_SETTINGS, new String[]{COLUMN_VALUE}, COLUMN_KEY + " = ?", new String[]{KEY_DB_ID}
                , null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
        String DbId = cursor.getString(valueIndex);
        cursor.close();
        return DbId;
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
                COLUMN_KEY + "=" + KEY_IS_MASTER,
                null);
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;

        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, this.dbID);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + "=" + KEY_DB_ID,
                null);
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
}
