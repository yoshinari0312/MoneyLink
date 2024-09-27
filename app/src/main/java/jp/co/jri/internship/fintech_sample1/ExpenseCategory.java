package jp.co.jri.internship.fintech_sample1;

import java.util.HashMap;

// 階層的なデータ構造を持つクラスを定義
public class ExpenseCategory {
    public String use;
    public HashMap<String, Integer> contentMap = new HashMap<>();
    public int totalAmount = 0;

    public ExpenseCategory(String use) {
        this.use = use;
    }
}
