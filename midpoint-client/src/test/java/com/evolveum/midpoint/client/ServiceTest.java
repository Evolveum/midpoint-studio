package com.evolveum.midpoint.client;

import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.impl.ServiceFactory;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import org.testng.annotations.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceTest {

    @Test
    public void test100SetupPrism() throws Exception {
        PrismContextFactory factory = new MidPointPrismContextFactory();
        PrismContext prismContext = factory.createPrismContext();
        prismContext.initialize();

        System.out.println(prismContext.getSchemaRegistry().debugDump());
    }

    @Test
    public void test110SearchUsers() throws Exception {
        Service service = new ServiceFactory()
                .url("https://demo.evolveum.com/midpoint")
                .username("administrator")
                .password("5ecr3t")
                .create();

        SearchService<UserType> search = service.search(UserType.class);
        SearchResultList<UserType> users = search.list();
        users.forEach(u -> System.out.println(u.toDebugName()));
    }

    @Test
    public void test120GetUser() throws Exception {
        Service service = new ServiceFactory()
                .url("https://demo.evolveum.com/midpoint")
                .username("administrator")
                .password("5ecr3t")
                .create();

        ObjectService<UserType> get = service.oid(UserType.class, "00000000-0000-0000-0000-000000000002");
        UserType user = get.get();
        System.out.println(user.asPrismObject().debugDump());
    }
}
