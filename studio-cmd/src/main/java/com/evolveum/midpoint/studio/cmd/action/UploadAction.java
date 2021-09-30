package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.Service;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import com.evolveum.midpoint.studio.cmd.opts.UploadOptions;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadAction extends Action<UploadOptions> {

    @Override
    public void execute() throws Exception {
        String value = options.getData().getValue();
        if (value != null) {
            // todo process input

            return;
        }

        File[] filesToProcess = StudioUtil.listFiles(options.getData());


        List<MidPointObject> objects = ClientUtils.parseFile(options.getData().getReference(), Charset.forName(options.getCharset()));
        List<MidPointObject> filtered = ClientUtils.filterObjectTypeOnly(objects, false);

        // overwrite, isImport, raw
        Service service = buildClient();

        for (MidPointObject obj : objects) {
            service.add(obj, Arrays.asList("raw", "isImport"));
        }
    }

    private Service buildClient() throws Exception {
        EnvironmentOptions env = StudioUtil.getOptions(context.getJc(), EnvironmentOptions.class);

        ServiceFactory factory = new ServiceFactory();
        factory
                .url(env.getUrl())
                .username(env.getUsername())
                .password(env.getOrAskPassword())
                .proxyServer(env.getProxyHost())
                .proxyServerPort(env.getProxyPort())
                .proxyServerType(env.getProxyType())
                .proxyUsername(env.getProxyUsername())
                .proxyPassword(env.getOrAskProxyPassword())
                .ignoreSSLErrors(env.isIgnoreSSLErrors())
                .responseTimeout(env.getResponseTimeout());

        factory.messageListener(message -> System.out.println(message));

        return factory.create();
    }
}
