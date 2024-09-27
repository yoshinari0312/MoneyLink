package jp.co.jri.internship.fintech_sample1;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TapPagerAdapter extends FragmentStateAdapter {

    String startDate;
    String endDate;

    public TapPagerAdapter(Main2Activity fragment, String startDate, String endDate){
        super(fragment);
        this.startDate = startDate;       // startDateを受け取る
        this.endDate = endDate;           // endDateを受け取る      // 遷移元のボタン名を受け取る
    }

    // 指定されたタブの位置（position）に対応するタブページ（Fragment）を作成する
    @NonNull
    @Override
    public Fragment createFragment(int position){
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        // tab1選択時
        if (position == 0) {
            fragment = new Tab1Fragment();
            bundle.putString("startDate", startDate);    // startDateをBundleに格納
            bundle.putString("endDate", endDate);        // endDateをBundleに格納
        }
        // tab2選択時
        else if (position == 1){
            fragment = new Tab2Fragment();
            bundle.putString("startDate", startDate);    // startDateをBundleに格納
            bundle.putString("endDate", endDate);        // endDateをBundleに格納
        }
        // tab3選択時
        else if (position == 2){
            fragment = new Tab3Fragment();
            bundle.putString("startDate", startDate);    // startDateをBundleに格納
            bundle.putString("endDate", endDate);        // endDateをBundleに格納
        }
        assert fragment != null;
        fragment.setArguments(bundle);
        return fragment;
    }

    //タブの数を返す
    @Override
    public int getItemCount(){
      return 3;
    }
}