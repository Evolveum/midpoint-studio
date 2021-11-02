package com.evolveum.midpoint.studio.ui.fixture;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.search.locators.Locator;
import com.intellij.remoterobot.search.locators.Locators;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
@FixtureName(name = "Dialog")
public class DialogFixture extends CommonContainerFixture {

    public DialogFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static Locator byTitle(String title) {
        return Locators.byXpath("title " + title, "//div[@title='" + title + "' and @class='MyDialog']");
    }
}
