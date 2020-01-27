package com.evolveum.midpoint.philosopher.cmd;

import com.evolveum.midpoint.philosopher.generator.Generator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GenerateAction implements Action<CmdGenerateOptions> {

    private CmdGenerateOptions options;

    @Override
    public void init(CmdGenerateOptions options) throws Exception {
        this.options = options;
    }

    @Override
    public void execute() throws Exception {
//        if (options.isUseRemote()) {
//
//        }
//        RemoteOptions con = options.getRemote();
//
//        String pwd = con.getPassword() != null ? con.getPassword() : con.getAskPassword();
//
//        Service client = new RestJaxbServiceBuilder()
//                .url(con.getUrl())
//                .username(con.getUsername())
//                .password(pwd)
//                .authentication(AuthenticationType.BASIC)
//                .build();

        Generator generator = new Generator(options);
        generator.generate();
    }
}
