package jp.co.jri.internship.fintech_sample1.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import jp.co.jri.internship.fintech_sample1.MainActivity;
import jp.co.jri.internship.fintech_sample1.R;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウト（activity_login.xml）を表示する
        setContentView(R.layout.activity_login);


        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        mContext = this;
        final EditText userIdEditText = findViewById(R.id.userId);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        // Email 補完機能 ここから
        // SharedPreferencesのインスタンスを一度だけ取得
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);

        // SharedPreferencesからメールアドレスを取得して自動補完
        String savedEmail = sharedPreferences.getString("savedEmail", "");
        if (savedEmail != null && !savedEmail.isEmpty()) {
            userIdEditText.setText(savedEmail);
        }
        // Email 補完機能 ここまで

        // 入力条件チェック結果に応じて表示を変更
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid()); //ログインボタンをグレーアウト
            if (loginFormState.getUserIdError() != null) {
                userIdEditText.setError(getString(loginFormState.getUserIdError())); //テキストボックスのエラー処理
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError())); //テキストボックスのエラー処理
            }
        });

        // ログイン結果に応じて処理を振り分け
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
                resetPassword();
                // 認証失敗のメッセージを表示
                TextView loginError = findViewById(R.id.loginError);
                loginError.setText("メールアドレスかパスワードが間違っています");
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            // アプリを強制終了しないようにする
            // finish();
        });

        // テキストボックスの内容が変更されたとき、入力条件を満たしている状態かチェック
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(userIdEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        userIdEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // Enterが入力されたとき
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(userIdEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        mContext);
            }
            return false;
        });

        //ログインボタンがクリックされたとき
        //Email 保存機能追記
        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);

            // メールアドレスをSharedPreferencesに保存
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("savedEmail", userIdEditText.getText().toString());
            editor.apply();

            loginViewModel.login(userIdEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    mContext);
        });
    }

    // ログイン認証が成功したとき
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Intent intent = new Intent(this, MainActivity.class);  //インテントの作成
        startActivity(intent);                                 //画面遷移

        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show(); //トーストを画面表示してユーザへ通知
    }

    // ログイン認証が失敗したとき
    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show(); //トーストを画面表示してユーザへ通知
    }

    // パスワードリセットメソッド
    private void resetPassword() {
        // Clear the password EditText after login
        final EditText passwordEditText = findViewById(R.id.password);
        passwordEditText.setText(""); // Clear the password field
    }
}