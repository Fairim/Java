import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;
    private final DataService dataService;
    private final ExecutorService threadPool;

    public HttpServer(int port, DataService dataService, int maxThreads) {
        this.port = port;
        this.dataService = dataService;
        this.threadPool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String line = reader.readLine();
            if (line == null || !line.startsWith("GET")) {
                clientSocket.close();
                return;
            }

            String path = line.split(" ")[1];
            if ("/posts".equals(path)) {
                List<Post> posts = dataService.getPosts(); // из задачи 2
                String response = buildJsonResponse(posts);

                writer.write("HTTP/1.1 200 OK\r\n");
                writer.write("Content-Type: application/json\r\n");
                writer.write("Content-Length: " + response.getBytes().length + "\r\n");
                writer.write("\r\n");
                writer.write(response);
                writer.flush();

                System.out.println("Served /posts to " + clientSocket.getInetAddress());

            } else {
                writer.write("HTTP/1.1 404 Not Found\r\n");
                writer.write("\r\n");
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private String buildJsonResponse(List<Post> posts) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            sb.append("{")
                    .append("\"userId\":").append(post.getUserId()).append(",")
                    .append("\"id\":").append(post.getId()).append(",")
                    .append("\"title\":\"").append(post.getTitle().replace("\"", "\\\"")).append("\",")
                    .append("\"body\":\"").append(post.getBody().replace("\"", "\\\"")).append("\"")
                    .append("}");
            if (i < posts.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
