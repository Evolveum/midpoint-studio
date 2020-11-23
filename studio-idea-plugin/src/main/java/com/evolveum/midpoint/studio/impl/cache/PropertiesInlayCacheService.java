package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.impl.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;

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
    }

    private void refresh(Environment environment) {
        if (environment == null) {
            expander = null;
            return;
        }

        EncryptionService enc = EncryptionService.getInstance(project);
        expander = new Expander(environment, enc, project);
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
}
