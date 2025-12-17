import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class GenerateToken {
    private String token;
    private String url;
    private String startToken;
    private static final Object fileLock = new Object();
    private Random random;
    private static final char[] all_chars_in_token = "0123456789abcdef".toCharArray();

    public GenerateToken() {
        url = "https://api.weather.yandex.ru/v2/forecast?lat=55.78874&lon=49.12214";
        startToken = "c4b6a13f-3588-4";
        random = new Random();
    }

    public void StartGenerateToken() {
        int trueToken = 0;
        while(trueToken == 0) {
            token = generateTokenContinuation();
            try {
                if (checkResponseCode(token) == 200) {
                    System.out.println("Работающий токен: " + token);
                    trueToken += 1;

                    synchronized(fileLock) {
                        try (FileWriter writer = new FileWriter("valid_tokens.txt", true)) {
                            writer.write(token + System.lineSeparator());
                        }
                    }
                }
                else if (checkResponseCode(token) == 403) {
                    System.out.println("Не верно! Ошибка 403, token: " + token);
                }else{
                    System.out.println("Не верно! Надеюсь не сломал, token: " + token);
                }
            } catch (IOException e) {
                System.out.println("Не верно!");
            }
        }
    }

    private String generateTokenContinuation() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 8; i++) {
            sb.append(all_chars_in_token[random.nextInt(16)]);
        }
        sb.append('-');
        for (int i = 0; i < 4; i++) {
            sb.append(all_chars_in_token[random.nextInt(16)]);
        }
        sb.append('-');
        sb.append('4');
        for (int i = 0; i < 3; i++) {
            sb.append(all_chars_in_token[random.nextInt(16)]);
        }
        sb.append('-');
        for (int i = 0; i < 4; i++) {
            sb.append(all_chars_in_token[random.nextInt(16)]);
        }
        sb.append('-');
        for (int i = 0; i < 12; i++) {
            sb.append(all_chars_in_token[random.nextInt(16)]);
        }
        return sb.toString();
    }

    private int checkResponseCode(String token) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Yandex-Weather-Key", token);
        return con.getResponseCode();
    }

    public static class MyThreadGenerator extends Thread {
        @Override
        public void run() {
            GenerateToken token = new GenerateToken();
            token.StartGenerateToken();
        }
    }
}
