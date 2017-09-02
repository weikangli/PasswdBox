package cf.lisuke.passwdbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cf.lisuke.passwdbox.DBUtils.DatabaseHelper;
import cf.lisuke.passwdbox.Model.Account;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * 一个登录界面
 * <p>一个登录界面LoginActivity，继承自 AppCompatActivity,实现了LoaderCallbacks<Cursor>接口</p>
 * <p>实现了登录功能</p>
 * @author lisuke
 * @version v0.1
 * @package cf.lisuke.passwdbox
 * @see AppCompatActivity
 */

public class LoginActivity extends AppCompatActivity {


    private UserLoginTask mAuthTask = null;


    /**
     * <p></p>
     */
    private AutoCompleteTextView mEmailView;
    /**
     * <p></p>
     */

    private EditText mPasswordView;
    /**
     * <p></p>
     */
    private View mProgressView;
    /**
     * <p></p>
     */
    private View mLoginFormView;

    /**
     * 程序的入口，加载界面XML信息，初始化mEmailView，mPasswordView，mLoginFormView，mProgressView,
     * @param savedInstanceState
     * @see #mEmailView #mPasswordView #mLoginFormView #mProgressView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * <p>尝试登录</p>
     * <p>判断输入格式错误，如（email,password）有错</p>
     * @see #isPasswordValid(String)  查看{@link #isPasswordValid(String)}描述
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);


        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {
            //开始登录
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * <p>isEmailValid 该方法判断是否是Email地址.</p>
     * @param email
     * @return true 是 Email 地址
     * @return false 不是 Email 地址
     */
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    /**
     * <p>isPasswordValid 该方法判断是否是Email地址.</p>
     * @param password
     * @return true 符合密码要求 长度 >4
     * @return false 不符合密码要求
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * <p>显示登录加载进度</p>
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 一个内部异步任务类，实现了登录注册功能
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        /**
         * 后台的登录操作
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(Void... params) {

            //登录
            DatabaseHelper databaseHelper = new DatabaseHelper(LoginActivity.this);
            try {
                Dao dao = databaseHelper.getDao(Account.class);

                Account account = (Account) dao.queryForId(mEmail);

                if(account==null){
                    account = new Account();
                    account.setEmail(mEmail);
                    account.setPasswd(mPassword);
                    dao.create(account);
                    System.out.println("register");
                }else{
                    System.out.println("login");
                    if (!account.getPasswd().equals(mPassword)){
                        System.out.println("login error.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        /**
         * 登录成功，跳转
         * @param success
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //隐藏进度
            showProgress(false);

            if (success) {
                System.out.println("login success");

                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra("user",mEmail);
                startActivity(intent);

                finish();
            } else {
                System.out.println("login error");
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        /**
         * 取消登录
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

