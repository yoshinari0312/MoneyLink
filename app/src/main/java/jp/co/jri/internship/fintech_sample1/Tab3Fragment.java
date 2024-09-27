package jp.co.jri.internship.fintech_sample1;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tab3Fragment extends Fragment {

    private BarChart barChart;
    private TextView periodTextView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // レイアウト（tab3_fragment.xml）を作成する
        View v = inflater.inflate(R.layout.tab3_fragment, container, false);

        // TapPagerAdapterからデータを受け取り、レイアウトに表示する
        String startDate = getArguments().getString("startDate");
        String endDate = getArguments().getString("endDate");

        periodTextView = v.findViewById(R.id.periodTextView);
        periodTextView.setText("期間: " + startDate + " ～ " + endDate);

        barChart = v.findViewById(R.id.barChart);

        // CsvReaderを使用してデータを読み込む
        CsvReader csvReader = new CsvReader();
        csvReader.readerFintechDataBase(getContext(), false);

        // 読み込んだデータを取得
        List<FintechData> dataList = csvReader.fintechObjects;

        // 期間内の収入と支出を計算し、棒グラフに表示
        processAndDisplayData(dataList, startDate, endDate);

        return v;
    }

    private void processAndDisplayData(List<FintechData> dataList, String startDate, String endDate) {
        // 日付のフォーマットを定義
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        // 収入と支出の合計を計算
        double totalIncome = 0;
        double totalExpense = 0;

        for (FintechData data : dataList) {
            try {
                Date transDate = sdf.parse(data.getTransDate());
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                if (!transDate.before(start) && !transDate.after(end)) {
                    int amount = data.getAmount();
                    if (amount > 0) {
                        totalIncome += amount; // 収入
                    } else {
                        totalExpense += Math.abs(amount); // 支出
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // データを棒グラフに設定
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, (float) totalIncome)); // 収入の棒
        barEntries.add(new BarEntry(1, (float) totalExpense)); // 支出の棒

        // データセットを作成
        BarDataSet barDataSet = new BarDataSet(barEntries, "収入と支出");
        barDataSet.setColors(new int[]{Color.parseColor("#005B47"), Color.parseColor("#B3D056")});
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        // データを棒グラフに設定
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // X軸ラベルを設定
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"収入", "支出"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(14f);  // X軸の文字サイズを14に設定

        // 棒グラフの設定
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getAxisLeft().setTextColor(Color.BLACK); // Y軸左のラベルの色
        barChart.getAxisLeft().setAxisMinimum(0f); // 左Y軸の最小値を0に設定
        barChart.getAxisLeft().setTextSize(14f);  // 左Y軸の文字サイズを14に設定
        barChart.getAxisRight().setEnabled(false); // Y軸右は非表示
        barChart.getLegend().setEnabled(false); // 凡例を非表示
        barChart.animateY(1000);
        barChart.setExtraOffsets(0f, 10f, 0f, 20f); // グラフの余白を調整

        // グラフを更新
        barChart.invalidate();
    }
}
