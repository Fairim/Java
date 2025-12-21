import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ApiClient {

    private static final String POSTS_URL = "https://jsonplaceholder.typicode.com/posts";

    public List<Post> fetchPosts() throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(POSTS_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);

            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error: " + statusCode);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                String response = readFully(inputStream);
                return parsePosts(response);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readFully(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private List<Post> parsePosts(String json) {
        List<Post> posts = new ArrayList<>();

        String content = json.substring(1, json.length() - 1);
        String[] objects = content.split("\\},  \\{");
        int size = objects.length;
        for (int i = 0; i < size; i++) {
            StringBuilder sb = new StringBuilder(objects[i]);
            if (i == 0){
                sb.append("}");
            }
            else if (i != size - 1) {
                sb.append("}");
                sb.insert(0, "{");
            }else{
                sb.insert(0, "{");
            }
            objects[i] =  sb.toString();
        }

        for (String obj : objects) {
            String normalized = obj
                    .replace("{", "");
            int userId = extractInt(normalized, "userId");
            int id = extractInt(normalized, "id");
            String title = extractString(normalized, "title");
            String body = extractString(normalized, "body");

            posts.add(new Post(userId, id, title, body));
        }

        return posts;
    }

    private int extractInt(String source, String key) {
        String value = extractRawValue(source, key);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Не найдено поле: " + key);
        }
        return Integer.parseInt(value);
    }

    private String extractString(String source, String key) {
        String value = extractRawValue(source, key);
        return value.replace("\"", " ");
    }

    private String extractRawValue(String source, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = source.indexOf(searchKey);
        if (keyIndex == -1) {
            return "";
        }

        int colonIndex = source.indexOf(":", keyIndex);
        if (colonIndex == -1) {
            return "";
        }

        int start = colonIndex + 1;

        int endComma = source.indexOf(",", start);
        int endBrace = source.indexOf("}", start);

        int end;
        if (endComma == -1) {
            end = endBrace;
        } else {
            end = endComma;
        }

        return source.substring(start, end).trim();
    }

}
