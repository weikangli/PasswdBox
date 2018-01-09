package cf.lisuke.passwdbox;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import cf.lisuke.passwdbox.DBUtils.DatabaseHelper;
import cf.lisuke.passwdbox.Model.Account;
import cf.lisuke.passwdbox.Model.Item;


/**
 * 登录之后进入的Activity，在该Activity处理对网站帐号业务的实现
 * 增 删 查 改
 * @author liweikang
 */
public class MainActivity extends AppCompatActivity {
    //当前登录的帐号
    private static Account account = null;
    //适配器，持有要显示的数据的集合
    private SimpleAdapter adapter = null;
    //持有的所有数据都存放在这
    List<Map<String,String>> list = null;
    ListView listView = null;
    private Dao dao = null;
    FloatingActionButton add;
    private Dao itemDao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String user = getIntent().getStringExtra("user");

        try {
            itemDao = new DatabaseHelper(this).getDao(Item.class);
            dao = new DatabaseHelper(this).getDao(Account.class);
            account = (Account) dao.queryForId(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.list);

        list = new LinkedList<Map<String, String>>();
        // 使用SimpleAdapter适配器
        adapter = new SimpleAdapter(this,list,R.layout.list_view_item_layout,new String[]{"mName","mSite"},new int[]{R.id.name,R.id.site});
        listView.setAdapter(adapter);
        //登录自动加载
        sync();
        //为列表项绑定点击监听器，实现查看帐号，密码
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                System.out.println("add");
                LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.item_show, null);

                final PopupWindow popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popupwindow_Background));
                popupWindow.showAsDropDown(add);
                TextView name = (TextView) popupWindow.getContentView().findViewById(R.id.show_name);
                TextView site = (TextView) popupWindow.getContentView().findViewById(R.id.show_site);
                TextView passwd = (TextView) popupWindow.getContentView().findViewById(R.id.show_password);
                Map<String,String> map = list.get(position);

                name.setText(map.get("mName"));
                site.setText(map.get("mSite"));
                passwd.setText(map.get("mPasswd"));
            }
        });

        //为列表项添加长按监听器，弹出菜单项，选择增删查改
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);

                popup.getMenuInflater()
                        .inflate(R.menu.poupup_menu_home, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.sync){
                            sync();
                        } else if (item.getItemId() == R.id.add){
                            add();
                        } else if (item.getItemId() == R.id.delete){
                            delete(position);
                        } else if (item.getItemId() == R.id.modify){
                            modify(position);
                        }
                        return true;
                    }
                });
                popup.show();

                return false;
            }
        });

        add = (FloatingActionButton) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
        FloatingActionButton sync = (FloatingActionButton) findViewById(R.id.sync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });
    }
    //删除
    private void delete(int order) {
        System.out.println("delete:"+order);
        Map<String,String> map = list.remove(order);
        int id = Integer.valueOf(map.get("mId"));
        try {
            itemDao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sync();
    }
    //修改
    private void modify(int order) {
        System.out.println("modify:"+order);
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.item_modify, null);

        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popupwindow_Background));
        popupWindow.showAsDropDown(add);
        Button btn = (Button) popupWindow.getContentView().findViewById(R.id.modify);
        final AutoCompleteTextView name = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.modify_name);
        final AutoCompleteTextView site = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.modify_site);
        final AutoCompleteTextView passwd = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.modify_password);

        final Map<String,String> map = list.get(order);
        name.setText(map.get("mName"));
        site.setText(map.get("mSite"));
        passwd.setText(map.get("mPasswd"));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (name.getText().length() < 1){
                    return;
                }
                if (passwd.getText().length() <1 ){
                    return;
                }
                Item item = null;
                try {
                    item = (Item) itemDao.queryForId(map.get("mId"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                item.setName(name.getText().toString());
                item.setPasswd(passwd.getText().toString());
                item.setSite(site.getText().toString());

                try {
                    itemDao.update(item);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                sync();
                popupWindow.dismiss();
            }
        });

    }
    //添加
    public void add(){
        System.out.println("add");
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.item_add, null);

        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popupwindow_Background));
        popupWindow.showAsDropDown(add);
        Button btn = (Button) popupWindow.getContentView().findViewById(R.id.register);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteTextView name = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.add_name);
                AutoCompleteTextView site = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.add_site);
                AutoCompleteTextView passwd = (AutoCompleteTextView) popupWindow.getContentView().findViewById(R.id.add_password);
                if (name.getText().length() < 1){
                    return;
                }
                if (passwd.getText().length() <1 ){
                    return;
                }
                Item item = new Item();
                item.setName(name.getText().toString());
                item.setPasswd(passwd.getText().toString());
                item.setSite(site.getText().toString());
                item.setAccount(account);
                insert(item);
                popupWindow.dismiss();
            }
        });
        sync();
    }
    //创建一条新密码记录
    private void insert(Item item) {
        try {
            item.setAccount(account);
            itemDao.create(item);
            sync();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //列表项，刷新同步
    public void sync(){
        try {
            account = (Account) dao.queryForId(account.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ForeignCollection<Item> items = account.getItems();
        list.clear();

        listView.setAdapter(adapter);
        for (Item item:items){
            Map<String,String> map = new HashMap<String,String>();
            map.put("mName",item.getName());
            map.put("mSite",item.getSite());
            map.put("mPasswd",item.getPasswd());
            map.put("mId",String.valueOf(item.getId()));
            list.add(map);
        }

        listView.invalidateViews();
    }
}
