package jp.co.jri.internship.fintech_sample1;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class Tab2Fragment extends Fragment {

    private PieChart pieChart;
    private TextView totalIncomeTextView;
    private ListView listView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // レイアウト（tab2_fragment.xml）を作成する
        View v = inflater.inflate(R.layout.tab2_fragment, container, false);

        // TapPagerAdapterからデータを受け取り、レイアウトに表示する
        String startDate = getArguments().getString("startDate");
        String endDate = getArguments().getString("endDate");

        TextView textView = v.findViewById(R.id.tapButton);
        textView.setText("期間: " + startDate + " ～ " + endDate);

        // PieChartと合計金額のTextView、ListViewを取得
        pieChart = v.findViewById(R.id.pieChart);
        totalIncomeTextView = v.findViewById(R.id.totalIncome);
        listView = v.findViewById(R.id.listView);

        // CsvReaderを使用してデータを読み込む
        CsvReader csvReader = new CsvReader();
        csvReader.readerFintechDataBase(getContext(), false);

        // 読み込んだデータを取得
        List<FintechData> dataList = csvReader.fintechObjects;

        // データを処理して円グラフとリストを表示
        processAndDisplayData(dataList, startDate, endDate);

        return v;
    }

    private void processAndDisplayData(List<FintechData> dataList, String startDate, String endDate) {
        // 日付のフォーマットを定義
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        // 収入のみを用途分類ごとに集計
        HashMap<String, Integer> incomeByUse = new HashMap<>();
        double totalIncome = 0;

        for (FintechData data : dataList) {
            try {
                Date transDate = sdf.parse(data.getTransDate());
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                if (!transDate.before(start) && !transDate.after(end)) {
                    int amount = data.getAmount();
                    if (amount > 0) {
                        // 収入のみ処理
                        String use = data.getContent();
                        int income = amount;
                        totalIncome += income;
                        if (incomeByUse.containsKey(use)) {
                            incomeByUse.put(use, incomeByUse.get(use) + income);
                        } else {
                            incomeByUse.put(use, income);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 収入データを金額の降順でソート
        List<Map.Entry<String, Integer>> sortedIncomeEntries = new ArrayList<>(incomeByUse.entrySet());
        sortedIncomeEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // 降順

        // 円グラフのエントリを作成
        ArrayList<PieEntry> entries = new ArrayList<>();
        List<Map<String, String>> listData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedIncomeEntries) {
            String use = entry.getKey();
            int amount = entry.getValue();
            entries.add(new PieEntry(amount, use));

            // リスト用のデータを作成
            Map<String, String> map = new HashMap<>();
            map.put("category", use);
            map.put("amount", String.format("%,d円", amount));
            listData.add(map);
        }

        // カラーリストを青と赤に設定
        ArrayList<Integer> colors = new ArrayList<>();
        int[] colorArray = {
                Color.parseColor("#3498DB"),
                Color.parseColor("#FF4500"),
        };
        for (int i = 0; i < entries.size(); i++) {
            colors.add(colorArray[i % colorArray.length]);
        }

        // 円グラフのデータセットを作成
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);

        // カスタムの ValueFormatter を設定して、パーセンテージを表示
        double finalTotalIncome = totalIncome;
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                double percentage = (pieEntry.getValue() / finalTotalIncome) * 100;
                String percentageText = String.format("%.0f%%", percentage);
                return percentageText;
            }
        });

        // 円グラフのデータを設定
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // 円グラフの設定
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setEntryLabelTextSize(10f); // エントリラベルのサイズ
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setRotationEnabled(false); // 回転を無効化
        pieChart.getLegend().setEnabled(false); // 凡例を非表示にする

        // 円グラフを更新
        pieChart.invalidate();

        // 収入の合計金額を表示
        totalIncomeTextView.setText("収入の合計金額: " + String.format("%,.0f円", totalIncome));

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
