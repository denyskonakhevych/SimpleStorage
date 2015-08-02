import com.google.gson.Gson;

/**
 * Created by koxa on 25.07.2015.
 */
public class JsonStorage implements Storage {

    @Override
    public void destroy() {

    }

    @Override
    public <E> void insert(String key, E value) {
        Gson gson = new Gson();
        //gson.toJson();
    }

    @Override
    public <E> E select(String key) {
        return null;
    }

    @Override
    public boolean exist(String key) {
        return false;
    }

    @Override
    public void deleteIfExists(String key) {

    }
}
