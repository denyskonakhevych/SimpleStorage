import com.google.gson.Gson;
import org.junit.Test;
import testdata.Person;
import testdata.TestDataGenerator;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        Object obj = EntityTransformer.transform(42.2);
        assertEquals(Double.class, obj.getClass());
    }

    @Test
    public void testTransformFloat() {
        Object obj = EntityTransformer.transform(42.2f);
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
        System.out.println(obj);
    }

    @Test
    public void testTransformNull() {
        Object obj = EntityTransformer.transform(null);
        assertNull(obj);
    }

    @Test
    public void testTransformPerson() {
        Person person = TestDataGenerator.genPerson(0);
        person.setRelative(person);
        Object transformed = EntityTransformer.transform(person);
        System.out.println(transformed);
    }

    @Test
    public void testTransformListOfList() {
        List<List<?>> list = new ArrayList<List<?>>();
        list.add(list);
        Object transformed = EntityTransformer.transform(list);
        System.out.println(transformed);
    }

    @Test
    public void testTransformSetOfSet() {
        Set<Set<?>> set = new HashSet<Set<?>>();
        set.add(set);
        Object transformed = EntityTransformer.transform(set);
        System.out.println(transformed);
    }
}
