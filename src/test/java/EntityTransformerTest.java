import org.junit.Test;
import testdata.Person;
import testdata.TestDataGenerator;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by koxa on 28.07.2015.
 */
public class EntityTransformerTest {

    //private static Gson gson = new Gson();


    @Test
    public void testTransformString() {
        Object obj = EntityTransformer.transform("String");
        assertEquals(String.class, obj.getClass());
    }

    @Test
    public void testTransformInteger() {
        Object obj = EntityTransformer.transform(42);
        assertEquals(Integer.class, obj.getClass());
    }

    @Test
    public void testTransformDouble() {
        Object obj = EntityTransformer.transform(42.42);
        assertEquals(Double.class, obj.getClass());
    }

    @Test
    public void testTransformFloat() {
        Object obj = EntityTransformer.transform(42.42f);
        assertEquals(Float.class, obj.getClass());
    }

    @Test
    public void testTransformLong() {
        Object obj = EntityTransformer.transform(42L);
        assertEquals(Long.class, obj.getClass());
    }

    @Test
    public void testTransformChar() {
        Object obj = EntityTransformer.transform('c');
        assertEquals(Character.class, obj.getClass());
    }

    @Test
    public void testTransformBoolean() {
        Object obj = EntityTransformer.transform(true);
        assertEquals(Boolean.class, obj.getClass());
    }

    @Test
    public void testTransformByte() {
        byte b = 42;
        Object obj = EntityTransformer.transform(b);
        assertEquals(Byte.class, obj.getClass());
    }

    @Test
    public void testTransformShort() {
        short s = 300;
        Object obj = EntityTransformer.transform(s);
        assertEquals(Short.class, obj.getClass());
    }

    @Test
    public void testTransformDate() {
        Date date = new Date();
        Object obj = EntityTransformer.transform(date);
        System.out.println( obj );
    }

    @Test
    public void testTransformNull() {
        Object obj = EntityTransformer.transform(null);
        assertNull( obj );
    }

    @Test
    public void testTransformPerson() {
        Person person = TestDataGenerator.genPerson(0);
        person.setRelative(person);
        Object transformed = EntityTransformer.transform(person);
        System.out.println( transformed );
    }

    @Test
    public void testTransformListOfList() {
        List<List<?>> list = new ArrayList<List<?>>();
        list.add( list );
        Object transformed = EntityTransformer.transform(list);
        assertNotNull( transformed );
        assertTrue( transformed instanceof Map );
        Map parameters = (Map) transformed;
        assertTrue( parameters.get( ":data" ) instanceof List );
        List transformedList = (List) parameters.get( ":data" );
        String refid = (String) ((Map)(transformedList.get( 0 ))).get( ":refid" );
        String oid = (String) parameters.get( ":oid" );
        assertEquals( oid, refid );
    }

    @Test
    public void testTransformSetOfSet() {
        Set<Set<?>> set = new HashSet<Set<?>>();
        set.add( set );
        Object transformed = EntityTransformer.transform(set);
        assertNotNull( transformed );
        assertTrue( transformed instanceof Map );
        Map parameters = (Map) transformed;
        assertTrue( parameters.get( ":data" ) instanceof Set );
        Set transformedSet = (Set) parameters.get( ":data" );
        assertTrue( transformedSet.iterator().hasNext() );
        String refid = (String) ((Map) transformedSet.iterator().next()).get( ":refid" );
        String oid = (String) parameters.get( ":oid" );
        assertEquals( oid, refid );
    }

    @Test
    public void testTransformArrayOfArrays() {
        int[][] array = new int[4][3];
        int[] subArray = new int[] {4,2};
        array[0] = subArray;
        array[1] = array[0];
        array[2] = new int[] {1,2,3};

        Object transformed = EntityTransformer.transform(array);
        System.out.println(transformed);
    }
}
