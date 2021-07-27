package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.Service;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import com.evolveum.midpoint.studio.cmd.opts.UploadOptions;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadAction extends Action<UploadOptions> {

    @Override
    public void execute() throws Exception {
        List<MidPointObject> objects = ClientUtils.parseFile(options.getData().getReference(), Charset.forName(options.getCharset()));

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
                .password(env.getOrAskPassword());
        // todo implement
//                .proxyServer(environment.getProxyServerHost())
//                .proxyServerPort(environment.getProxyServerPort())
//                .proxyServerType(environment.getProxyServerType())
//                .proxyUsername(environment.getProxyUsername())
//                .proxyPassword(environment.getProxyPassword())
//                .ignoreSSLErrors(environment.isIgnoreSslErrors())
//                .responseTimeout(settings.getRestResponseTimeout());

        factory.messageListener(message -> System.out.println(message));

        return factory.create();
    }
}
