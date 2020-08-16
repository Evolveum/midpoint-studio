package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.scripting.ObjectProcessingOutput;
import com.evolveum.midpoint.client.api.scripting.OperationSpecificData;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;
import com.evolveum.midpoint.client.api.scripting.ValueGenerationData;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestScriptingUtil implements ScriptingUtil {

    @Override
    public <X extends OperationSpecificData> List<ObjectProcessingOutput<X>> extractObjectProcessingOutput(ExecuteScriptResponseType executeScriptResponseType, Function<Object, X> function) {
        return null;
    }

    @Override
    public List<ObjectProcessingOutput<ValueGenerationData<String>>> extractPasswordGenerationResults(ExecuteScriptResponseType executeScriptResponseType) {
        return null;
    }
}
