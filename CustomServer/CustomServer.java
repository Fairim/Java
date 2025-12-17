import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CustomServer {
    static HashSet<String> tokens = new HashSet<>();
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);

        server.createContext("/login", new TokenHandler());
        server.createContext("/image", new ImageHandler());
        server.createContext("/game", new RoshamboHandler());
        server.createContext("/delete", new DeleteHandler());

        server.start();
        System.out.println("Your link to server: http://localhost:8080");
    }

    // Выдача токена
    static private class TokenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(!"GET".equals(exchange.getRequestMethod())) {
                String response = "{\"error\":\"Method not allowed\"}";
                exchange.sendResponseHeaders(405, response.getBytes().length);
                return;
            }
            String newToken = UUID.randomUUID().toString();
            tokens.add(newToken);
            String jsonResponse = String.format(
                    "{\"token\": \"%s\", \"message\": \"Login successful\", \"active_tokens\": %d}",
                    newToken, tokens.size()
            );
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            // Отправляем ответ

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
                os.flush();
            }

            System.out.println("Generated new token: " + newToken);
        }
    }

    // Отображение фото
    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("/Users/ruslanahmetsafin/IdeaProjects/Server/src/image.jpg");
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "image/jpg");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // Игра в камень, ножницы, бумага
    static private class RoshamboHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(!"POST".equals(exchange.getRequestMethod())) {
                String response = "{\"error\":\"Method not allowed\"}";
                sendResponse(exchange,405, response);
                return;
            }
            String requestBody = readRequestBody(exchange);
            Map<String, String> headers = new HashMap<>();
            String[] lines = requestBody.split(",");
            for(String line : lines) {
                String[] fields = line.split(":");
                if (fields.length != 2 || (!fields[0].equals("token") && !fields[0].equals("choice"))) {
                    String response = "{\"error\":\"Incorrect input parameters\"}";
                    sendResponse(exchange,405, response);
                    return;
                }
                headers.put(fields[0], fields[1].toLowerCase());
            }
            if (checkToken(exchange, headers.get("token"))) {
                int choicePlayer = checkGameParameters(headers.get("choice"));
                int gameState = 0;
                if (choicePlayer != -1) {
                    gameState = game(choicePlayer);
                } else {
                    String response = "{\"error\":\"Incorrect input token\"}";
                    sendResponse(exchange, 405, response);
                    return;
                }
                String response = switch (gameState) {
                    case 0 -> "{\"status\":\"Draw\"}";
                    case 1 -> "{\"status\":\"Win\"}";
                    case -1 -> "{\"status\":\"Lose\"}";
                    default -> "";
                };
                sendResponse(exchange, 200, response);
                System.out.println(response);
            }
        }

        private int checkGameParameters(String choice) {
            if (choice.equals("ножницы")) {
                return 0;
            }else if (choice.equals("камень")) {
                return 1;
            }else if (choice.equals("бумага")) {
                return 2;
            }
            return -1;
        }

        private int game(int choice){
            Random random = new Random();
            int randomNum = random.nextInt(3);
            //0 - ножницы, 1 - камень, 2 - бумага
            //2 - 0 = 2 | L
            //2 - 1 = 1 | W
            //1 - 0 = 1 | W
            //1 - 2 = -1 | L
            //0 - 1 = -1 | L
            //0 - 2 = -2 | W

            int diff = randomNum - choice;
            if(diff == 0) {
                return 0;
            }
            if (diff == 1 || diff == -2) {
                return 1;
            }
            return -1;
        }
        //check
        //curl -X POST -d "token:33bbaab2-a9a6-4ed0-88eb-ffa8e5cfafd1,choice:Ножницы" http://localhost:8080/game
    }

    // Удаление файла по обсолютному пути
    static private class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                String response = "{\"error\":\"Method not allowed\"}";
                sendResponse(exchange, 405, response);
                return;
            }
            String requestBody = readRequestBody(exchange);
            Map<String, String> headers = new HashMap<>();
            String[] lines = requestBody.split(",");
            for(String line : lines) {
                String[] fields = line.split(":");
                if (fields.length != 2 || (!fields[0].equals("path") && !fields[0].equals("token"))) {
                    String response = "{\"error\":\"Incorrect input parameters\"}";
                    sendResponse(exchange,405, response);
                    return;
                }
                headers.put(fields[0], fields[1].toLowerCase());
            }
            if(checkToken(exchange, headers.get("token"))) {
                String path = headers.get("path");
                try {
                    Files.deleteIfExists(Paths.get(path));
                    System.out.println("Deleted file: " + path);
                    sendResponse(exchange, 200, "Deleted file");
                }catch (Exception e) {
                    System.out.println("Error deleting file: " + path);
                    sendResponse(exchange, 405, e.getMessage());
                }
            }
        }
        //check
        //curl -X DELETE "http://localhost:8080/delete" -d "path:/Users/ruslanahmetsafin/IdeaProjects/Server/src/file.txt,token:f47366dd-f741-47ed-85d7-6689257f6981"
    }

    static void sendResponse(HttpExchange exchange, int code, String mеssage) throws IOException {
        byte[] responseBytes = mеssage.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }

    static private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toString(StandardCharsets.UTF_8.name());
        }
    }

    static private boolean checkToken(HttpExchange exchange, String token) throws IOException{
        if(!tokens.contains(token)) {
            String response = "{\"error\":\"Incorrect input token\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(405, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
                os.flush();
            }
            System.out.println("Не правильеый токен!");
            return false;
        }
        return true;
    }

}