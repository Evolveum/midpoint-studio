package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
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
import java.util.Optional;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointClient {

    private static final Logger LOG = Logger.getInstance(MidPointClient.class);

    private static final String NOTIFICATION_KEY = "MidPoint Rest Client";

    private Project project;

    private Environment environment;

    private boolean suppressNotifications;

    private boolean suppressConsole;

    private Optional<Console> console;

    private Service client;

    public MidPointClient(Project project, @NotNull Environment environment) {
        this(project, environment, null);
    }

    public MidPointClient(Project project, @NotNull Environment environment, MidPointSettings settings) {
        this(project, environment, settings, false, false);
    }

    public MidPointClient(Project project, @NotNull Environment environment, boolean suppressNotifications, boolean suppressConsole) {
        this(project, environment, null, suppressNotifications, suppressConsole);
    }

    public MidPointClient(Project project, @NotNull Environment environment, MidPointSettings settings, boolean suppressNotifications, boolean suppressConsole) {
        this.project = project;
        this.environment = environment;
        this.suppressNotifications = suppressNotifications;
        this.suppressConsole = suppressConsole;

        if (settings == null) {
            if (project != null) {
                MidPointService ms = MidPointService.getInstance(project);
                settings = ms.getSettings();
            } else {
                settings = MidPointSettings.createDefaultSettings();
            }
        }

        if (project != null) {
            console = Optional.of(new MidPointManagerConsole(project));
        } else {
            console = Optional.ofNullable(null);
        }

        init(settings);
    }

    private void init(MidPointSettings settings) {
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
                    .ignoreSSLErrors(environment.isIgnoreSslErrors())
                    .responseTimeout(settings.getRestResponseTimeout());

            factory.messageListener((message) -> {

                if (!settings.isPrintRestCommunicationToConsole() || suppressConsole) {
                    return;
                }

                console.ifPresent(c -> c.printToConsole(environment, MidPointClient.class, message, null, Console.ContentType.INFO_OUTPUT));
            });

            client = factory.create();

            if (!suppressConsole) {
                console.ifPresent(c -> c.printToConsole(environment, MidPointClient.class, "Client created", null, Console.ContentType.INFO_OUTPUT));
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
        if (suppressConsole) {
            return;
        }

        console.ifPresent(c -> c.printToConsole(getEnvironment(), MidPointClient.class, message));
    }

    private Collection<SelectorOptions<GetOperationOptions>> buildSearchSelectorOptions(boolean raw) {
        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        if (raw) {
            options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
        }

        PrismContext ctx = client.prismContext();
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_SUBTYPE), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(OrgType.F_DISPLAY_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));

        return options;
    }

    public <O extends ObjectType> SearchResult search(Class<O> type, ObjectQuery query, boolean raw) {
        Collection<SelectorOptions<GetOperationOptions>> options = buildSearchSelectorOptions(raw);

        printToConsole("Starting objects search for " + type.getSimpleName() + ", " + options);

        SearchResult result = new SearchResult();
        try {
            result = client.search(ObjectTypes.getObjectType(type).getClassDefinition(), query, options);

            printToConsole("Search done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    /**
     * todo "move" to MidPointObject like apis, not PrismObject here if not necessary
     */
    @Deprecated
    public <O extends ObjectType> SearchResultList<O> list(Class<O> type, ObjectQuery query, boolean raw) {
        Collection<SelectorOptions<GetOperationOptions>> options = buildSearchSelectorOptions(raw);

        return list(type, query, options);
    }

    /**
     * todo "move" to MidPointObject like apis, not PrismObject here if not necessary
     */
    @Deprecated
    private <O extends ObjectType> SearchResultList<O> list(Class<O> type, ObjectQuery query,
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
            MidPointUtils.handleGenericException(project, getEnvironment(), MidPointClient.class, NOTIFICATION_KEY, message, ex);
        }
    }

    public <O extends ObjectType> MidPointObject get(Class<O> type, String oid, SearchOptions opts) throws ObjectNotFoundException {
        printToConsole("Getting object " + type.getSimpleName() + " oid= " + oid + ", " + opts);

        MidPointObject result = null;
        try {
            Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
            if (opts.raw()) {
                options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
            }

            result = client.get(ObjectTypes.getObjectType(type).getClassDefinition(), oid, options);

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

    public UploadResponse uploadRaw(MidPointObject obj, List<String> options, boolean expand, VirtualFile file) throws IOException, AuthenticationException {
        if (expand) {
            EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
            Expander expander = new Expander(environment, cm, project);

            String expanded = expander.expand(obj.getContent(), file);

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
        Expander expander = new Expander(environment, cm, project);

        String expanded = expander.expand(xml);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parse();
    }

    public List<PrismObject<?>> parseObjects(String xml) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm, project);

        String expanded = expander.expand(xml);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parseObjects();
    }

    public List<PrismObject<?>> parseObjects(VirtualFile file) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm, project);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded);
            return parser.parseObjects();
        }
    }

    public <O extends ObjectType> PrismObject<O> parseObject(VirtualFile file) throws IOException, SchemaException {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;
        Expander expander = new Expander(environment, cm, project);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded);
            return parser.parse();
        }
    }

    public PrismParser createParser(InputStream data) {
        PrismContext ctx = getPrismContext();

        return MidPointUtils.createParser(ctx, data);
    }

    public PrismParser createParser(String xml) {
        PrismContext ctx = getPrismContext();

        return MidPointUtils.createParser(ctx, xml);
    }

    public TestConnectionResult testConnection() {
        return client.testServiceConnection();
    }

    public <O extends ObjectType> OperationResult recompute(Class<O> type, String oid) {
        printToConsole("Starting recompute for " + oid + "(" + type.getSimpleName() + ")");

        try {
            return client.recompute(type, oid);
        } catch (Exception ex) {
            handleGenericException("Error occurred while recomputing object", ex);
        }

        return null;
    }

    public List<String> getSourceProfiles() throws IOException {
        return client.getSourceProfiles();
    }

    public List<ScriptObject> getSourceProfileScripts(String profile) throws IOException {
        return client.getSourceProfileScripts(profile);
    }
}
