package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.client.ProxyType;
import com.evolveum.midpoint.studio.util.Color;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Environment implements Serializable, Comparable<Environment> {

    public static final Environment DEFAULT = new Environment();

    static {
        DEFAULT.setName("Default");
        DEFAULT.setUrl("http://localhost:8080/midpoint");
        DEFAULT.setAwtColor(MidPointUtils.generateAwtColor());
        DEFAULT.setIgnoreSslErrors(false);
        DEFAULT.setUsername("administrator");
        DEFAULT.setPassword("5ecr3t");
    }

    private EnvironmentState environment = new EnvironmentState();

    private String username;

    private String password;

    private String proxyUsername;

    private String proxyPassword;

    public Environment() {
        this(UUID.randomUUID().toString());
    }

    public Environment(String id) {
        environment.setId(id);
    }

    public Environment(Environment other) {
        this.environment = other.environment.copy();

        this.username = other.username;
        this.password = other.password;
        this.proxyUsername = other.proxyUsername;
        this.proxyPassword = other.proxyPassword;
    }

    public String getId() {
        return environment.getId();
    }

    public void setId(String id) {
        environment.setId(id);
    }

    public String getName() {
        return environment.getName();
    }

    public void setName(String name) {
        environment.setName(name);
    }

    public String getUrl() {
        return environment.getUrl();
    }

    public void setUrl(String url) {
        environment.setUrl(url);
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
        return environment.getColor();
    }

    public void setColor(Color color) {
        environment.setColor(color);
    }

    public void setAwtColor(java.awt.Color color) {
        environment.setAwtColor(color);
    }

    public String getPropertiesFilePath() {
        return environment.getPropertiesFilePath();
    }

    public void setPropertiesFilePath(String propertiesFilePath) {
        environment.setPropertiesFilePath(propertiesFilePath);
    }

    @Transient
    public java.awt.Color getAwtColor() {
        Color color = environment.getColor();
        return color != null ? new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()) : null;
    }

    public boolean isIgnoreSslErrors() {
        return environment.isIgnoreSslErrors();
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        environment.setIgnoreSslErrors(ignoreSslErrors);
    }

    public String getShortName() {
        return environment.getShortName();
    }

    public void setShortName(String shortName) {
        environment.setShortName(shortName);
    }

    public String getProxyServerHost() {
        return environment.getProxyServerHost();
    }

    public void setProxyServerHost(String proxyServerHost) {
        environment.setProxyServerHost(proxyServerHost);
    }

    public Integer getProxyServerPort() {
        return environment.getProxyServerPort();
    }

    public void setProxyServerPort(Integer proxyServerPort) {
        environment.setProxyServerPort(proxyServerPort);
    }

    public ProxyType getProxyServerType() {
        return environment.getProxyServerType();
    }

    public void setProxyServerType(ProxyType proxyServerType) {
        environment.setProxyServerType(proxyServerType);
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
        return environment.getNodeUrls();
    }

    public void setNodeUrls(List<String> nodeUrls) {
        environment.setNodeUrls(nodeUrls);
    }

    public boolean isUseHttp2() {
        return environment.isUseHttp2();
    }

    public void setUseHttp2(boolean useHttp2) {
        environment.setUseHttp2(useHttp2);
    }

    @Override
    public int compareTo(@NotNull Environment o) {
        return String.CASE_INSENSITIVE_ORDER.compare(environment.getName(), o.environment.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        if (!Objects.equals(environment, that.environment)) return false;
        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(password, that.password)) return false;
        if (!Objects.equals(proxyUsername, that.proxyUsername))
            return false;
        return Objects.equals(proxyPassword, that.proxyPassword);
    }

    @Override
    public int hashCode() {
        int result = environment != null ? environment.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxyUsername != null ? proxyUsername.hashCode() : 0);
        result = 31 * result + (proxyPassword != null ? proxyPassword.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Environment{");
        sb.append("id='").append(environment.getId()).append('\'');
        sb.append(", name='").append(environment.getName()).append('\'');
        sb.append(", url='").append(environment.getUrl()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
