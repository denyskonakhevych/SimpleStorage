import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import org.junit.runners.JUnit4;
import testdata.Group;
import testdata.Person;
import testdata.TestDataGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class StorageTest {

    private BlobStorage storage;
    private static final File FILES_DIRECTORY = new File("storage.dir");

    @Before
    public void setUp() throws Exception {
        storage = new BlobStorage(FILES_DIRECTORY, "database");
    }

    @Ignore
    @Test
    public void bidirectionalTest() {
        Person person = TestDataGenerator.genPerson(0);
        Person person1 = TestDataGenerator.genPerson(1);
        Person person2 = TestDataGenerator.genPerson(2);
        Group group = TestDataGenerator.genGroup(0);
        Group group1 = TestDataGenerator.genGroup(1);

        person.getGroups().add(group);
        person1.getGroups().add(group);
        person1.getGroups().add(group1);
        person2.getGroups().add(group1);

        group.getContacts().add(person);
        group.getContacts().add(person1);
        group1.getContacts().add(person1);
        group1.getContacts().add(person2);

        List<Person> persons = new ArrayList<>(Arrays.asList(person, person1, person2));

        storage.insert("testPerson", person);
        storage.insert("testPerson1", person1);
        storage.insert("testPerson2", person2);
        storage.insert("testPersons", persons);
        Person retrievedPerson = storage.select("testPerson");
        Person retrievedPerson1 = storage.select("testPerson1");
        Person retrievedPerson2 = storage.select("testPerson2");
        List<Person> retrievedPersons = storage.select("testPersons");
        assertEquals(person,retrievedPerson);
        assertEquals(person1,retrievedPerson1);
        assertEquals(person2,retrievedPerson2);
    }

    @Test
    public void insertTest() {
        Person person = TestDataGenerator.genPerson(0);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(person,retrievedPerson);
    }

    @Test
    public void testInsertWithRelation() {
        Person person = TestDataGenerator.genPerson(1);
        Person relatedPerson = TestDataGenerator.genPerson(2);
        person.setRelative(relatedPerson);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(person, retrievedPerson);
        assertEquals(relatedPerson,retrievedPerson.getRelative());
    }

    @Test
    public void testEqualObjectsWithNotEqualReferences() {
        Person person1 = TestDataGenerator.genPerson(1);
        Person person2 = TestDataGenerator.genPerson(1);
        assertEquals(person1, person2);
        assertTrue(person1 != person2);
        storage.insert("testPerson1", person1);
        storage.insert("testPerson2", person2);
        Person retrievedPerson1 = storage.select("testPerson1");
        Person retrievedPerson2 = storage.select("testPerson2");
        assertEquals(retrievedPerson1, retrievedPerson2);
        assertTrue(retrievedPerson1 != retrievedPerson2);
    }

    @Test
    public void testInsertWithSelfReference() {
        Person person = TestDataGenerator.genPerson(1);
        person.setRelative(person);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(retrievedPerson, retrievedPerson.getRelative());
    }

    @Test
    public void testInsertWithCyclicReference() {
        Person person = TestDataGenerator.genPerson(1);
        Person relatedPerson = TestDataGenerator.genPerson(2);
        person.setRelative(relatedPerson);
        relatedPerson.setRelative(relatedPerson);

        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(retrievedPerson.getRelative(), retrievedPerson.getRelative().getRelative());
    }

    @After
    public void clear() throws Exception {
        storage.destroy();
    }
}
