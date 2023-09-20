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
    private Method updateMethod, redrawMethod, checkCollisionMethod;

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
            checkCollisionMethod = DrawingLoop.class.getDeclaredMethod("checkDrawCollisions", ArrayList.class);
            updateMethod.setAccessible(true);
            redrawMethod.setAccessible(true);
            checkCollisionMethod.setAccessible(true);
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

    @Test
    public void characterMovesToTheRightAtTheRightSpeedAfterRightKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        int startX = characterUnderTest.getX();

        platformUnderTest.getKeys().add(KeyCode.D);
        for (int i = 0; i < 2; i++) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }

        Field isMoveRight = characterUnderTest.getClass().getDeclaredField("isMoveRight");
        isMoveRight.setAccessible(true);
        assertTrue(platformUnderTest.getKeys().isPressed(KeyCode.D), "Controller: Right key pressing is acknowledged");
        assertTrue(isMoveRight.getBoolean(characterUnderTest), "Model: Character moving right state is set");
        assertTrue(characterUnderTest.getX() > startX, "View: Character is moving right");
        assertEquals(startX + 3, characterUnderTest.getX(), "View: Character is moving right at the right speed");
    }

    @Test
    public void characterShouldJumpAfterPressingTheKeyWhenCharacterIsOnTheGround() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Character characterUnderTest = characterListUnderTest.get(0);

        Field canJump = characterUnderTest.getClass().getDeclaredField("canJump");
        canJump.setAccessible(true);
        while (!canJump.getBoolean(characterUnderTest)) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            checkCollisionMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }
        int startY = characterUnderTest.getY();
        platformUnderTest.getKeys().add(KeyCode.W);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);

        Field isJumping = characterUnderTest.getClass().getDeclaredField("isJumping");
        isJumping.setAccessible(true);
        assertTrue(platformUnderTest.getKeys().isPressed(KeyCode.W), "Controller: Jump key pressing is acknowledged");
        assertTrue(isJumping.getBoolean(characterUnderTest), "Model: Character jumping state is set");
        assertTrue(characterUnderTest.getY() < startY, "View: Character is jumping");
    }


    @Test
    public void characterShouldNotJumpAfterPressingTheKeyWhenCharacterIsInTheAir() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Character characterUnderTest = characterListUnderTest.get(0);

        Field canJump = characterUnderTest.getClass().getDeclaredField("canJump");
        Field isFalling = characterUnderTest.getClass().getDeclaredField("isFalling");
        canJump.setAccessible(true);
        isFalling.setAccessible(true);

        int startY = characterUnderTest.getY();

        assertTrue(isFalling.getBoolean(characterUnderTest), "Model: Character falling state is set");

        platformUnderTest.getKeys().add(KeyCode.W);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);

        assertFalse(canJump.getBoolean(characterUnderTest), "Model: Character cannot jump");
        assertTrue(characterUnderTest.getY() > startY, "View: Character is falling");
    }

    @Test
    public void characterShouldNotMovePassTheBorder() throws InvocationTargetException, IllegalAccessException {
        Character characterUnderTest = characterListUnderTest.get(0);

        platformUnderTest.getKeys().add(KeyCode.A);

        for (int i = 0; i < 100; i++) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
            checkCollisionMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }

        assertEquals(0, characterUnderTest.getX(), "View: Character cannot move pass the left border");

        platformUnderTest.getKeys().add(KeyCode.D);

        for (int i = 0; i < 200; i++) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
            checkCollisionMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }

        assertEquals(Platform.WIDTH - characterUnderTest.getWidth(), characterUnderTest.getX(), "View: Character cannot move pass the right border");
    }

    @Test
    public void characterShouldNotMoveForwardWhenCollidedWithOtherCharacter() throws InvocationTargetException, IllegalAccessException {
        Character characterUnderTest = characterListUnderTest.get(0);
        Character anotherCharacter = new Character(100, 30, 0, 0, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP);
        characterListUnderTest.add(anotherCharacter);

        platformUnderTest.getKeys().add(KeyCode.D);
        for (int i = 0; i < 300; i++) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
            checkCollisionMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }

        assertEquals(100 - Character.CHARACTER_WIDTH - 1, characterUnderTest.getX(), "View: Character cannot move pass the left border");
    }

    @Test
    public void characterStompingShouldWorkNormally() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);

        Field isFalling = characterUnderTest.getClass().getDeclaredField("isFalling");
        isFalling.setAccessible(true);
        isFalling.setBoolean(characterUnderTest, false);

        characterUnderTest.collasped();

        Field score = characterUnderTest.getClass().getDeclaredField("score");
        score.setAccessible(true);
        assertEquals(Platform.GROUND - 5, characterUnderTest.getY(), "View: Character stomping works");
    }

    @Test
    public void characterScoreShouldIncreaseWhenStompedOn() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        Character anotherCharacter = new Character(100, 200, 0, 0, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP);
        Field x = anotherCharacter.getClass().getDeclaredField("x");
        x.setAccessible(true);
        x.set(anotherCharacter, 30);
        characterListUnderTest.add(anotherCharacter);
        for (int i = 0; i < 300; i++) {
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
            checkCollisionMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }

        assertEquals(1, characterUnderTest.getScore(), "Model: Character score increases when stomped on");
    }

}
