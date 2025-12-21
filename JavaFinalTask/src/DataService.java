import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class DataService {

    private final ApiClient apiClient;
    private final long ttlMillis;

    private List<Post> cachedPosts;
    private long cacheTimestamp;

    public DataService(ApiClient apiClient, long ttlMillis) {
        this.apiClient = apiClient;
        this.ttlMillis = ttlMillis;
    }

    public List<Post> getPosts() throws IOException {
        long now = Instant.now().toEpochMilli();

        synchronized (this) {
            if (cachedPosts == null || isCacheExpired(now)) {
                System.out.println(
                        Thread.currentThread().getName() + " -> Загружаю данные из API"
                );

                cachedPosts = apiClient.fetchPosts();
                cacheTimestamp = now;
            } else {
                System.out.println(
                        Thread.currentThread().getName() + " -> Беру данные из кеша"
                );
            }

            return cachedPosts;
        }
    }

    private boolean isCacheExpired(long now) {
        return now - cacheTimestamp > ttlMillis;
    }
}