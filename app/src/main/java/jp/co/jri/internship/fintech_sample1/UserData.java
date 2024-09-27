package jp.co.jri.internship.fintech_sample1;

/*　フィンテックアプリで扱うデータの形式　*/
public class UserData {

    //  データの名称と型
    private final String userId;
    private final String password;
    private final String displayName;

    // クラスにデータをセットする際の形式
    public UserData(String userId, String password, String displayName){
        this.userId = userId;
        this.password = password;
        this.displayName = displayName;
    }

    //　それぞれのデータを参照する際に利用するメソッド
    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }
}


