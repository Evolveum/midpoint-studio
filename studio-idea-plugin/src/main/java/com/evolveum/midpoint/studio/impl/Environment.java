package com.evolveum.midpoint.studio.impl;

import com.intellij.util.xmlb.annotations.Transient;
import com.evolveum.midpoint.studio.client.ProxyType;
import com.evolveum.midpoint.studio.util.Color;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Environment implements Serializable, Comparable<Environment> {

    public static final Environment DEMO = new Environment();

    public static final Environment DEFAULT = new Environment();

    static {
        DEMO.setName("Demo");
        DEMO.setUrl("https://demo.evolveum.com/midpoint");
        DEMO.setAwtColor(MidPointUtils.generateAwtColor());
        DEMO.setIgnoreSslErrors(false);
        DEMO.setUsername("administrator");
        DEMO.setPassword("IGA4ever");

        DEFAULT.setName("Default");
        DEFAULT.setUrl("http://localhost:8080/midpoint");
        DEFAULT.setAwtColor(MidPointUtils.generateAwtColor());
        DEFAULT.setIgnoreSslErrors(false);
        DEFAULT.setUsername("administrator");
        DEFAULT.setPassword("5ecr3t");
    }

    private String id;

    private String name;

    private String shortName;

    private String url;

    private String username;

    private String password;

    private boolean ignoreSslErrors;

    private Color color;

    private String proxyServerHost;

    private Integer proxyServerPort;

    private ProxyType proxyServerType = ProxyType.HTTP;

    private String proxyUsername;

    private String proxyPassword;

    private String propertiesFilePath;

    private boolean useHttp2;

    private List<String> nodeUrls;

    public Environment() {
        this(UUID.randomUUID().toString());
    }

    public Environment(String id) {
        this.id = id;
    }

    public Environment(Environment other) {
        this.id = other.id;
        this.name = other.name;
        this.shortName = other.shortName;
        this.url = other.url;
        this.username = other.username;
        this.password = other.password;
        this.ignoreSslErrors = other.ignoreSslErrors;
        this.color = other.color;
        this.proxyServerHost = other.proxyServerHost;
        this.proxyServerPort = other.proxyServerPort;
        this.proxyServerType = other.proxyServerType;
        this.proxyUsername = other.proxyUsername;
        this.proxyPassword = other.proxyPassword;
        this.propertiesFilePath = other.propertiesFilePath;
        this.nodeUrls = other.nodeUrls;
        this.useHttp2 = other.useHttp2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Transient
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Transient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAwtColor(java.awt.Color color) {
        this.color = color == null ? null : new Color(color);
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public void setPropertiesFilePath(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }

    @Transient
    public java.awt.Color getAwtColor() {
        if (color == null) {
            return null;
        }

        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getProxyServerHost() {
        return proxyServerHost;
    }

    public void setProxyServerHost(String proxyServerHost) {
        this.proxyServerHost = proxyServerHost;
    }

    public Integer getProxyServerPort() {
        return proxyServerPort;
    }

    public void setProxyServerPort(Integer proxyServerPort) {
        this.proxyServerPort = proxyServerPort;
    }

    public ProxyType getProxyServerType() {
        return proxyServerType;
    }

    public void setProxyServerType(ProxyType proxyServerType) {
        this.proxyServerType = proxyServerType;
    }

    @Transient
    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    @Transient
    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public List<String> getNodeUrls() {
        return nodeUrls;
    }

    public void setNodeUrls(List<String> nodeUrls) {
        this.nodeUrls = nodeUrls;
    }

    public boolean isUseHttp2() {
        return useHttp2;
    }

    public void setUseHttp2(boolean useHttp2) {
        this.useHttp2 = useHttp2;
    }

    @Override
    public int compareTo(@NotNull Environment o) {
        return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        if (ignoreSslErrors != that.ignoreSslErrors) return false;
        if (useHttp2 != that.useHttp2) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (proxyServerHost != null ? !proxyServerHost.equals(that.proxyServerHost) : that.proxyServerHost != null)
            return false;
        if (proxyServerPort != null ? !proxyServerPort.equals(that.proxyServerPort) : that.proxyServerPort != null)
            return false;
        if (proxyServerType != that.proxyServerType) return false;
        if (proxyUsername != null ? !proxyUsername.equals(that.proxyUsername) : that.proxyUsername != null)
            return false;
        if (proxyPassword != null ? !proxyPassword.equals(that.proxyPassword) : that.proxyPassword != null)
            return false;
        if (propertiesFilePath != null ? !propertiesFilePath.equals(that.propertiesFilePath) : that.propertiesFilePath != null)
            return false;
        return nodeUrls != null ? nodeUrls.equals(that.nodeUrls) : that.nodeUrls == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (ignoreSslErrors ? 1 : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (proxyServerHost != null ? proxyServerHost.hashCode() : 0);
        result = 31 * result + (proxyServerPort != null ? proxyServerPort.hashCode() : 0);
        result = 31 * result + (proxyServerType != null ? proxyServerType.hashCode() : 0);
        result = 31 * result + (proxyUsername != null ? proxyUsername.hashCode() : 0);
        result = 31 * result + (proxyPassword != null ? proxyPassword.hashCode() : 0);
        result = 31 * result + (propertiesFilePath != null ? propertiesFilePath.hashCode() : 0);
        result = 31 * result + (useHttp2 ? 1 : 0);
        result = 31 * result + (nodeUrls != null ? nodeUrls.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Environment{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Environment copy() {
        return new Environment(this);
    }
}
