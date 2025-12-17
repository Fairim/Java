import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestHttpConnection {
    public static void main(String[] args) throws IOException {
        String token = "c4b6a13f-3588-4534-ba5d-529829a10090";
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=55.78874&lon=49.12214";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("X-Yandex-Weather-Key", token);
        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resp = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            resp.append(inputLine);
        }

        in.close();
        System.out.println(resp.toString());
    }

}
