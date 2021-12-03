package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.ui.fixture.ActionMenuFixture;
import com.evolveum.midpoint.studio.ui.fixture.ActionMenuItemFixture;
import com.evolveum.midpoint.studio.ui.fixture.DialogFixture;
import com.evolveum.midpoint.studio.ui.fixture.WelcomeFrameFixture;
import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.fixtures.JCheckboxFixture;
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import com.intellij.remoterobot.search.locators.Locators;
import com.intellij.remoterobot.utils.Keyboard;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static java.awt.event.KeyEvent.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RobotUtils {

    private RemoteRobot robot;

    private Keyboard keyboard;

    public RobotUtils(RemoteRobot robot) {
        this.robot = robot;
        this.keyboard = new Keyboard(robot);
    }

    public void createProject(String projectName) {
        step("Create New MidPoint Project", () -> {
            final WelcomeFrameFixture welcomeFrame = robot.find(WelcomeFrameFixture.class, Duration.ofSeconds(1));
            welcomeFrame.createNewProjectLink().click();

            DialogFixture newProjectDialog = welcomeFrame.find(DialogFixture.class, DialogFixture.byTitle("New Project"), Duration.ofSeconds(1));
            newProjectDialog.findText("MidPoint").click();

            newProjectDialog.find(JCheckboxFixture.class, Locators.byXpath("//div[@text='Log REST communication:']/following-sibling::div[1]")).select();

            newProjectDialog.find(JTextFieldFixture.class, Locators.byXpath("//div[@text='Password:']/following-sibling::div[1]")).setText("qwe123");
            newProjectDialog.find(JTextFieldFixture.class, Locators.byXpath("//div[@text='Repeat password:']/following-sibling::div[1]")).setText("qwe123");

            newProjectDialog.button("Next").click();

            newProjectDialog.find(JTextFieldFixture.class, Locators.byXpath("//div[@accessiblename='Project name:' and @accessiblename.key='label.project.name' and @class='JTextField']"))
                    .setText(projectName);

            File path = new File("./build/tmp/projects");
            if (!path.exists()) {
                path.mkdir();
            }

            File projectPath = new File(path, projectName);
            Assertions.assertFalse(projectPath.exists());
            projectPath.mkdir();

            newProjectDialog.find(JTextFieldFixture.class, Locators.byXpath("//div[@accessiblename='Project location:' and @accessiblename.key='label.project.files.location' and @class='JTextField']"))
                    .setText(projectPath.getAbsolutePath());

            newProjectDialog.button("Finish").click();
        });
    }

    public void closeProject() {
        step("Close the project", () -> {
            if (robot.isMac()) {
                openAllSearch();

                keyboard.enterText("Close Project");
                keyboard.enter();
            } else {
                actionMenu("File").click();
                actionMenuItem("Close Project").click();
            }
        });
    }

    private void openAllSearch() {
        for (int i = 0; i < 10; i++) {
            keyboard.hotKey(VK_SHIFT, VK_META, VK_A);

            List<CommonContainerFixture> searchAll = robot.findAll(CommonContainerFixture.class, Locators.byXpath( "//div[@class='SearchEverywhereUI']"));
            if (searchAll != null && !searchAll.isEmpty()) {
                return;
            }

            try {
                Thread.sleep(2000L);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Assertions.fail("Couldn't open all search");
    }

    public ActionMenuFixture actionMenu(String text) {
        List<ActionMenuFixture> list = robot.findAll(ActionMenuFixture.class,
                Locators.byXpath("text '" + text + "'", "//div[@class='ActionMenu' and @text='" + text + "']"));
        return list.isEmpty() ? null : list.get(0);
    }

    public ActionMenuItemFixture actionMenuItem(String text) {
        List<ActionMenuItemFixture> list = robot.findAll(ActionMenuItemFixture.class,
                Locators.byXpath("text '" + text + "'", "//div[@class='ActionMenuItem' and @text='" + text + "']"));
        return list.isEmpty() ? null : list.get(0);
    }
}
