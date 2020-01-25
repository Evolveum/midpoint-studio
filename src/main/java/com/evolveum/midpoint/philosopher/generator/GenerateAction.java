package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.impl.restjaxb.AuthenticationType;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceBuilder;
import com.evolveum.midpoint.philosopher.Action;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GenerateAction implements Action<GenerateOptions> {

    private GenerateOptions options;

    @Override
    public void init(GenerateOptions options) throws Exception {
        this.options = options;
    }

    @Override
    public void execute() throws Exception {
        ConnectionOptions con = options.getConnection();

        String pwd = con.getPassword() != null ? con.getPassword() : con.getAskPassword();

        Service client = new RestJaxbServiceBuilder()
                .url(con.getUrl())
                .username(con.getUsername())
                .password(pwd)
                .authentication(AuthenticationType.BASIC)
                .build();

        Generator generator = new Generator(options, client);
        generator.generate();
    }
}
