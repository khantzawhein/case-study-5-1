package com.se233.chapter5;

import com.se233.chapter5.controller.DrawingLoop;
import com.se233.chapter5.controller.GameLoop;
import com.se233.chapter5.model.Character;
import com.se233.chapter5.view.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {
    private Character floatingCharacter;
    private ArrayList<Character> characterListUnderTest;
    private Platform platformUnderTest;
    private GameLoop gameLoopUnderTest;
    private DrawingLoop drawingLoopUnderTest;
    private Method updateMethod, redrawMethod;

    @BeforeEach
    public void setup() {
        JFXPanel jfxPanel = new JFXPanel();
        floatingCharacter = new Character(30, 30, 0, 0, KeyCode.A, KeyCode.D, KeyCode.W);
        characterListUnderTest = new ArrayList<>();
        characterListUnderTest.add(floatingCharacter);
        platformUnderTest = new Platform();
        gameLoopUnderTest = new GameLoop(platformUnderTest);
        drawingLoopUnderTest = new DrawingLoop(platformUnderTest);
        try {
            updateMethod = GameLoop.class.getDeclaredMethod("update", ArrayList.class);
            redrawMethod = DrawingLoop.class.getDeclaredMethod("paint", ArrayList.class);
            updateMethod.setAccessible(true);
            redrawMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            updateMethod = redrawMethod = null;
        }
    }

    @Test
    public void characterInitialValuesShouldMatchConstructorArguments() {
        assertEquals(30, floatingCharacter.getX(), "Initial X");
        assertEquals(30, floatingCharacter.getY(), "Initial Y");
        assertEquals(0, floatingCharacter.getOffsetX(), "Offset X");
        assertEquals(0, floatingCharacter.getOffsetY(), "Offset Y");
        assertEquals(KeyCode.A, floatingCharacter.getLeftKey(), "Left Key");
        assertEquals(KeyCode.D, floatingCharacter.getRightKey(), "Right Key");
        assertEquals(KeyCode.W, floatingCharacter.getUpKey(), "Up Key");
    }

    @Test
    public void characterShouldMoveToTheLeftAfterTheLeftKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        int startX = characterUnderTest.getX();
        platformUnderTest.getKeys().add(KeyCode.A);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isMoveLeft = characterUnderTest.getClass().getDeclaredField("isMoveLeft");
        isMoveLeft.setAccessible(true);
        assertTrue(platformUnderTest.getKeys().isPressed(KeyCode.A), "Controller: Left key pressing is acknowledged");
        assertTrue(isMoveLeft.getBoolean(characterUnderTest), "Model: Character moving left state is set");
        assertTrue(characterUnderTest.getX() < startX, "View: Character is moving left");

    }
}
