package de.anjakammer.bassa.SQLiteSync;


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
                    "( "+COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_KEY + " TEXT NOT NULL, " +
                    COLUMN_VALUE + " TEXT NOT NULL );";
    public static final String SETTINGS_DROP = "DROP TABLE IF EXISTS " + TABLE_SETTINGS;
    private static final String KEY_IS_MASTER = "isMaster";
    private static final String KEY_DB_ID = "DB_ID";
    private static final String KEY_TABLES = "tables";
    private static final String KEY_LASTSYNCTIME = "lastSyncTime";
    private static final String COLUMN_IS_DELETED = "isDeleted";
    private static final String COLUMN_TIMESTAMP = "timestamp";


    public SQLiteSyncHelper(SQLiteDatabase db, boolean isThisDBMaster, String dbID){
        this.db = db;
        this.dbID = dbID;
        this.isMaster = isThisDBMaster;

        if(!isTableExisting(TABLE_SETTINGS)){
            prepareSyncableDB(this.isMaster, dbID);
            return;
        }

        if(isDbMaster() != this.isMaster) {
            setMaster(this.isMaster);
        }

        if(!getDbId().equals(dbID)) {
            setDbID(dbID);
        }
    }

    private void prepareSyncableDB(boolean isThisBDMaster, String dbID){
        try {
            this.db.execSQL(SETTINGS_CREATE);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "creating failed for table: "+ TABLE_SETTINGS + e.getMessage());
            Log.e(LOG_TAG, "QUERY: "+ SETTINGS_CREATE );
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

    public void tearDownSyncableDB(){
        db.execSQL(SETTINGS_DROP);
    }

    public void delete(String table, long _id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, getTimestamp());
        values.put(COLUMN_IS_DELETED, 1);

        db.update(table,
                values,
                COLUMN_ID + " = " + _id,
                null);

        Log.d(LOG_TAG, "Object marked as deleted in table: "+ table+ ", _id: " + _id );
    }

    public void update(String table, long _id, ContentValues values){
        values.put(COLUMN_TIMESTAMP, getTimestamp());
        db.update(table,
                values,
                COLUMN_ID + " = " + _id,
                null);

        Log.d(LOG_TAG, "Object updated in table: "+ table +", _id: " + _id );
    }

    public long insert(String table, ContentValues values){
        values.put(COLUMN_TIMESTAMP, getTimestamp());

        long _id = -1;
        try {
            _id = db.insert(table, null, values);
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "inserting failed: "+  e.getMessage());
        }
        Log.d(LOG_TAG, "Object updated in table: "+ table +", _id: " + _id );
        return _id;
    }

    public Cursor select(boolean distinct, String table, String[] columns,
                         String selection, String[] selectionArgs, String groupBy,
                         String having, String orderBy, String limit){

        // TODO which are NOT deleted
//        if(selection)
        return db.query(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    public JSONObject getDelta(JSONObject peer) {

        // TODO test-data from peer delta
        String lastSyncTime = "1462109540";
        peer = new JSONObject();
        try {
            peer.put(KEY_LASTSYNCTIME,lastSyncTime);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing test-peer JSON: " + e.getMessage());
        }
        // TODO test-data from peer delta

        JSONObject delta = prepareDeltaObject(lastSyncTime, getDbId());

        List<String> tables = getSyncableTableNames();
        for (String table: tables) {
            Cursor cursor = this.db.query(
                    table, new String[] {COLUMN_TIMESTAMP}, COLUMN_TIMESTAMP +" >= '?'",
                    new String[] {lastSyncTime}
                    ,null, null, null
            );
            // TODO fill delta JSONObject
            cursor.close();
        }
        return delta;

    }

    private JSONObject prepareDeltaObject(String lastSyncTime, String dbId){
        JSONObject delta = new JSONObject();
        try {
            delta.put(KEY_DB_ID, dbId);
            delta.put(KEY_IS_MASTER,String.valueOf(this.isMaster));
            delta.put(KEY_LASTSYNCTIME,lastSyncTime);
            delta.put(KEY_TABLES,new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error while preparing delta: " + e.getMessage());
        }
        return delta;
    }

    private String getDbId(){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS,new String[] {COLUMN_VALUE},COLUMN_KEY + " = ?",new String[] {KEY_DB_ID}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
        String DbId = cursor.getString(valueIndex);
        cursor.close();
        return DbId;
    }

    public List<String> getSyncableTableNames(){
        List<String> result = new ArrayList<>();
        Cursor cursor = this.db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master", null);
        // TODO nur tabellen in die Liste schmeißen, die auch is_deleted und timestamp haben
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        result.remove(TABLE_SETTINGS);
        return result;
    }

    public void makeTableSyncable(String table){
        try{
            addIsDeletedColumn(table);
        }catch(Exception e){
            e.printStackTrace();
        }
        addTimestampColumn(table);
    }

    private void addIsDeletedColumn(String table){
        List<String> columns = getAllColumns(table);
        if(!columns.contains(COLUMN_IS_DELETED)){
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_IS_DELETED + " INTEGER");
        }
    }

    private void addTimestampColumn(String table){
        List<String> columns = getAllColumns(table);
        if(!columns.contains(COLUMN_TIMESTAMP)){
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_TIMESTAMP + " TEXT");
        }
    }

    boolean isTableExisting(String tableName)
    {
        if (tableName == null || this.db == null || !this.db.isOpen())
        {
            return false;
        }
        Cursor cursor = this.db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
                new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public boolean isDbMaster(){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS,new String[] {COLUMN_VALUE},COLUMN_KEY + " = ?",new String[] {KEY_IS_MASTER}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
        boolean isThisBDMaster = cursor.getInt(valueIndex)== 1;
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

    private String getTimestamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    public List<String> getAllColumns(String table){
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + table , null);
        cursor.moveToFirst();
        List<String> columns =  Arrays.asList(cursor.getColumnNames());
        cursor.close();
        return columns;
    }
}
