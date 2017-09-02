package cf.lisuke.passwdbox.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by lisuke on 17-6-18.
 */

/**
 * 帐号实体持有的对应的网站帐号项，使用注解进行Orm映射
 * @author lisuke
 * @see Account
 */
@DatabaseTable(tableName = "account_item_table")
public class Item {
    @DatabaseField(generatedId = true)
    int id;
    //外键，自动获取外键对应的值
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    Account account = null;
    @DatabaseField()
    String site = null;

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", account=" + account +
                ", site='" + site + '\'' +
                ", name='" + name + '\'' +
                ", passwd='" + passwd + '\'' +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @DatabaseField(canBeNull = true)
    String name = null;
    @DatabaseField(canBeNull = true)
    String passwd = null;

}
