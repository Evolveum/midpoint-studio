package com.evolveum.midpoint.studio.impl.sdk;

import com.evolveum.midpoint.sdk.api.SdkContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SdkService {

    private SdkContext sdkContext;

    public static SdkService getInstance(@NotNull Project project) {
        return project.getService(SdkService.class);
    }

//    public synchronized SdkContext loadSdk(File file) {
//        file = new File("/Users/lazyman/Work/monoted/git/evolveum/midpoint-studio/midpoint-sdk-impl/build/libs/midpoint-sdk-impl-4.9.0-all.jar");
//        try {
//            return new SdkFactory()
//                    .sdkFile(file)
//                    .build();
//
//        } catch (SdkException ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
    public SdkContext getContext() {
//        if (sdkContext == null) {
//            sdkContext = loadSdk(null);
//        }
        return sdkContext;
    }
}
