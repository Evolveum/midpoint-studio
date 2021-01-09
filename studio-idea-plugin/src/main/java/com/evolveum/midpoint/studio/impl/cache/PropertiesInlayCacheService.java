package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.*;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class PropertiesInlayCacheService {

    private Project project;

    private Expander expander;

    public PropertiesInlayCacheService(Project project) {
        this.project = project;

        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

            @Override
            public void environmentChanged(Environment oldEnv, Environment newEnv) {
                refresh(newEnv);
            }
        });

        EnvironmentService env = project.getService(EnvironmentService.class);
        refresh(env.getSelected());
    }

    private void refresh(Environment environment) {
        if (environment == null) {
            expander = null;
            return;
        }

        EncryptionService enc = EncryptionService.getInstance(project);
        expander = new Expander(environment, enc, project);

        // force re-highlight editors, this probably shouln't be here, but right now no better place
        ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
        DaemonCodeAnalyzer dca = DaemonCodeAnalyzer.getInstance(project);
        dca.restart();
    }

    public String expandKeyForInlay(String key, VirtualFile file) {
        if (expander == null) {
            return null;
        }

        if (expander.isEncrypted(key)) {
            return "Encrypted";
        }

        if (expander.isExpandingFile(key, file)) {
            return "File Content";
        }

        return expander.expandKeyFromProperties(key);
    }

    public Set<String> getKeys() {
        if (expander == null) {
            return new HashSet();
        }

        return expander.getKeys();
    }
}
