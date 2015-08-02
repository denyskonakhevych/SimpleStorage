package io.simpstor;

/**
 * Created by koxa on 02.08.2015.
 */
public class StorageSerializerFactory {
    private static StorageSerializer kryoStorageSerializer = new KryoSerializer();

    public static <T> T getInstance( SerializerType type ) {
        if (SerializerType.KRYO.equals(type) ) {
            return (T) kryoStorageSerializer.getSerializer().get();
        }
        return null;
    }
}
