package com.evolveum.midpoint.client.prism;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.ServiceFactory;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.util.PrismContextFactory;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.util.DOMUtilSettings;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RestServiceBuilder {

    public static final PrismContext DEFAULT_PRISM_CONTEXT;

    static {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(ServiceFactory.class.getClassLoader());

            DOMUtilSettings.setAddTransformerFactorySystemProperty(false);
            // todo create web client just to obtain extension schemas!

            PrismContextFactory factory = new MidPointPrismContextFactory();
            PrismContext prismContext = factory.createPrismContext();
            prismContext.initialize();

            DEFAULT_PRISM_CONTEXT = prismContext;
        } catch (Exception ex) {
            throw new IllegalStateException("Couldn't initialize prism context", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private RestServiceConfiguration configuration = new RestServiceConfiguration();

    public RestServiceBuilder url(final String url) {
        configuration.url(url);
        return this;
    }

    public RestServiceBuilder username(final String username) {
        configuration.username(username);
        return this;
    }

    public RestServiceBuilder password(final String password) {
        configuration.password(password);
        return this;
    }

    public RestServiceBuilder proxyServer(final String proxyServer) {
        configuration.proxyServer(proxyServer);
        return this;
    }

    public RestServiceBuilder proxyServerPort(final Integer proxyServerPort) {
        configuration.proxyServerPort(proxyServerPort);
        return this;
    }

    public RestServiceBuilder proxyServerType(final Proxy.Type proxyServerType) {
        configuration.proxyServerType(proxyServerType);
        return this;
    }

    public RestServiceBuilder proxyUsername(final String proxyUsername) {
        configuration.proxyUsername(proxyUsername);
        return this;
    }

    public RestServiceBuilder proxyPassword(final String proxyPassword) {
        configuration.proxyPassword(proxyPassword);
        return this;
    }

    public RestServiceBuilder messageListener(final MessageListener messageListener) {
        configuration.messageListener(messageListener);
        return this;
    }

    public RestServiceBuilder ignoreSSLErrors(final boolean ignoreSSLErrors) {
        configuration.ignoreSSLErrors(ignoreSSLErrors);
        return this;
    }

    protected OkHttpClient.Builder createBuilder() throws Exception {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (configuration.username() != null || configuration.password() != null) {
            builder.authenticator((route, response) -> {

                String credential = Credentials.basic(configuration.username(), configuration.password());
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            });
        }

        if (configuration.ignoreSSLErrors()) {
            X509TrustManager tm = new EmptyTrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, tm);
            builder.hostnameVerifier((hostname, session) -> true);
        }

        if (configuration.proxyServer() != null) {
            Proxy proxy = new Proxy(configuration.proxyServerType(),
                    new InetSocketAddress(configuration.proxyServer(), configuration.proxyServerPort()));
            builder.proxy(proxy);

            if (configuration.proxyUsername() != null || configuration.proxyPassword() != null) {
                builder.proxyAuthenticator((route, response) -> {

                    String credential = Credentials.basic(configuration.proxyUsername(), configuration.proxyPassword());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
        }

        builder.addInterceptor(chain -> {

            Request request = chain.request();
            Request newRequest;

            newRequest = request.newBuilder()
                    .addHeader("Accept", "application/xml")
                    .addHeader("Content-Type", "application/xml")
                    .build();

            return chain.proceed(newRequest);
        });

        if (configuration.messageListener() != null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    message -> configuration.messageListener().message(message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        return builder;
    }

    public Service create() throws Exception {
        OkHttpClient.Builder builder = createBuilder();

        return new RestService(new RestServiceContext(configuration, builder.build(), DEFAULT_PRISM_CONTEXT));
    }
}
