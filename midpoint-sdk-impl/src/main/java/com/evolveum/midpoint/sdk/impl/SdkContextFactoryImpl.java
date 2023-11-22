package com.evolveum.midpoint.sdk.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.sdk.api.SdkComponent;
import com.evolveum.midpoint.sdk.api.SdkContext;
import com.evolveum.midpoint.sdk.api.SdkContextFactory;
import com.evolveum.midpoint.sdk.api.SdkException;

@SdkComponent(type = SdkContextFactory.class)
public class SdkContextFactoryImpl implements SdkContextFactory {

    @Override
    public SdkContext creatContext() throws SdkException {
        long start = System.currentTimeMillis();
        SdkContext.Builder builder = new SdkContext.Builder();

        try {
            MidPointPrismContextFactory factory = new MidPointPrismContextFactory();
            PrismContext ctx = factory.createPrismContext();
            ctx.initialize();

            builder.prismContext(ctx);
        } catch (Exception e) {
            throw new SdkException("Couldn't initialize PrismContext", e);
        }

        // todo implement
        System.out.println("Initialized in " + (System.currentTimeMillis() - start) + " ms");
        return builder.build();
    }
}
