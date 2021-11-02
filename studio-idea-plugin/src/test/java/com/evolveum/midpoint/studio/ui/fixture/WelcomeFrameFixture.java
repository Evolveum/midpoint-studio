package com.evolveum.midpoint.studio.ui.fixture;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.UtilsKt.hasAnyComponent;

/**
 * Created by Viliam Repan (lazyman).
 */
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
public class WelcomeFrameFixture extends CommonContainerFixture {

    public WelcomeFrameFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public ComponentFixture createNewProjectLink() {
        return welcomeFrameLink("New Project");
    }

    public ComponentFixture importProjectLink() {
        return welcomeFrameLink("Get from VCS");
    }

    private ComponentFixture welcomeFrameLink(String text) {
        if (hasAnyComponent(this, byXpath("//div[@class='NewRecentProjectPanel']"))) {
            return find(ComponentFixture.class, byXpath("//div[@class='JBOptionButton' and @text='" + text + "']"));
        }
        return find(
                ComponentFixture.class,
                byXpath("//div[@class='NonOpaquePanel'][./div[@text='" + text + "']]//div[@class='JButton']")
        );
    }
}
