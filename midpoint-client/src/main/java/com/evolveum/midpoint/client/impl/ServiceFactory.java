package com.evolveum.midpoint.client.impl;

import com.evolveum.midpoint.client.api.MessageListener;
import com.evolveum.midpoint.client.api.ProxyType;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.common.rest.MidpointAbstractProvider;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.util.DOMUtilSettings;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.exception.CommonException;
import com.evolveum.midpoint.util.exception.SystemException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ServiceFactory {

    private static final PrismContext DEFAULT_PRISM_CONTEXT;

    static {
        try {
            DOMUtilSettings.setAddTransformerFactorySystemProperty(false);
            // todo create web client just to obtain extension schemas!

            PrismContextFactory factory = new MidPointPrismContextFactory();
            PrismContext prismContext = factory.createPrismContext();
            prismContext.initialize();

            DEFAULT_PRISM_CONTEXT = prismContext;
        } catch (Exception ex) {
            throw new IllegalStateException("Couldn't initialize prism context", ex);
        }
    }

    private String url;

    private String username;

    private String password;

    private boolean ignoreSSLErrors;

    private String proxyServer;

    private Integer proxyServerPort;

    private ProxyType proxyServerType = ProxyType.HTTP;

    private String proxyUsername;

    private String proxyPassword;

    private MessageListener messageListener;

    public ServiceFactory url(final String url) {
        this.url = url;
        return this;
    }

    public ServiceFactory username(final String username) {
        this.username = username;
        return this;
    }

    public ServiceFactory password(final String password) {
        this.password = password;
        return this;
    }

    public ServiceFactory proxyServer(final String proxyServer) {
        this.proxyServer = proxyServer;
        return this;
    }

    public ServiceFactory proxyServerPort(final Integer proxyServerPort) {
        this.proxyServerPort = proxyServerPort;
        return this;
    }

    public ServiceFactory proxyServerType(final ProxyType proxyServerType) {
        this.proxyServerType = proxyServerType;
        return this;
    }

    public ServiceFactory proxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
        return this;
    }

    public ServiceFactory proxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
        return this;
    }

    public ServiceFactory messageListener(final MessageListener messageListener) {
        this.messageListener = messageListener;
        return this;
    }

    public ServiceFactory ignoreSSLErrors(final boolean ignoreSSLErrors) {
        this.ignoreSSLErrors = ignoreSSLErrors;
        return this;
    }

    public Service create() throws Exception {
        List<Provider> providers = (List) Arrays.asList(
                new com.bea.xml.stream.XMLOutputFactoryBase(),
                setupProvider(new CompatibilityXmlProvider(DEFAULT_PRISM_CONTEXT), DEFAULT_PRISM_CONTEXT));
//                setupProvider(new MidpointXmlProvider(), prismContext),
//                setupProvider(new MidpointJsonProvider<>(), prismContext),
//                setupProvider(new MidpointYamlProvider<>(), prismContext));

        WebClient client;
        if (username != null) {
            client = WebClient.create(url, providers, username, password, null);
        } else {
            client = WebClient.create(url, providers);
        }

        if (ignoreSSLErrors) {
            HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();

            TLSClientParameters params = conduit.getTlsClientParameters();
            if (params == null) {
                params = new TLSClientParameters();
                conduit.setTlsClientParameters(params);
            }

            params.setTrustManagers(new TrustManager[]{new EmptyTrustManager()});
            params.setDisableCNCheck(true);
        }

        if (proxyServer != null) {
            HTTPConduit conduit = (HTTPConduit) WebClient.getConfig(client).getConduit();
            HTTPClientPolicy httpClientPolicy = conduit.getClient();
            if (httpClientPolicy == null) {
                httpClientPolicy = new HTTPClientPolicy();
                conduit.setClient(httpClientPolicy);
            }
            httpClientPolicy.setProxyServer(proxyServer);
            httpClientPolicy.setProxyServerPort(proxyServerPort);
            httpClientPolicy.setProxyServerType(proxyServerType.getType());

            if (proxyUsername != null) {
                ProxyAuthorizationPolicy proxyAuthorization = conduit.getProxyAuthorization();
                if (proxyAuthorization == null) {
                    proxyAuthorization = new ProxyAuthorizationPolicy();
                    conduit.setProxyAuthorization(proxyAuthorization);
                }
                proxyAuthorization.setUserName(proxyUsername);
                proxyAuthorization.setPassword(proxyPassword);
            }
        }

        client.accept(MediaType.APPLICATION_XML);   // todo add json, yaml
        client.type(MediaType.APPLICATION_XML);

        ClientConfiguration config = WebClient.getConfig(client);

        LoggingFeature logging = new LoggingFeature();
        logging.setLimit(100);
//        logging.setPrettyLogging(true);   // todo fix pretty print, doesn't work
        if (messageListener != null) {
            logging.setSender(event -> {
                String msg = LogMessageFormatter.format(event);

                MessageListener.MessageType type;
                switch (event.getType()) {
                    case REQ_IN:
                    case REQ_OUT:
                        type = MessageListener.MessageType.REQUEST;
                        break;
                    case RESP_IN:
                    case RESP_OUT:
                        type = MessageListener.MessageType.RESPONSE;
                        break;
                    case FAULT_IN:
                    case FAULT_OUT:
                        type = MessageListener.MessageType.FAULT;
                        break;
                    default:
                        type = null;
                }

                messageListener.handleMessage(event.getMessageId(), type, msg);
            });
        }
        logging.initialize(config.getEndpoint(), config.getBus());

        ServiceContext context = new ServiceContext(DEFAULT_PRISM_CONTEXT, client);

        return new ServiceImpl(context);
    }

    private MidpointAbstractProvider setupProvider(MidpointAbstractProvider provider, PrismContext prismContext) {

        try {
            FieldUtils.writeField(provider, "prismContext", prismContext, true);
            FieldUtils.writeField(provider, "localizationService", new LocalizationServiceImpl(), true);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        return provider;
    }

    private static class LocalizationServiceImpl implements LocalizationService {

        @Override
        public String translate(LocalizableMessage msg, Locale locale, String defaultMessage) {
            String translated = translate(msg, locale);
            return translated != null ? translated : defaultMessage;
        }

        @Override
        public String translate(PolyString polyString, Locale locale, boolean allowOrig) {
            String def = allowOrig ? polyString.getOrig() : null;
            return translate(polyString.getOrig(), new Object[]{}, locale, def);
        }

        @Override
        public Locale getDefaultLocale() {
            return Locale.getDefault();
        }

        @Override
        public String translate(String key, Object[] params, Locale locale) {
            return translate(key, params, locale, null);
        }

        @Override
        public String translate(String key, Object[] params, Locale locale, String defaultMessage) {
            if (defaultMessage != null) {
                return defaultMessage;
            }

            return key;
        }

        @Override
        public String translate(LocalizableMessage msg, Locale locale) {
            return msg != null ? msg.getFallbackMessage() : null;
        }

        @Override
        public <T extends CommonException> T translate(T e) {
            return e;
        }
    }
}
