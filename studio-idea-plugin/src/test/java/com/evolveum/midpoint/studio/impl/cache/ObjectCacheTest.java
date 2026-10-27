package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Covers that a cache read never loads on the caller's thread.
 */
public class ObjectCacheTest extends BasePlatformTestCase {

    private static final String OID = "00000000-0000-0000-0000-000000000001";

    private final CountDownLatch releaseSecondLoad = new CountDownLatch(1);

    private final AtomicInteger loads = new AtomicInteger();

    private ObjectCache<UserType> cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cache = new ObjectCache<>(getProject(), UserType.class) {

            @Override
            protected Collection<MidPointObject> loadObjects() {
                int load = loads.incrementAndGet();
                if (load == 2) {
                    try {
                        assertTrue(releaseSecondLoad.await(10, TimeUnit.SECONDS));
                    } catch (InterruptedException ex) {
                        throw new AssertionError(ex);
                    }
                }
                return List.of(user("user-v" + load));
            }
        };
    }

    public void testEmptyCacheAnswersNullAndLoadsInBackground() throws Exception {
        assertNull("nothing cached, nothing loaded on this thread", cache.get(OID));

        cache.waitForReload();

        assertEquals("user-v1", name(cache.get(OID)));
        assertEquals(1, loads.get());
    }

    public void testExpiredCacheAnswersStaleValueAndRefreshesInBackground() throws Exception {
        cache.get(OID);
        cache.waitForReload();
        assertEquals("user-v1", name(cache.get(OID)));

        cache.expireForTests();

        assertEquals("stale value while the refresh runs", "user-v1", name(cache.get(OID)));
        assertTrue("refresh running", cache.isReloading());
        assertEquals("still stale, no second request", "user-v1", name(cache.get(OID)));

        releaseSecondLoad.countDown();
        cache.waitForReload();

        assertEquals("user-v2", name(cache.get(OID)));
        assertEquals(2, loads.get());
    }

    private static MidPointObject user(String name) {
        String xml = """
                <user xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3" oid="%s">
                    <name>%s</name>
                </user>
                """.formatted(OID, name);
        MidPointObject object = new MidPointObject(xml, ObjectTypes.USER, false);
        object.setOid(OID);
        object.setName(name);
        return object;
    }

    private static String name(UserType user) {
        return user != null && user.getName() != null ? user.getName().getOrig() : null;
    }
}
