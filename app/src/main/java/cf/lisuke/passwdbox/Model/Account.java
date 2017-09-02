package cf.lisuke.passwdbox.Model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by lisuke on 17-6-18.
 */

/**
 * 登录的帐号实体，使用注解进行Orm映射
 * @author lisuke
 * @see Item
 */
@DatabaseTable(tableName = "account_table")
public class Account {

    @DatabaseField(unique = true,id = true)
    private String email = null;

    @DatabaseField(canBeNull = false)
    private String passwd = null;

    // 持有外键，懒加载
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Item> items = null;

    public ForeignCollection<Item> getItems() {
        return items;
    }

    public void setItems(ForeignCollection<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Account{" +
                "email='" + email + '\'' +
                ", passwd='" + passwd + '\'' +
                ", items=" + items +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
