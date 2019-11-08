package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.impl.ServiceFactory;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.DownloadOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestObjectManagerImpl implements RestObjectManager {

    private static final Logger LOG = Logger.getInstance(RestObjectManagerImpl.class);

    private static final String NOTIFICATION_KEY = "MidPoint Rest";

    private MidPointManager midPointManager;
    private FileObjectManager fileObjectManager;

    private CredentialsManager credentialsManager;
    private PropertyManager propertyManager;

    private ConsoleView consoleView;

    private Environment environment;
    private Service client;

    public RestObjectManagerImpl(@NotNull Project project,
                                 @NotNull MidPointManager midPointManager,
                                 @NotNull EnvironmentManagerImpl environmentManager,
                                 @NotNull FileObjectManager fileObjectManager,
                                 @NotNull CredentialsManager credentialsManager,
                                 @NotNull PropertyManager propertyManager) {
        this.midPointManager = midPointManager;
        this.fileObjectManager = fileObjectManager;
        this.credentialsManager = credentialsManager;
        this.propertyManager = propertyManager;

        LOG.info("Initializing");

        project.getMessageBus().connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void environmentChanged(Environment oldEnv, Environment newEnv) {
                reload(newEnv);
            }
        });
    }

    @Override
    public boolean isReady() {
        return environment != null & client != null;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    private void reload(Environment env) {
        LOG.debug("Reloading environment with " + env + ", previous " + environment);
        if (Objects.equals(env, environment)) {
            return;
        }

        environment = env != null ? new Environment(env) : null;
        if (environment == null) {
            client = null;
            printToConsole("Client was destroyed");
            return;
        }

        printToConsole("Creating new client for " + env.getName());

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

            factory.messageListener((messageId, type, message) -> {

                ConsoleViewContentType contentType = ConsoleViewContentType.LOG_INFO_OUTPUT;
                if (MessageListener.MessageType.FAULT == type) {
                    contentType = ConsoleViewContentType.LOG_ERROR_OUTPUT;
                }

                printToConsole(message, contentType);
            });

            client = factory.create();

            printToConsole("Client created");
        } catch (Exception ex) {
            handleGenericException("Couldn't create rest client", ex);
        }
    }

    private Service getClient() {
        return client;
    }

    @Override
    public PrismContext getPrismContext() {
        if (client == null) {
            return null;
        }

        return getClient().prismContext();
    }

    @Deprecated
    private void printToConsole(String message) {
        printToConsole(message, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    @Deprecated
    private void printToConsole(String message, ConsoleViewContentType type) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, message, null, type);
    }

    @Override
    public <O extends ObjectType> VirtualFile[] download(Class<O> type, ObjectQuery query, DownloadOptions options) {
        Collection<SelectorOptions<GetOperationOptions>> opts = new ArrayList<>();
        if (options.raw()) {
            opts.add(SelectorOptions.create(GetOperationOptions.createRaw()));
        }

        SearchResultList<O> result = search(type, query, opts);
        if (result == null) {
            return VirtualFile.EMPTY_ARRAY;
        }

        List<O> objects = result.getList();
        List<PrismObject<O>> prisms = new ArrayList<>();
        objects.forEach(o -> prisms.add((PrismObject) o.asPrismObject()));

        return fileObjectManager.saveObjects(prisms, options.showOnly());
    }

    @Override
    public <O extends ObjectType> SearchResultList search(Class<O> type, ObjectQuery query, boolean raw) {
        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        if (raw) {
            options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
        }

        PrismContext ctx = getPrismContext();
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(ObjectType.F_SUBTYPE), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));
        options.add(SelectorOptions.create(ctx.toUniformPath(OrgType.F_DISPLAY_NAME), GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE)));

        return search(type, query, options);
    }

    @Override
    public <O extends ObjectType> PrismObject<O> get(Class<O> type, String oid, SearchOptions opts) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, "Getting object "
                + type.getSimpleName() + " oid= " + oid + ", " + opts);

        PrismObject<O> result = null;
        try {
            Service client = getClient();

            Collection<SelectorOptions<GetOperationOptions>> options =
                    SelectorOptions.createCollection(GetOperationOptions.createRaw());
            ObjectType o = client.oid(ObjectTypes.getObjectType(type).getClassDefinition(), oid).get(options);
            result = (PrismObject) o.asPrismObject();

            midPointManager.printToConsole(RestObjectManagerImpl.class, "Get done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    private <O extends ObjectType> SearchResultList<O> search(Class<O> type, ObjectQuery query,
                                                              Collection<SelectorOptions<GetOperationOptions>> options) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, "Starting objects search for "
                + type.getSimpleName() + ", " + options);

        SearchResultList<O> result = null;
        try {
            Service client = getClient();

            result = (SearchResultList) client.search(ObjectTypes.getObjectType(type).getClassDefinition())
                    .list(query, options);

            midPointManager.printToConsole(RestObjectManagerImpl.class, "Search done");
        } catch (Exception ex) {
            handleGenericException("Error occurred while searching objects", ex);
        }

        return result;
    }

    @Override
    public OperationResult testResource(String oid) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, "Starting test resource for " + oid);

        try {
            Service client = getClient();
            return client.oid(ResourceType.class, oid).testConnection();
        } catch (Exception ex) {
            handleGenericException("Error occurred while testing resource", ex);
        }

        return null;
    }

    @Override
    public void upload(String text, UploadOptions options) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploading objects (text) started, " + options);

        try {
            PrismContext prismContext = getPrismContext();

            Expander expander = new Expander(credentialsManager, propertyManager);
            String expanded = expander.expand(text);

            PrismParser parser = createParser(expanded, prismContext);
            List<PrismObject<?>> objects = parser.xml().parseObjects();

            for (PrismObject obj : objects) {
                try {
                    upload(obj, options);
                    midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploaded object " + obj.getName() + "(" + obj.getCompileTimeClass().getSimpleName() + ")");
                } catch (Exception ex) {
                    handleGenericException("Couldn't upload object '" + obj.getName() + "'", ex);
                }
            }
        } catch (Exception ex) {
            handleGenericException("Error occurred while uploading objects", ex);
        }
    }

    private PrismParser createParser(String data, PrismContext ctx) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(data).language(PrismContext.LANG_XML).context(parsingContext);
    }

    private PrismParser createParser(InputStream data, PrismContext ctx) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(data).language(PrismContext.LANG_XML).context(parsingContext);
    }

    @Override
    public void upload(List<VirtualFile> files, UploadOptions options) {
        midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploading files (" + files.size() + ") started, " + options);
        try {
            Expander expander = new Expander(credentialsManager, propertyManager);

            PrismContext prismContext = getPrismContext();

            for (VirtualFile file : files) {
                try (InputStream is = file.getInputStream()) {
                    Charset charset = file.getCharset();
                    InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

                    PrismParser parser = createParser(expanded, prismContext);
                    List<PrismObject<?>> objects = parser.xml().parseObjects();
                    for (PrismObject obj : objects) {
                        try {
                            upload(obj, options);
                            midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploaded object " + obj.getName() + "(" + obj.getCompileTimeClass().getSimpleName() + ")");
                        } catch (Exception ex) {
                            handleGenericException("Couldn't upload object '" + obj.getName() + "'", ex);
                        }
                    }
                } catch (Exception ex) {
                    handleGenericException("Couldn't parse file '" + file.getName() + "'", ex);
                }
            }
        } catch (Exception ex) {
            handleGenericException("Couldn't upload files", ex);
        }
    }

    @Override
    public List<PrismObject<?>> parseObjects(VirtualFile file) throws IOException, SchemaException {
        Expander expander = new Expander(credentialsManager, propertyManager);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded, getPrismContext());
            return parser.parseObjects();
        }
    }

    @Override
    public <O extends ObjectType> PrismObject<O> parse(VirtualFile file) {
        Expander expander = new Expander(credentialsManager, propertyManager);

        try (InputStream is = file.getInputStream()) {
            Charset charset = file.getCharset();
            InputStream expanded = expander.expand(is, charset != null ? charset : StandardCharsets.UTF_8);

            PrismParser parser = createParser(expanded, getPrismContext());
            return parser.parse();
        } catch (IOException | SchemaException ex) {
            ex.printStackTrace(); // todo implement
        }

        return null;
    }

    private void handleGenericException(String message, Exception ex) {
        NotificationAction action = null;
        if (ex instanceof ClientException) {
            OperationResult result = ((ClientException) ex).getResult();
            if (result != null) {
                action = new ShowResultNotificationAction(result);
            }
        }

        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Error",
                message + ", reason: " + ex.getMessage(), NotificationType.ERROR, action);
        midPointManager.printToConsole(RestObjectManagerImpl.class, message, ex);
    }

    private <O extends ObjectType> UploadResponse upload(PrismObject<O> obj, UploadOptions options) throws AuthenticationException {
        AddOptions opts = options.buildAddOptions();
        Service client = getClient();

        UploadResponse response = new UploadResponse();

        String oid = client.add((ObjectType) obj.asObjectable()).add(opts);
        response.setOid(oid);

        if (options.testConnection() && ResourceType.class.equals(obj.getCompileTimeClass())) {
            OperationResult result = testResource(obj.getOid());

            response.setResult(result);

            if (!result.isSuccess()) {
                MidPointUtils.publishNotification(NOTIFICATION_KEY, "Test connection error",
                        "Test connection error for '" + obj.getName() + "'", NotificationType.ERROR,
                        new ShowResultNotificationAction(result));
            }
        }

        return response;
    }
}
