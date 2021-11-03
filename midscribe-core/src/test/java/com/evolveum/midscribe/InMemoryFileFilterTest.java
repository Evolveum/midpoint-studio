package com.evolveum.midscribe;

import com.evolveum.midscribe.util.InMemoryFileFilter;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryFileFilterTest {

    private static final File OBJECTS_FOLDER = new File("./src/test/resources/objects");

    @Test
    public void simpleTest() {
        InMemoryFileFilter filter = new InMemoryFileFilter(OBJECTS_FOLDER, null, null);

        assertTrue(filter.accept(OBJECTS_FOLDER));
        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "resources")));

        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "a.txt")));
        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "a.XML")));
        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "a.XmL")));

        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "resources/hr.xml")));
    }

    @Test
    public void customIncludes() {
        InMemoryFileFilter filter = new InMemoryFileFilter(OBJECTS_FOLDER, null, Arrays.asList("users/*.xml", "tasks/misc/*"));

        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "resources/sample.xml")));

        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "users/sample.xml")));

        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "tasks/sample.xml")));
        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "tasks/misc/sample.xml")));
        // todo implement
    }

    @Test
    public void customExcludes() {
        // todo implement
    }

    @Test
    public void customIncludesExcludes() {
        // todo implement
    }
}
