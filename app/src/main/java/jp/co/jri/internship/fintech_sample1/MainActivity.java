package jp.co.jri.internship.fintech_sample1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.*;

//以降追加
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    List<FintechData> fintech = new ArrayList<>();
    Boolean reverse = false;
    //　プログラムの起動時に実行するメソッド
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウト（activity_main.xml）を表示する
        setContentView(R.layout.activity_main);

        // 「表示」ボタンをタップしたらonClick()を呼び出す
        findViewById(R.id.btSearch).setOnClickListener(this);
        // 「遷移」ボタンをタップしたらonClick()を呼び出す
        findViewById(R.id.btLink).setOnClickListener(this);
    
        //追加(9/24安岡)
        findViewById(R.id.etStart).setOnClickListener(this);
        findViewById(R.id.etEnd).setOnClickListener(this);
        findViewById(R.id.newButton).setOnClickListener(this);
    }

    // ボタンをタップした時に実行するメソッド
    @Override
    public void onClick(View view) {
        // クリックされたボタンを識別
        int getButton = view.getId();
        // 「表示」がクリックされたとき、表示処理用のメソッドを呼び出す
        if (getButton == R.id.btSearch) {
            clickBtSearch(view);
        }
        // 「遷移」がクリックされたとき、画面遷移用のメソッドを呼び出す
        if (getButton == R.id.btLink) {
            clickBtnLink(view);
        }
    
        // 追加(9/24安岡)
        if (getButton == R.id.etStart) {
            clickEtStart(view);
        }
        if (getButton == R.id.etEnd) {
            clickEtEnd(view);
        }if (getButton == R.id.newButton) {
            clickNewButton(view);
        }
    }


    //追加(9/24安岡)
    public void clickEtStart(View view) {
        EditText etStart = findViewById(R.id.etStart);
        showDatePickerDialog(etStart);
    }

    //追加(9/24安岡)
    public void clickEtEnd(View view) {
        EditText etEnd = findViewById(R.id.etEnd);
        showDatePickerDialog(etEnd);
    }

    public void clickNewButton(View view) {
        reverse = !reverse;
        List<FintechData> fintechDataList = fintech;
        if (reverse) {
            Collections.reverse(fintechDataList);
        }
        List<Map<String, ?>> listData = fintechDataToMapList(fintechDataList);  // Adapterに渡す形式のlist型変数の宣言と初期化
        SimpleAdapter adapter = new SimpleAdapter(                  // ()内で指定した内容のAdapterを生成する
                MainActivity.this,
                listData,                                           // ListView用に自作したレイアウトにFintechDataのどの項目を表示するかを指定する
                R.layout.custom_list_layout,                        // 自作したレイアウト名
                new String[]{"transDate", "content", "amount"},     // 表示するFintechDataの項目を指定
                new int[]{R.id.tvList1, R.id.tvList2, R.id.tvList3} // 自作したレイアウトのViewのidを指定
        );

        // Adapterの内容をlistViewに表示する
        ListView lvHistoricalData = (ListView) findViewById(R.id.lvHistoricalData); //Viewにプログラムでの変数名を割り当てる
        lvHistoricalData.setAdapter(adapter);   //Adapterの内容をlvHistoricalDataに表示する
    }

    //「表示」がクリックされたとき
    @SuppressLint("DefaultLocale")
    public void clickBtSearch(View view) {

        // 収支の集計領域を準備する
        int sumIncome = 0;  // 収入の集計領域
        int sumExpense = 0; // 支出の集計領域

        int minusExpCredit = 0;
        int minusCredit = 0;

        // 対象期間の始まりと終わりを入力する
        EditText etStart = findViewById(R.id.etStart);     // Viewにプログラムでの変数名を割り当てる
        EditText etEnd = findViewById(R.id.etEnd);       // Viewにプログラムでの変数名を割り当てる
        String startDate = etStart.getText().toString();   // EditTextに入力した内容を文字列にして変数に渡す
        String endDate = etEnd.getText().toString();     // EditTextに入力した内容を文字列にして変数に渡す

        //キーボードの制御
        etStart.setOnFocusChangeListener((v, hasFocus) -> {
             //EditTextのフォーカスが外れた場合
            if (!hasFocus) {
                //ソフトキーボードを非表示にする
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        etStart.clearFocus();
        etEnd.setOnFocusChangeListener((v, hasFocus) -> {
            //EditTextのフォーカスが外れた場合
            if (!hasFocus) {
                //ソフトキーボードを非表示にする
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        etEnd.clearFocus();

        // 表示用のList(fintechDataList)を用意する
        List<FintechData> fintechDataList = new ArrayList<>();

        // LocalFintechDateBase.txt(Androidローカルファイル)が存在しない場合、FintechDataBase.csvからデータを読み込み、
        // LocalFintechDateBase.txtに書き出したのち、List(fintechDataList)を作成する
        // LocalFintechDateBase.txtが存在する場合、ローカルファイルからList(fintechDataList)を作成する
        CsvReader parser = new CsvReader();
        String filename = "LocalFintechDateBase.txt";
        File file = this.getFileStreamPath(filename);
        parser.readerFintechDataBase(getApplicationContext(), file.exists());

        // text.txt(Androidローカルファイル)から1行取り出し、表示用のList(fintechDataList)に入れる
        // 上記をtext.txtが終わるまで繰り返す
        String prevMonth = "00";
        for (FintechData fdata : parser.fintechObjects) {
            // transDateがstartDateから、endDateまでの日付のデータのみListに入れる
            if (fdata.getTransDate().compareTo(startDate) >= 0) {
                if (fdata.getTransDate().compareTo(endDate) <= 0) {
                    fintechDataList.add(fdata);
                    String dateStr = fdata.getTransDate();
                    String monthStr = dateStr.substring(5, 7);

                    if (!(prevMonth.equals(monthStr))){
                        minusExpCredit = minusExpCredit + minusCredit;
                    }
                    prevMonth = monthStr;
                    // 収支の合計を集計する
                    if (fdata.getAmount() >= 0) {
                        sumIncome = sumIncome + fdata.getAmount();   // 収入の合計を求める
                    } else {
                        sumExpense = sumExpense + fdata.getAmount(); // 支出の合計を求める
                        if (!(fdata.getCategory().equals("クレジットカード"))){
                            minusExpCredit = minusExpCredit + fdata.getAmount();
                        } else {
                            minusCredit = minusCredit + fdata.getAmount();
                        }
                    }
                    
                }
            }
        }

        int sumAccountBalance = sumIncome + minusExpCredit;
        int sumProjectedBalance = sumIncome + sumExpense;

        // Adapterに表示用のList(fintechDataList)を受け渡す
        fintech = fintechDataList;
        if (reverse) {
            Collections.reverse(fintechDataList);
        }
        List<Map<String, ?>> listData = fintechDataToMapList(fintechDataList);  // Adapterに渡す形式のlist型変数の宣言と初期化
        SimpleAdapter adapter = new SimpleAdapter(                  // ()内で指定した内容のAdapterを生成する
                MainActivity.this,
                listData,                                           // ListView用に自作したレイアウトにFintechDataのどの項目を表示するかを指定する
                R.layout.custom_list_layout,                        // 自作したレイアウト名
                new String[]{"transDate", "content", "amount"},     // 表示するFintechDataの項目を指定
                new int[]{R.id.tvList1, R.id.tvList2, R.id.tvList3} // 自作したレイアウトのViewのidを指定
        );

        // Adapterの内容をlistViewに表示する
        ListView lvHistoricalData = (ListView) findViewById(R.id.lvHistoricalData); //Viewにプログラムでの変数名を割り当てる
        lvHistoricalData.setAdapter(adapter);   //Adapterの内容をlvHistoricalDataに表示する

        // 収入の合計を出力する
        TextView tvIncome = (TextView) findViewById(R.id.tvIncome);
        tvIncome.setText(String.format("%,d", sumIncome));

        // 支出の合計を出力する
        TextView tvExpense = (TextView) findViewById(R.id.tvExpense);
        tvExpense.setText(String.format("%,d", sumExpense));

        // 残高を出力する
        // 口座残高
        TextView tvAccountBalance = (TextView) findViewById(R.id.tvAccountBalance);
        tvAccountBalance.setText(String.format("%,d",sumAccountBalance));
        // 見込み残高
        TextView tvProjectedBalance = (TextView) findViewById(R.id.tvProjectedBalance);
        tvProjectedBalance.setText(String.format("%,d",sumProjectedBalance));
    }

    //「遷移」がクリックされたとき
    public void clickBtnLink(View view) {
        Intent intent = new Intent(this, Main2Activity.class);  //インテントの作成

        // 遷移先で表示するため、遷移ボタンのボタン名を取得しintentに格納する
        TextView tv = (TextView) view;

        // EditTextから日付を取得
        EditText etStart = findViewById(R.id.etStart);
        EditText etEnd = findViewById(R.id.etEnd);
        String startDate = etStart.getText().toString();
        String endDate = etEnd.getText().toString();

        // 日付をIntentに追加
        intent.putExtra("startDate", startDate);
        intent.putExtra("endDate", endDate);

        startActivity(intent); //画面遷移
    }

    // Adapterに渡す形式のlist型変数の宣言と初期化
    private List<Map<String, ?>> fintechDataToMapList(List<FintechData> fintechDataList) {
        List<Map<String, ?>> data = new ArrayList<>();
        for (FintechData fintechData : fintechDataList) {
            data.add(fintechDataToMap(fintechData));
        }
        return data;
    }

    // Adapterに渡す形式のlist型変数の宣言と初期化（詳細）
    @SuppressLint("DefaultLocale")
    private Map<String, ?> fintechDataToMap(FintechData fintechData) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", fintechData.getId());
        map.put("transDate", fintechData.getTransDate());
        map.put("transTime", fintechData.getTransTime());
        map.put("service", fintechData.getService());
        map.put("category", fintechData.getCategory());
        map.put("supplier", fintechData.getSupplier());
        map.put("content", fintechData.getContent());
        map.put("use", fintechData.getUse());
        map.put("amount", String.format("%,d", fintechData.getAmount()));
        map.put("balance", String.format("%,d", fintechData.getBalance()));
        return map;
    }
    
    //追加(9/24安岡)
    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editText.setText(String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth));
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}