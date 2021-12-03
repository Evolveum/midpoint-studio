package com.evolveum.midpoint.studio.ui;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.stepsProcessing.StepLogger;
import com.intellij.remoterobot.stepsProcessing.StepWorker;
import com.intellij.remoterobot.utils.Keyboard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;


/**
 * Created by Viliam Repan (lazyman).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class StudioUiTest {

    protected RemoteRobot robot;

    protected Keyboard keyboard;

    protected RobotUtils utils;

    @BeforeAll
    public void beforeClass() {
        setupRemoteRobot();

        utils.createProject("midpoint-project-" + getClass().getSimpleName() + "-" + System.currentTimeMillis());
    }

    protected void setupRemoteRobot() {
        robot = new RemoteRobot("http://127.0.0.1:8082");
        keyboard = new Keyboard(robot);

        utils = new RobotUtils(robot);

        StepWorker.registerProcessor(new StepLogger());
    }

    @AfterAll
    public void afterClass() {
        utils.closeProject();
    }
}
