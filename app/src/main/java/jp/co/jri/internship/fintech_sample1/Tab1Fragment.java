package jp.co.jri.internship.fintech_sample1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends Fragment {

    private PieChart outerPieChart; // 外側の円グラフ
    private PieChart innerPieChart; // 内側の円グラフ
    private TextView totalExpenseTextView;
    private ListView listView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // レイアウト（tab1_fragment.xml）を作成する
        View v = inflater.inflate(R.layout.tab1_fragment, container, false);
        
        // 「戻る」ボタンをタップしたらonClick()を呼び出す
        Button btBack = v.findViewById(R.id.btBack);
        btBack.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              clickBtBack();
          }
        });

        // TapPagerAdapterからデータを受け取り、レイアウトに表示する
        String startDate = getArguments().getString("startDate");
        String endDate = getArguments().getString("endDate");

        TextView textView = v.findViewById(R.id.tapButton);
        textView.setText("期間: " + startDate + " ～ " + endDate);

        // 各ビューを取得
        outerPieChart = v.findViewById(R.id.outerPieChart);
        innerPieChart = v.findViewById(R.id.innerPieChart);
        totalExpenseTextView = v.findViewById(R.id.totalExpense);
        listView = v.findViewById(R.id.listView);

        // CsvReaderを使用してデータを読み込む
        CsvReader csvReader = new CsvReader();
        // ローカルファイルを使用するかどうかを指定するフラグを設定
        // ここではローカルファイルが存在しない前提でfalseに設定します
        csvReader.readerFintechDataBase(getContext(), false);

        // 読み込んだデータを取得
        List<FintechData> dataList = csvReader.fintechObjects;

        // データを処理して円グラフを表示
        processAndDisplayData(dataList, startDate, endDate);

        return v;
    }

    //「戻る」ボタンがクリックされた時の処理
    public void clickBtBack(){
        Intent intent = new Intent(getActivity(),MainActivity.class);
        startActivity(intent);//画面遷移
    }

    private void processAndDisplayData(List<FintechData> dataList, String startDate, String endDate) {
        // 日付のフォーマットを定義
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        // 収入を除外し、支出のみを用途分類（use）と内容（content）ごとに階層的に集計
        HashMap<String, ExpenseCategory> expenseCategories = new HashMap<>();
        int totalExpense = 0;

        for (FintechData data : dataList) {
            try {
                Date transDate = sdf.parse(data.getTransDate());
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                if (!transDate.before(start) && !transDate.after(end)) {
                    int amount = data.getAmount();
                    if (amount < 0) {
                        // 支出のみ処理
                        String use = data.getUse();
                        String content = data.getContent();
                        int expense = Math.abs(amount); // 金額を正の値に変換
                        totalExpense += expense;

                        // 用途分類ごとに集計
                        ExpenseCategory category;
                        if (expenseCategories.containsKey(use)) {
                            category = expenseCategories.get(use);
                        } else {
                            category = new ExpenseCategory(use);
                            expenseCategories.put(use, category);
                        }
                        category.totalAmount += expense;

                        // 内容ごとに集計
                        if (category.contentMap.containsKey(content)) {
                            category.contentMap.put(content, category.contentMap.get(content) + expense);
                        } else {
                            category.contentMap.put(content, expense);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // カスタムカラーリストを定義
        ArrayList<Integer> innerColors = new ArrayList<>();
        ArrayList<Integer> outerColors = new ArrayList<>();

        // 内側の円グラフのエントリとカラーリストを作成
        ArrayList<PieEntry> innerEntries = new ArrayList<>();
        int[] innerColorTemplate = {
                Color.parseColor("#0000FF"),
                Color.parseColor("#FF0000"),
                Color.parseColor("#2CA02C"),
                Color.parseColor("#FFA601"),
        };

        // 内側のエントリを金額の降順にソート
        List<ExpenseCategory> sortedCategories = new ArrayList<>(expenseCategories.values());
        sortedCategories.sort((c1, c2) -> {
            return Integer.compare(c2.totalAmount, c1.totalAmount); // 降順
        });

        int colorIndex = 0;
        for (ExpenseCategory category : sortedCategories) {
            innerEntries.add(new PieEntry(category.totalAmount, category.use));
            // 内側のカラーリストに色を追加
            innerColors.add(innerColorTemplate[colorIndex % innerColorTemplate.length]);
            colorIndex++;
        }

        // 外側の円グラフのエントリとカラーリストを作成
        ArrayList<PieEntry> outerEntries = new ArrayList<>();
        int[] outerColorTemplate = {
                Color.parseColor("#0047F9"),
                Color.parseColor("#0088F5"),
                Color.parseColor("#00B7FE"),
                Color.parseColor("#00DBFE"),
                Color.parseColor("#FF7F7A"),
                Color.parseColor("#BCBD22"),
                Color.parseColor("#FFD001"),
        };

        colorIndex = 0;
        for (ExpenseCategory category : sortedCategories) {
            List<Map.Entry<String, Integer>> sortedContentEntries = new ArrayList<>(category.contentMap.entrySet());
            sortedContentEntries.sort((e1, e2) -> {
                return Integer.compare(e2.getValue(), e1.getValue()); // 降順
            });

            for (Map.Entry<String, Integer> entry : sortedContentEntries) {
                outerEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                // 外側のカラーリストに色を追加
                outerColors.add(outerColorTemplate[colorIndex % outerColorTemplate.length]);
                colorIndex++;
            }
        }

        // リストデータを作成
        List<Map<String, String>> listData = new ArrayList<>();
        for (ExpenseCategory category : expenseCategories.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("category", category.use);
            map.put("amount", String.format("%,d円", category.totalAmount));
            listData.add(map);
        }

        // リストデータを金額の降順でソート
        listData.sort((o1, o2) -> {
            // 金額を取り出して数値に変換
            int amount1 = Integer.parseInt(o1.get("amount").replace(",", "").replace("円", ""));
            int amount2 = Integer.parseInt(o2.get("amount").replace(",", "").replace("円", ""));
            // 降順でソート
            return Integer.compare(amount2, amount1);
        });

        // 内側の円グラフのデータセットを作成
        PieDataSet innerDataSet = new PieDataSet(innerEntries, "");
        innerDataSet.setColors(innerColors);
        innerDataSet.setValueTextSize(10f);
        innerDataSet.setValueTextColor(Color.WHITE);

        // カスタムの ValueFormatter を設定
        int finalTotalExpense = totalExpense;
        innerDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                double percentage = (pieEntry.getValue() / finalTotalExpense) * 100;
                String percentageText = String.format("%.0f%%", percentage);
                return percentageText;
            }
        });

        // 内側の円グラフを設定
        innerPieChart.setData(new PieData(innerDataSet));
        innerPieChart.getDescription().setEnabled(false);
        innerPieChart.setDrawHoleEnabled(false);
        innerPieChart.getLegend().setEnabled(false); // 凡例を非表示にする
        innerPieChart.setRotationEnabled(false); // 回転を無効化
        innerPieChart.setTouchEnabled(false);
        innerPieChart.setEntryLabelTextSize(10f);
        innerPieChart.setUsePercentValues(true);

        // 外側の円グラフのデータセットを作成
        PieDataSet outerDataSet = new PieDataSet(outerEntries, "");
        outerDataSet.setColors(outerColors);
        outerDataSet.setValueTextSize(10f);
        outerDataSet.setDrawValues(true); // 値の表示を無効化
        outerDataSet.setDrawIcons(false);
        outerDataSet.setValueTextColor(Color.WHITE);

        // カスタムの ValueFormatter を設定
        int finalTotalExpense1 = totalExpense;
        outerDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                double percentage = (pieEntry.getValue() / finalTotalExpense1) * 100;
                String percentageText = String.format("%.0f%%", percentage);
                return percentageText;
            }
        });

        // 外側の円グラフを設定
        outerPieChart.setData(new PieData(outerDataSet));
        outerPieChart.getDescription().setEnabled(false);
        outerPieChart.setDrawHoleEnabled(true);
        outerPieChart.setHoleRadius(64f); // 内側の円グラフが見えるように穴を開ける
        outerPieChart.setTransparentCircleRadius(0f);
        outerPieChart.getLegend().setEnabled(false); // 凡例を非表示にする
        outerPieChart.setRotationEnabled(false); // 回転を無効化
        outerPieChart.setTouchEnabled(false);
        outerPieChart.setEntryLabelTextSize(10f);
        outerPieChart.setUsePercentValues(true);


        // 円グラフを更新
        innerPieChart.invalidate();
        outerPieChart.invalidate();

        // 支出の合計金額を表示
        totalExpenseTextView.setText("支出の合計金額: " + totalExpense + "円");

        // リストビューにデータを設定
        SimpleAdapter adapter = new SimpleAdapter(
                getContext(),
                listData,
                android.R.layout.simple_list_item_2,
                new String[]{"category", "amount"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        listView.setAdapter(adapter);
    }
}
