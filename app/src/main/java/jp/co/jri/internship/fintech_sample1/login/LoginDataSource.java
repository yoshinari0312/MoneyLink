package jp.co.jri.internship.fintech_sample1.login;
import android.content.Context;
import jp.co.jri.internship.fintech_sample1.CsvReader;
import jp.co.jri.internship.fintech_sample1.UserData;
import java.io.IOException;
import java.util.List;

public class LoginDataSource {
    public Result<LoggedInUser> login(String userId, String password, Context context) {
        try {
            // CsvReaderインスタンスを作成し、ユーザーデータを読み込む
            CsvReader csvReader = new CsvReader();
            csvReader.readerUserDataBase(context); // CSVからユーザーデータを読み込む
            List<UserData> userObjects = csvReader.userObjects;
            // ユーザーデータの中から一致するIDとパスワードを検索する
            for (UserData userData : userObjects) {
                if (userData.getUserId().equals(userId) && userData.getPassword().equals(password)) {
                    // ユーザーが見つかり、パスワードが一致した場合
                    LoggedInUser loggedInUser = new
                            LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            userData.getDisplayName() // 実際のユーザー名を返す
                    );
                    return new Result.Success<>(loggedInUser); // 認証成功
                }
            }
            // 一致するユーザーが見つからない場合はエラーを返す
            return new Result.Error(new IOException("Invalid userId or password"));
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e)); // エラー処理
        }
    }
}