package jp.co.jri.internship.fintech_sample1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウト（activity_main2.xml）を表示する
        setContentView(R.layout.activity_main2);

        // intentに格納されている遷移元の日付を受け取る
        Intent intent = getIntent();
        String startDate = intent.getStringExtra("startDate");
        String endDate = intent.getStringExtra("endDate");

        // アダプタを設定
        ViewPager2 pager = (ViewPager2)findViewById(R.id.pager);
        TapPagerAdapter adapter = new TapPagerAdapter(this, startDate, endDate);
        pager.setAdapter(adapter);

        // TabLayoutとViewPager2を関連付ける（押下されたタブと内容表示を関連付ける）
        TabLayout tabs = (TabLayout)findViewById(R.id.tab_layout);
        new TabLayoutMediator(
                tabs,
                pager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("支出");
                            break;
                        case 1:
                            tab.setText("収入");
                            break;
                        case 2:
                            tab.setText("収支");
                            break;
                    }
                }
        ).attach();
    }
}

