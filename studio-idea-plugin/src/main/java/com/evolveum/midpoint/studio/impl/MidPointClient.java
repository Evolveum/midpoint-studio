package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.client.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO CLEAN THIS WHOLE CLASS AND UNDERLYING CLIENT API, IT'S A MESS.
 * <p>
 * Created by Viliam Repan (lazyman).
 */
public class MidPointClient {

    private static final Logger LOG = Logger.getInstance(MidPointClient.class);

    private static final String NOTIFICATION_KEY = "MidPoint Rest Client";

    private Project project;

    private Environment environment;

    private MidPointService midPointManager;

    private Service client;

    private boolean suppressNotifications;

    public MidPointClient(Project project, @NotNull Environment environment) {
        this(project, environment, false);
    }

    public MidPointClient(Project project, @NotNull Environment environment, boolean suppressNotifications) {
        this.project = project;
        this.environment = environment;
        this.suppressNotifications = suppressNotifications;

        if (project != null) {
            this.midPointManager = MidPointService.getInstance(project);
        }

        init();
    }

    private void init() {
        LOG.debug("Initialization of rest client for environment " + environment.getName());
        long time = System.currentTimeMillis();

        try {
            ServiceFactory factory = new ServiceFactory();
            factory
                    .url(environment.getUrl())
                    .username(environment.getUsername())
                    .password(environment.getPassword())
                    .proxyServer(environment.getProxyServerHost())
                    .proxyServerPort(environment.getProxyServerPort())
                    .proxyServerType(environment.getProxyServerType())
                    .proxyUsername(environment.getProxyUsername())
                    .proxyPassword(environment.getProxyPassword())
                    .ignoreSSLErrors(environment.isIgnoreSslErrors());

            factory.messageListener((message) -> {

                if (midPointManager == null || !midPointManager.getSettings().isPrintRestCommunicationToConsole()) {
                    return;
                }

                midPointManager.printToConsole(MidPointClient.class, message, null, ConsoleViewContentType.LOG_INFO_OUTPUT);
            });

            client = factory.create();

            if (midPointManager != null) {
                midPointManager.printToConsole(MidPointClient.class, "Client created", null, ConsoleViewContentType.LOG_INFO_OUTPUT);
            }
        } catch (Exception ex) {
            handleGenericException("Couldn't create rest client", ex);
        }

        LOG.debug("Rest client initialized in " + (System.currentTimeMillis() - time) + "ms");
    }

    public Project getProject() {
        return project;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public PrismContext getPrismContext() {
        return client.prismContext();
    }

    private void printToConsole(String message) {
        if (midPointManager != null) {
            midPointManager.printToConsole(MidPointClient.class, message);
        }
    }

    public <O extends ObjectType> SearchResultList search(Class<O> type, ObjectQuery query, boolean raw) {
        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        if (raw) {
            options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
        }

        PrismContext ctx = client.prismContext();
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_SUBTYPE), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(OrgType.F_DISPLAY_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));

        return search(type, query, options);
    }

    private <O extends ObjectType> SearchResultList<O> search(Class<O> type, ObjectQuery query,
                                                              Collection<SelectorOptions<GetOperationOptions>> options) {
        printToConsole("Starting objects search for " + type.getSimpleName() + ", " + options);

        SearchResultList<O> result = null;
        try {
            result = (SearchResultList) client.list(ObjectTypes.getObjectType(type).getClassDefinition(), query, options);

            printToConsole("Search done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    private void handleGenericException(String message, Exception ex) {
        if (!suppressNotifications) {
            MidPointUtils.handleGenericException(project, MidPointClient.class, NOTIFICATION_KEY, message, ex);
        }
    }

    public <O extends ObjectType> String getRaw(Class<O> type, String oid, SearchOptions opts) throws ObjectNotFoundException {
        printToConsole("Getting object " + type.getSimpleName() + " oid= " + oid + ", " + opts);

        String result = null;
        try {
            Collection<SelectorOptions<GetOperationOptions>> options =
                    SelectorOptions.createCollection(GetOperationOptions.createRaw());
            result = client.getRaw(ObjectTypes.getObjectType(type).getClassDefinition(), oid, options);

            printToConsole("Get done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    public <O extends ObjectType> PrismObject<O> get(Class<O> type, String oid, SearchOptions opts) {
        printToConsole("Getting object " + type.getSimpleName() + " oid= " + oid + ", " + opts);

        PrismObject<O> result = null;
        try {
            Collection<SelectorOptions<GetOperationOptions>> options =
                    SelectorOptions.createCollection(GetOperationOptions.createRaw());
            ObjectType o = client.get(ObjectTypes.getObjectType(type).getClassDefinition(), oid, options);
            result = (PrismObject) o.asPrismObject();

            printToConsole("Get done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    public OperationResult testResource(String oid) {
        printToConsole("Starting test resource for " + oid);

        try {
            return client.testResourceConnection(oid);
        } catch (Exception ex) {
            handleGenericException("Error occurred while testing resource", ex);
        }

        return null;
    }

    public <O extends ObjectType> void delete(Class<O> type, String oid, DeleteOptions options) throws AuthenticationException, ObjectNotFoundException, IOException {
        client.delete(type, oid, options);
    }

    public ExecuteScriptResponseType execute(String object) throws IOException, SchemaException, AuthenticationException {
        return client.execute(object);
    }

    public UploadResponse uploadRaw(MidPointObject obj, List<String> options, boolean expand) throws IOException, AuthenticationException {
        if (expand) {
            EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
            Expander expander = new Expander(environment, cm);

            String expanded = expander.expand(obj.getContent());

            obj = MidPointObject.copy(obj);
            obj.setContent(expanded);
        }

        UploadResponse response = new UploadResponse();

        String oid = client.add(obj, options);
        if (oid == null && obj.getOid() != null) {
            oid = obj.getOid();
        }
        response.setOid(oid);

        return response;
    }

    public <O extends ObjectType> UploadResponse upload(PrismObject<O> obj, List<String> options) throws SchemaException, IOException, AuthenticationException {
        UploadResponse response = new UploadResponse();

        PrismSerializer<String> serializer = getPrismContext().serializerFor(PrismContext.LANG_XML);

        String content = serializer.serialize(obj.getValue(), obj.getElementName().asSingleName());
        MidPointObject object = new MidPointObject(content, ObjectTypes.getObjectType(obj.getCompileTimeClass()), false);
        object.setOid(obj.getOid());
        if (obj.getName() != null) {
            object.setName(obj.getName().getOrig());
        }

        String oid = client.add(object, options);
        response.setOid(oid);

        return response;
    }

    public String serialize(Object obj) throws SchemaException {
        return client.context().serialize(obj);
    }

    public PrismObject<?> parseObject(String xml) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm);

        String expanded = expander.expand(xml);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parse();
    }

    public List<PrismObject<?>> parseObjects(String xml) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm);

        String expanded = expander.expand(xml);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parseObjects();
    }

    public List<PrismObject<?>> parseObjects(VirtualFile file) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded);
            return parser.parseObjects();
        }
    }

    public <O extends ObjectType> PrismObject<O> parseObject(VirtualFile file) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded);
            return parser.parse();
        }
    }

    public PrismParser createParser(InputStream data) {
        PrismContext ctx = getPrismContext();

        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(data).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public PrismParser createParser(String xml) {
        PrismContext ctx = getPrismContext();

        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(xml).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public TestConnectionResult testConnection() {
        return client.testServiceConnection();
    }
}
