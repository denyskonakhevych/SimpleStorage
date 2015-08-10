import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by koxa on 26.07.2015.
 */
public class EntityTransformer {

    private static final String OID_KEY = ":oid";
    private static final String REF_KEY = ":refid";
    private static final String CLASS_KEY = ":class";
    private static final String WRAPPER_CLASS = ":wrapper";
    private static final String DATA_KEY = ":data";

    public static Object transform(Object entity) {
        return transform(new IdentityHashMap<Object, String>(), entity);
    }

    public static Object transform(final Map<Object, String> visitedEntities, Object entity) {
        if (entity == null || EntityTransformerUtil.isPrimitive( entity ))
            return entity;
        if (isVisited(visitedEntities, entity) && !isPrimitiveArray(entity))
            return generateRefObject(visitedEntities, entity);
        if (isDate( entity ))
            return transformDate(visitedEntities, entity);
        if (isCollectionMapOrArray(entity))
            return transformCollectionOrMap(visitedEntities, entity);
        return transformObject( visitedEntities, entity );
    }

//    private static boolean isPrimitive(Object entity) {
//        return (Primitives.isWrapperType(entity.getClass())
//                || Primitives.isPrimitive(entity.getClass())
//                || entity instanceof String);
//    }

    private static boolean isDate(Object date) {
        return date instanceof Date;
    }

//    private static boolean isPrimitiveClass(Class clazz) {
//        return (Primitives.isWrapperType(clazz)
//                || Primitives.isPrimitive(clazz)
//                || (String.class).equals( clazz ));
//    }

    private static boolean isCollectionMapOrArray(Object entity) {
        return isList(entity) || isSet(entity) || isMap(entity) || isArray(entity);
    }

    private static boolean isPrimitiveArray(Object entity) {
        return isArray(entity) && EntityTransformerUtil.isPrimitiveClass( entity.getClass().getComponentType() );
    }

    private static boolean isArray(Object entity) {
        return entity != null && entity.getClass().isArray();
    }

    private static boolean isList(Object entity) {
        return entity instanceof List;
    }

    private static boolean isSet(Object entity) {
        return entity instanceof Set;
    }

    private static boolean isMap(Object entity) {
        return entity instanceof Map;
    }

    private static Object transformCollectionOrMap(final Map<Object, String> visitedEntities, final Object entity) {
        Map<String,Object> collectionEntity = new HashMap<>();
        addOID( visitedEntities, entity, collectionEntity );
        Object transformedCollectionOrMap = null;
        collectionEntity.put(CLASS_KEY, WRAPPER_CLASS);
        if (isList( entity ))
            transformedCollectionOrMap = transformList( visitedEntities, (List) entity );
        else if (isSet( entity ))
            transformedCollectionOrMap = transformSet( visitedEntities, (Set) entity );
        else if (isMap( entity ))
            transformedCollectionOrMap = transformMap( visitedEntities, (Map) entity );
        else if (isPrimitiveArray( entity ))
            return entity;
        else if (isArray( entity ))
            transformedCollectionOrMap = transformArray(visitedEntities, entity);
        collectionEntity.put(DATA_KEY, transformedCollectionOrMap);
        return collectionEntity;
    }

    private static List transformList(final Map<Object, String> visitedEntities, final List listEntity) {
        List transformedList = new ArrayList(listEntity.size());
        for (Object entry : listEntity) {
            transformedList.add(transform(visitedEntities, entry));
        }
//        Class listType = (Class) ((TypeVariable[])listEntity.getClass().getTypeParameters())[0].getGenericDeclaration();
//        listType.newInstance()
        return transformedList;
    }

    private static Object[] transformArray(final Map<Object, String> visitedEntities, final Object arrayEntity) {
        Class arrayType = arrayEntity.getClass().getComponentType(); // cast to primitive array
        Object[] arrayEntityToObject = (Object[]) arrayEntity;
        Object[] transformedArray = (Object[]) Array.newInstance(arrayType, arrayEntityToObject.length);
        for (int i = 0; i < arrayEntityToObject.length; i++) {
            transformedArray[i] = transform(visitedEntities, arrayEntityToObject[i]);
        }
        return transformedArray;
    }

    private static Set transformSet(final Map<Object, String> visitedEntities, final Set setEntity) {
        Set transformedSet = new HashSet(setEntity.size());
        for (Object entry : setEntity) {
            transformedSet.add(transform(visitedEntities, entry));
        }
        return transformedSet;
    }

    private static Map transformMap(final Map<Object, String> visitedEntities, final Map mapEntity) {
        Map transformedMap = new HashMap(mapEntity.size());
        for (Object entryKey : mapEntity.entrySet()) {
            Object entryValue = mapEntity.get(entryKey);
            transformedMap.put( transform( visitedEntities, entryKey ), transform( visitedEntities, entryValue ) );
        }
        return transformedMap;
    }

    private static Object transformDate(final Map<Object, String> visitedEntities, Object entity) {
        Map<String, Object> transformedObject = new HashMap<>();
        addOID(visitedEntities, entity, transformedObject);
        transformedObject.put(CLASS_KEY, entity.getClass());
        Date date = (Date) entity;
        transformedObject.put( "date", date.getTime() );
        return transformedObject;
    }

    private static Object transformObject(final Map<Object, String> visitedEntities, Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        Map<String, Object> transformedObject = new HashMap<>();
        addOID(visitedEntities, entity, transformedObject);
        transformedObject.put(CLASS_KEY, entity.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object fieldValue = getFieldValue(entity, field);
            transformedObject.put(fieldName, transform(visitedEntities, fieldValue));
        }
        return transformedObject;
    }

    private static void addOID(final Map<Object, String> visitedEntities, Object entity, Map<String, Object> transformedObject) {
        String oid = generateId();
        transformedObject.put(OID_KEY, oid);
        visitedEntities.put(entity, oid);
    }

    private static Object getFieldValue(Object entity, Field field) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isVisited(final Map<Object, String> visitedEntities, Object entity) {
        return visitedEntities.containsKey(entity);
    }

    private static Map<String, String> generateRefObject(final Map<Object, String> visitedEntities, Object entity) {
        final Map<String, String> ref = new HashMap<>();
        ref.put(REF_KEY, visitedEntities.get(entity));
        return ref;
    }

    private static String generateId() {
        return java.util.UUID.randomUUID().toString();
    }
}
