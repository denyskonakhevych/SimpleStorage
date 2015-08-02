package io.simpstor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.google.common.base.Optional;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by koxa on 02.08.2015.
 */
public class KryoSerializer implements StorageSerializer {

    @Override
    public Optional<Kryo> getSerializer() {
        Kryo kryo = new Kryo();

        kryo.register(ObjectEntity.class);
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        kryo.setReferences(false);

        // Serialize Arrays$ArrayList
        //noinspection ArraysAsListWithZeroOrOneArgument
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        SynchronizedCollectionsSerializer.registerSerializers(kryo);
        // Serialize inner AbstractList$SubAbstractListRandomAccess
        kryo.addDefaultSerializer(new ArrayList<>().subList(0, 0).getClass(),
                new CollectionSerializer() {
                    @Override
                    protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                        return new ArrayList();
                    }
                });
        // Serialize AbstractList$SubAbstractList
        kryo.addDefaultSerializer(new LinkedList<>().subList(0, 0).getClass(),
                new CollectionSerializer() {
                    @Override
                    protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                        return new ArrayList();
                    }
                });
        // To keep backward compatibility don't change the order of serializers above

        return Optional.of(kryo);
    }
}
