import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import io.simpstor.ObjectEntity;
import io.simpstor.SerializerType;
import io.simpstor.StorageSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by koxa on 25.07.2015.
 */
public class BlobStorage implements Storage {

    private final String mDbName;
    private String mFilesDir;
    private boolean mPaperDirIsCreated;
    private static final Logger logger = LoggerFactory.getLogger(BlobStorage.class);
    private Kryo serializer = StorageSerializerFactory.getInstance(SerializerType.KRYO);

    private Kryo getKryo() {
        return serializer;
    }

//    private final ThreadLocal<Kryo> mKryo = new ThreadLocal<Kryo>() {
//        @Override
//        protected Kryo initialValue() {
//            return createKryoInstance();
//        }
//    };

//    private Kryo createKryoInstance() {
//        Kryo kryo = new Kryo();
//
//        kryo.register(ObjectEntity.class);
//        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
//        kryo.setReferences(false);
//
//        // Serialize Arrays$ArrayList
//        //noinspection ArraysAsListWithZeroOrOneArgument
//        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
//        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
//        SynchronizedCollectionsSerializer.registerSerializers(kryo);
//        // Serialize inner AbstractList$SubAbstractListRandomAccess
//        kryo.addDefaultSerializer(new ArrayList<>().subList(0, 0).getClass(),
//                new CollectionSerializer() {
//                    @Override
//                    protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
//                        return new ArrayList();
//                    }
//                });
//        // Serialize AbstractList$SubAbstractList
//        kryo.addDefaultSerializer(new LinkedList<>().subList(0, 0).getClass(),
//                new CollectionSerializer() {
//                    @Override
//                    protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
//                        return new ArrayList();
//                    }
//                });
//        // To keep backward compatibility don't change the order of serializers above
//
//        return kryo;
//    }

    public BlobStorage(File filesDir, String dbName) {
        mFilesDir = getDbPath(filesDir, dbName);
        mDbName = dbName;
    }

    @Override
    public synchronized void destroy() {
        assertInit();

        if (!deleteDirectory(mFilesDir)) {
            logger.error("Couldn't delete Paper dir " + mFilesDir);
        }
        mPaperDirIsCreated = false;
    }

    @Override
    public synchronized <E> void insert(String key, E value) {
        assertInit();

        final ObjectEntity<E> objectEntity = new ObjectEntity<>(value);
        objectEntity.removeReferences();

        final File originalFile = getOriginalFile(key);
        final File backupFile = makeBackupFile(originalFile);
        // Rename the current file so it may be used as a backup during the next read
        if (originalFile.exists()) {
            //Rename original to backup
            if (!backupFile.exists()) {
                if (!originalFile.renameTo(backupFile)) {
                    throw new PaperDbException("Couldn't rename file " + originalFile
                            + " to backup file " + backupFile);
                }
            } else {
                //Backup exist -> original file is broken and must be deleted
                //noinspection ResultOfMethodCallIgnored
                originalFile.delete();
            }
        }

        writeTableFile(key, objectEntity, originalFile, backupFile);
        objectEntity.restoreReferences();
    }

    @Override
    public synchronized <E> E select(String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        final File backupFile = makeBackupFile(originalFile);
        if (backupFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            originalFile.delete();
            //noinspection ResultOfMethodCallIgnored
            backupFile.renameTo(originalFile);
        }

        if (!exist(key)) {
            return null;
        }

        return readTableFile(key, originalFile);
    }

    @Override
    public synchronized boolean exist(String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists();
    }

    @Override
    public synchronized void deleteIfExists(String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        if (!originalFile.exists()) {
            return;
        }

        boolean deleted = originalFile.delete();
        if (!deleted) {
            throw new PaperDbException("Couldn't delete file " + originalFile
                    + " for table " + key);
        }
    }

    private File getOriginalFile(String key) {
        final String tablePath = mFilesDir + File.separator + key + ".pt";
        return new File(tablePath);
    }

    /**
     * Attempt to write the file, delete the backup and return true as atomically as
     * possible.  If any exception occurs, delete the new file; next time we will restore
     * from the backup.
     *
     * @param key          table key
     * @param objectEntity   table instance
     * @param originalFile file to write new data
     * @param backupFile   backup file to be used if write is failed
     */
    private <E> void writeTableFile(String key, ObjectEntity<E> objectEntity,
                                    File originalFile, File backupFile) {
        try {
            FileOutputStream fileStream = new FileOutputStream(originalFile);

            final Output kryoOutput = new Output(fileStream);
            getKryo().writeObject(kryoOutput, objectEntity);
            kryoOutput.flush();
            fileStream.flush();
            sync(fileStream);
            kryoOutput.close(); //also close file stream

            // Writing was successful, delete the backup file if there is one.
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
        } catch (IOException | KryoException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new PaperDbException("Couldn't clean up partially-written file "
                            + originalFile, e);
                }
            }
            throw new PaperDbException("Couldn't save table: " + key + ". " +
                    "Backed up table will be used on next read attempt", e);
        }
    }

    private <E> E readTableFile(String key, File originalFile) {
        try {
            final Input i = new Input(new FileInputStream(originalFile));
            final Kryo kryo = getKryo();
            //noinspection unchecked
            final ObjectEntity<E> objectEntity = kryo.readObject(i, ObjectEntity.class);
            objectEntity.restoreReferences();
            i.close();
            return objectEntity.getContent();
        } catch (FileNotFoundException | KryoException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new PaperDbException("Couldn't clean up broken/unserializable file "
                            + originalFile, e);
                }
            }
            throw new PaperDbException("Couldn't read/deserialize file " + originalFile
                    + " for table " + key, e);
        }
    }

    private String getDbPath(File filesDir, String dbName) {
        return filesDir + File.separator + dbName;
    }

    private void assertInit() {
        if (!mPaperDirIsCreated) {
            createPaperDir();
            mPaperDirIsCreated = true;
        }
    }

    private void createPaperDir() {
        if (!new File(mFilesDir).exists()) {
            boolean isReady = new File(mFilesDir).mkdirs();
            if (!isReady) {
                throw new RuntimeException("Couldn't create Paper dir: " + mFilesDir);
            }
        }
    }

    private static boolean deleteDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.toString());
                    } else {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    private File makeBackupFile(File originalFile) {
        return new File(originalFile.getPath() + ".bak");
    }

    /**
     * Perform an fsync on the given FileOutputStream.  The stream at this
     * point must be flushed but not yet closed.
     */
    private static boolean sync(FileOutputStream stream) {
        //noinspection EmptyCatchBlock
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }
}
