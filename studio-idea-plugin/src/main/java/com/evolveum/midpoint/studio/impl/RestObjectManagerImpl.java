package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.evolveum.midpoint.studio.action.browse.DownloadOptions;
import com.evolveum.midpoint.client.api.AddOptions;
import com.evolveum.midpoint.client.api.MessageListener;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.impl.ServiceFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;

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
            printToConsole("Couldn't create client, reason: " + ex.getMessage());
            // todo error handling
            ex.printStackTrace();
            throw new RuntimeException(ex);

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

    private void printToConsole(String message) {
        printToConsole(message, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    private void printToConsole(String message, ConsoleViewContentType type) {
        if (consoleView == null) {
            consoleView = midPointManager.getConsole();
        }

        if (consoleView != null) {
            consoleView.print("EnvironmentManager: " + message + "\n", type);
        }
    }

    @Override
    public <O extends ObjectType> void download(Class<O> type, ObjectQuery query, DownloadOptions options) {
        //todo implement

        SearchResultList<ObjectType> result = null;
        try {
            Service client = getClient();
            Collection<SelectorOptions<GetOperationOptions>> opts = new ArrayList<>();
            if (options.raw()) {
                opts.add(SelectorOptions.create(GetOperationOptions.createRaw()));
            }

            result = (SearchResultList) client.search(ObjectTypes.getObjectType(type).getClassDefinition())
                    .list(query, opts);
//                printInfoMessage("Search in progress");
        } catch (Exception ex) {
            ex.printStackTrace(); // todo implement
//                printErrorMessage("Couldn't list objects, reason: " + ex.getMessage());
        }

        if (result == null) {
//            browseToolPanel.setState(BrowseToolPanel.State.DONE);
            // todo warning
            return;
        }

        List<ObjectType> objects = result.getList();
        List<PrismObject<O>> prisms = new ArrayList<>();
        objects.forEach(o -> prisms.add((PrismObject) o.asPrismObject()));

        fileObjectManager.saveObjects(prisms, options.showOnly());
    }

    @Override
    public <O extends ObjectType> SearchResultList search(Class<O> type, ObjectQuery query, boolean raw) {
        SearchResultList result = null;
        try {
            Service service = getClient();

            Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
            // todo retreive only name, displayname and subtype

            if (raw) {
                options.add(SelectorOptions.create(GetOperationOptions.createRaw()));
            }

            result = service.search(type).list(query, options);
        } catch (Exception ex) {
            ex.printStackTrace(); // todo implement
//                printErrorMessage("Couldn't list objects, reason: " + ex.getMessage());
        }

        return result;
    }

    @Override
    public OperationResult testResource(String oid) {
        try {
            Service client = getClient();
            return client.oid(ResourceType.class, oid).testConnection();

        } catch (Exception ex) {
            ex.printStackTrace(); // todo implement
        }

        return null;
    }

    @Override
    public void upload(String text, UploadOptions options) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                PrismContext prismContext = getPrismContext();

                Expander expander = new Expander(credentialsManager, propertyManager);
                String expanded = expander.expand(text);

                PrismParser parser = prismContext.parserFor(expanded);
                List<PrismObject<?>> objects = parser.xml().parseObjects();

                for (PrismObject obj : objects) {
                    upload(obj, options);
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // todo implement
            }
        });
    }

    @Override
    public void upload(List<VirtualFile> files, UploadOptions options) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
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
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // todo implement
            }
        });
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
