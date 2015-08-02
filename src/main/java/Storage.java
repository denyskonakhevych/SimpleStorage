/**
 * Created by koxa on 25.07.2015.
 */
interface Storage {

    void destroy();

    <E> void insert(String key, E value);

    <E> E select(String key);

    boolean exist(String key);

    void deleteIfExists(String key);
}
