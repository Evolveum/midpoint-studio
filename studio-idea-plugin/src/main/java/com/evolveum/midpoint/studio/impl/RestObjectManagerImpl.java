package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.client.api.AddOptions;
import com.evolveum.midpoint.client.api.MessageListener;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.impl.ServiceFactory;
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
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public RestObjectManagerImpl(@NotNull MidPointManager midPointManager,
                                 @NotNull EnvironmentManagerImpl environmentManager,
                                 @NotNull FileObjectManager fileObjectManager,
                                 @NotNull CredentialsManager credentialsManager,
                                 @NotNull PropertyManager propertyManager) {
        this.midPointManager = midPointManager;
        this.fileObjectManager = fileObjectManager;
        this.credentialsManager = credentialsManager;
        this.propertyManager = propertyManager;

        environmentManager.addListener(this);

        reload(environmentManager.getSelected());
    }

    @Override
    public <T> void onEvent(Event<T> evt) {
        if (!EnvironmentManagerImpl.EVT_SELECTION_CHANGED.equals(evt.getId())) {
            return;
        }

        reload((Environment) evt.getObject());
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

        try {
            this.environment = env != null ? (Environment) BeanUtils.cloneBean(env) : null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

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

            PrismParser parser = prismContext.parserFor(expanded);
            List<PrismObject<?>> objects = parser.xml().parseObjects();

            for (PrismObject obj : objects) {
                upload(obj, options);
                midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploaded object " + obj.getName() + "(" + obj.getCompileTimeClass().getSimpleName() + ")");
            }
        } catch (Exception ex) {
            handleGenericException("Error occurred while uploading objects", ex);
        }
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

                    PrismParser parser = prismContext.parserFor(expanded);
                    List<PrismObject<?>> objects = parser.xml().parseObjects();
                    for (PrismObject obj : objects) {
                        upload(obj, options);
                        midPointManager.printToConsole(RestObjectManagerImpl.class, "Uploaded object " + obj.getName() + "(" + obj.getCompileTimeClass().getSimpleName() + ")");
                    }
                }
            }
        } catch (Exception ex) {
            handleGenericException("Couldn't upload files", ex);
        }
    }

    private void handleGenericException(String message, Exception ex) {
        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Error",
                message + ", reason: " + ex.getMessage(), NotificationType.ERROR);
        midPointManager.printToConsole(RestObjectManagerImpl.class, message, ex);
    }

    private <O extends ObjectType> void upload(PrismObject<O> obj, UploadOptions options) {
        Service client;
        try {
            AddOptions opts = options.buildAddOptions();
            client = getClient();
            client.add((ObjectType) obj.asObjectable()).post(opts);

            if (options.testConnection() && ResourceType.class.equals(obj.getCompileTimeClass())) {
                OperationResult result = testResource(obj.getOid());

                // todo add handler to method parameters to handle upload/test connection results after each object
//                String status = result.isSuccess() ? "SUCCESS" : result.dump(true);
            }
        } catch (Exception ex) {
            // todo implement, handle exception
            ex.printStackTrace();
        }
    }
}
