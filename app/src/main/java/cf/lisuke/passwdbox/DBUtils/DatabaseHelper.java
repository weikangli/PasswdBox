package cf.lisuke.passwdbox.DBUtils;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cf.lisuke.passwdbox.Model.Account;
import cf.lisuke.passwdbox.Model.Item;

/**
 * Created by lisuke on 17-6-14.
 */


public  class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
    private static String DatabasePath = "sqlite.db";
    //持有Dao，缓冲
    private Map<String, Dao> daos = new HashMap<String, Dao>();

    public DatabaseHelper(Context context)
    {
        this(context, DatabasePath);
    }

    public DatabaseHelper(Context context, String DatabasePath)
    {
        super(context, DatabasePath, null, 4);
    }
    //获取数据库文件的保存路径
    public static String getDatabasePath() {
        return DatabasePath;
    }

    public static void setDatabasePath(String databasePath) {
        DatabasePath = databasePath;
    }

    /**
     * 根据Orm 映射 自动的创建SQLite Table
     * @param database
     * @param connectionSource
     */
    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource)
    {
        try
        {
            TableUtils.createTableIfNotExists(connectionSource, Account.class);
            TableUtils.createTableIfNotExists(connectionSource, Item.class);

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新创建数据库表
     * @param database
     * @param connectionSource
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        try
        {
            TableUtils.dropTable(connectionSource, Account.class, true);
            TableUtils.dropTable(connectionSource, Item.class, true);

            onCreate(database, connectionSource);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static DatabaseHelper instance;



    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context)
    {
        context = context.getApplicationContext();
        if (instance == null)
        {
            synchronized (DatabaseHelper.class)
            {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }

        return instance;
    }

    /**
     * 获取Dao
     * 单例
     * 缓冲层
     * @param clazz
     * @return
     * @throws SQLException
     */
    @Override
    public synchronized Dao getDao(Class clazz) throws SQLException
    {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (daos.containsKey(className))
        {
            dao = daos.get(className);
        }
        if (dao == null)
        {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close()
    {
        super.close();

        for (String key : daos.keySet())
        {
            Dao dao = daos.get(key);
            dao = null;
        }
    }

}