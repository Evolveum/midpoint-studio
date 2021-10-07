package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.Service;
import com.evolveum.midpoint.studio.cmd.opts.UploadOptions;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Notes:
 * - upload/execute raw
 * - first + stop on first error
 * - first + recompute
 * - first + test resource
 * - first + test resource + bulk action (validate)
 * <p>
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

        List<MidPointObject> objects = ClientUtils.parseFile(options.getData().getReference(), context.getCharset());
        List<MidPointObject> filtered = ClientUtils.filterObjectTypeOnly(objects, false);

        Service service = buildClient();

        for (MidPointObject obj : objects) {
            try {
                if (obj.isExecutable()) {
                    ExecuteScriptResponseType response = service.execute(obj.getContent());

                    if (response != null) {
                        OperationResultType res = response.getResult();
//                        result = OperationResult.createOperationResult(res);
                    }
                } else {
                    String oid = service.add(obj, Arrays.asList(Service.OPTION_RAW, Service.OPTION_IS_IMPORT));
                }
            } catch (Exception ex) {

            }

            if (options.isRecompute()) {

            }

            if (options.isTestResource()) {

            }

            if (options.isValidateResource()) {

            }
        }
    }
}
