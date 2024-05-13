package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.*;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointClient {

    private static final Logger LOG = Logger.getInstance(MidPointClient.class);

    private static final String NOTIFICATION_KEY = "MidPoint Rest Client";

    private final Project project;

    private final Environment environment;

    private boolean suppressNotifications;

    private boolean suppressConsole;

    private final Optional<Console> console;

    private Service client;

    public MidPointClient(Project project, @NotNull Environment environment) {
        this(project, environment, null);
    }

    public MidPointClient(Project project, @NotNull Environment environment, MidPointConfiguration settings) {
        this(project, environment, settings, false, false);
    }

    public MidPointClient(Project project, @NotNull Environment environment, boolean suppressNotifications, boolean suppressConsole) {
        this(project, environment, null, suppressNotifications, suppressConsole);
    }

    public MidPointClient(Project project, @NotNull Environment environment, MidPointConfiguration settings, boolean suppressNotifications, boolean suppressConsole) {
        this.project = project;
        this.environment = environment;
        this.suppressNotifications = suppressNotifications;
        this.suppressConsole = suppressConsole;

        if (settings == null) {
            if (project != null) {
                MidPointService ms = MidPointService.get(project);
                settings = ms.getSettings();
            } else {
                settings = MidPointConfiguration.createDefaultSettings();
            }
        }

        if (project != null) {
            console = Optional.of(new MidPointManagerConsole(project));
        } else {
            console = Optional.ofNullable(null);
        }

        init(settings);
    }

    private void init(MidPointConfiguration settings) {
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
                    .useHttp2(environment.isUseHttp2())
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
        LOG.debug(message);

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
            result = client.list(ObjectTypes.getObjectType(type).getClassDefinition(), query, options);

            printToConsole("Search done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    private void handleGenericException(String message, Exception ex) {
        LOG.debug(message, ex);

        if (!suppressNotifications) {
            MidPointUtils.handleGenericException(project, getEnvironment(), MidPointClient.class, NOTIFICATION_KEY, message, ex);
        }
    }

    public <O extends ObjectType> MidPointObject get(Class<O> type, String oid, SearchOptions opts) {
        return get(type, oid, opts, false);
    }

    public <O extends ObjectType> MidPointObject get(Class<O> type, String oid, SearchOptions opts, boolean allowNotFound) {
        printToConsole("Getting object " + type.getSimpleName() + ", oid=" + oid + ", " + opts);

        MidPointObject result = null;
        try {
            Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
            if (opts.raw()) {
                options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
            }

            if (UserType.class.equals(type)) {
                options.add(SelectorOptions.create(UniformItemPath.create(UserType.F_JPEG_PHOTO),
                        GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
            }

            if (LookupTableType.class.equals(type)) {
                options.add(SelectorOptions.create(UniformItemPath.create(LookupTableType.F_ROW),
                        GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
            }

            result = client.get(ObjectTypes.getObjectType(type).getClassDefinition(), oid, options);

            printToConsole("Get done");
        } catch (Exception ex) {
            if (allowNotFound && ex instanceof ObjectNotFoundException) {
                return null;
            }

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

    private Expander createExpander() {
        EncryptionService cm = project != null ? EncryptionService.getInstance(project) : null;

        return new Expander(environment, cm, project);
    }

    public UploadResponse modify(MidPointObject obj, List<String> options, boolean expand, VirtualFile file)
            throws ObjectNotFoundException, IOException, AuthenticationException, SchemaException {

        if (expand) {
            obj = expand(obj, file);
        }

        UploadResponse response = new UploadResponse();

        if (obj.isDelta()) {
            client.modify(obj, options);
        } else {
            // todo just nasty options handling. This definitely calls for cleanup (whole midpoint client api attempt mess)
            SearchOptions opts = new SearchOptions();
            opts.raw(options.contains("raw"));

            MidPointObject existingObject = get(obj.getType().getClassDefinition(), obj.getOid(), opts, true);
            if (existingObject == null) {
                String oid = client.add(obj, options);
                response.setOid(oid);

                return response;
            }
            PrismObject existing = parseObject(existingObject.getContent());
            PrismObject current = parseObject(obj.getContent());

            ObjectDelta<?> delta = existing.diff(current);
            ObjectModificationType deltaType = DeltaConvertor.toObjectModificationType(delta);
            String deltaXml = serialize(deltaType);

            MidPointObject deltaObj = MidPointObject.copy(obj);
            deltaObj.setDelta(true);
            deltaObj.setContent(deltaXml);

            client.modify(deltaObj, options);
        }

        return response;
    }

    private MidPointObject expand(MidPointObject obj, VirtualFile file) {
        Expander expander = createExpander();

        String expanded = expander.expand(obj.getContent(), file);

        MidPointObject expandedObject = ClientUtils.parseText(expanded).get(0);

        obj.setContent(expanded);
        obj.setOid(expandedObject.getOid());
        obj.setName(expandedObject.getName());
        obj.setDisplayName(expandedObject.getDisplayName());

        return obj;
    }

    public UploadResponse uploadRaw(MidPointObject obj, List<String> options, boolean expand, VirtualFile file) throws IOException, AuthenticationException {
        if (expand) {
            obj = expand(obj, file);
        }

        UploadResponse response = new UploadResponse();

        String oid;
        if (obj.isDelta()) {
            oid = client.modify(obj, options);
        } else {
            oid = client.add(obj, options);
        }

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
        return parseObject(xml, null);
    }

    public PrismObject<?> parseObject(String xml, ExpanderOptions opts) throws IOException, SchemaException {
        return parseObject(xml, null, opts);
    }

    public PrismObject<?> parseObject(String xml, VirtualFile file, ExpanderOptions opts) throws IOException, SchemaException {
        Expander expander = createExpander();

        String expanded = expander.expand(xml, file, opts);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parse();
    }

    public List<PrismObject<?>> parseObjects(String xml) throws IOException, SchemaException {
        Expander expander = createExpander();

        String expanded = expander.expand(xml);

        PrismParser parser = createParser(new ByteArrayInputStream(expanded.getBytes()));
        return parser.parseObjects();
    }

    public List<PrismObject<?>> parseObjects(VirtualFile file) throws IOException, SchemaException {
        Expander expander = createExpander();

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded);
            return parser.parseObjects();
        }
    }

    public <O extends ObjectType> PrismObject<O> parseObject(VirtualFile file) throws IOException, SchemaException {
        Expander expander = createExpander();

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset);

            PrismParser parser = createParser(expanded);
            return parser.parse();
        }
    }

    public PrismParser createParser(InputStream data) {
        PrismContext ctx = getPrismContext();

        return ClientUtils.createParser(ctx, data);
    }

    public PrismParser createParser(String xml) {
        PrismContext ctx = getPrismContext();

        return ClientUtils.createParser(ctx, xml);
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

    public Map<SchemaFileType, String> getExtensionSchemas() {
        printToConsole("Getting extension schemas");

        Map<SchemaFileType, String> result = Collections.emptyMap();
        try {
            result = client.getExtensionSchemas();

            printToConsole("Extension schemas fetched");
        } catch (Exception ex) {
            handleGenericException("Error occurred while trying to fetch extension schemas", ex);
        }

        return result;
    }

    public void setSuppressConsole(boolean suppressConsole) {
        this.suppressConsole = suppressConsole;
    }

    public void setSuppressNotifications(boolean suppressNotifications) {
        this.suppressNotifications = suppressNotifications;
    }
}
