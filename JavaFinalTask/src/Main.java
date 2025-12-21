import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Задача 1
            ApiClient apiClient = new ApiClient();

            // Задача 2
            DataService dataService = new DataService(apiClient, 60_000); // TTL = 60 секунд

            // Задача 3
            HttpServer server = new HttpServer(8080, dataService, 10); // 10 потоков
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
