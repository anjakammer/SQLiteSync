
# How to use SQLite3 on Android
- turn on the debug mode of your device
- connect your Android device via adb-usb-drivers of the manufactorer
- install and run your application on the device
- insert, update and delete data
- close the application, to close the database
- open the Android terminal emulator

#### run this, to get root permissions
```sh
$ su
```
#### go to the database directory
```sh
$ ls /data/data/com.example.app/databases
```
#### run sqlite3 and load the database
run all sql commands with (;) at the end
```sh
$ sqlite3 database.db
```


#### to get all attributes of a table, run:
```sh
$ PRAGMA table_info(tablename);
```
#### to get all tables in the database, run:
```sh
$ SELECT DISTINCT tbl_name from sqlite_master;
```
