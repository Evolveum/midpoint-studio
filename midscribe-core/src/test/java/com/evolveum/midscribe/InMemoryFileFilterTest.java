package com.evolveum.midscribe;

import com.evolveum.midscribe.util.InMemoryFileFilter;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by Viliam Repan (lazyman).
 */
public class InMemoryFileFilterTest {

    private static final File OBJECTS_FOLDER = new File("./src/test/resources/objects");

    @Test
    public void simpleTest() {
        InMemoryFileFilter filter = new InMemoryFileFilter(null, null);

        assertTrue(filter.accept(OBJECTS_FOLDER));
        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "resources")));

        assertFalse(filter.accept(new File(OBJECTS_FOLDER, "a.txt")));
        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "a.XML")));
        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "a.XmL")));

        assertTrue(filter.accept(new File(OBJECTS_FOLDER, "resources/hr.xml")));
    }

    @Test
    public void customIncludes() {
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
