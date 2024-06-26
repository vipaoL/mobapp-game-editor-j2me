/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import mobileapplication3.editor.Main;

/**
 *
 * @author vipaol
 */
public class RootContainer extends Canvas implements IContainer {
    
    private IUIComponent rootUIComponent = null;
    private int[] currentlyPressedKeys;
    //private Thread keyRepeater = null;
    private boolean isKeyRepeaterRunning = false;
    private int keyRepeaterDelay;
    private KeyboardHelper kbHelper;

    public RootContainer(IUIComponent rootUIComponent) {
        setFullScreenMode(true);
        currentlyPressedKeys = new int[3];
//        for (int i = 0; i < currentlyPressedKeys.length; i++) {
//            currentlyPressedKeys[i] = Integer.MIN_VALUE;
//        }
        kbHelper = new KeyboardHelper();
        setRootUIComponent(rootUIComponent);
    }

    public RootContainer setRootUIComponent(IUIComponent rootUIComponent) {
        if (this.rootUIComponent != null) {
            this.rootUIComponent.setParent(null);
        }
        
        this.rootUIComponent = rootUIComponent.setParent(this).setFocused(true);
        return this;
    }

    protected void paint(Graphics g) {
        if (rootUIComponent != null) {
            rootUIComponent.paint(g);
        }
    }
    
//    private void startKeyRepeatedThread() {
//        if (isKeyRepeaterRunning) {
//            return;
//        }
//        
//        System.out.println("starting thread");
//        
//        keyRepeater = new Thread(new Runnable() {
//            public void run() {
//                isKeyRepeaterRunning = true;
//                while(isShown()) {
//                    try {
//                        Thread.sleep(keyRepeaterDelay);
//                        long start = System.currentTimeMillis();
//                        keyRepeaterDelay = 0;
//                        for (int i = 0; i < currentlyPressedKeys.length; i++) {
//                            if (currentlyPressedKeys[i] != Integer.MIN_VALUE) {
//                                handleKeyRepeated(currentlyPressedKeys[i]);
//                            }
//                        }
//
//                        long dt = System.currentTimeMillis() - start;
//                        Thread.sleep(Math.max(0, 100 - dt));
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//                isKeyRepeaterRunning = false;
//            }
//        }, "Key repeater");
//        keyRepeater.start();
//    }
    
    protected void keyPressed(int keyCode) {
        kbHelper.keyPressed(keyCode);
        
        //        for (int i = 0; i < currentlyPressedKeys.length; i++) {
//            if (currentlyPressedKeys[i] == keyCode) {
//                return;
//            }
//        }
//        
//        for (int i = 0; i < currentlyPressedKeys.length; i++) {
//            if (currentlyPressedKeys[i] == Integer.MIN_VALUE) {
//                currentlyPressedKeys[i] = keyCode;
//                break;
//            }
//        }
//        
//        keyRepeaterDelay = 100;
//        startKeyRepeatedThread();
    }
    
    private void handleKeyPressed(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyPressed(keyCode, count)) {
                repaint();
            }
        }
    }

    protected void keyReleased(int keyCode) {
        kbHelper.keyReleased(keyCode);
//        for (int i = 0; i < currentlyPressedKeys.length; i++) {
//            if (currentlyPressedKeys[i] == keyCode) {
//                currentlyPressedKeys[i] = Integer.MIN_VALUE;
//            }
//        }
    }
    
    protected void keyRepeated(int keyCode) {
        if (false) {
            handleKeyRepeated(keyCode, 1);
        }
    }
    
    protected void handleKeyRepeated(int keyCode, int pressedCount) {
        if (Main.util.getGameAction(keyCode) == Canvas.FIRE) {
            return;
        }
        if (rootUIComponent != null) {
            if (rootUIComponent.keyRepeated(keyCode, pressedCount)) {
                repaint();
            }
        }
    }
    
    protected void pointerPressed(int x, int y) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.pointerPressed(x, y)) {
                repaint();
            }
        }
    }
    
    protected void pointerDragged(int x, int y) {
        if (rootUIComponent != null) {
            if (rootUIComponent.pointerDragged(x, y)) {
                repaint();
            }
        }
    }
    
    protected void pointerReleased(int x, int y) {
        if (rootUIComponent != null) {
            if (rootUIComponent.pointerReleased(x, y)) {
                repaint();
            }
        }
    }
    
    protected void sizeChanged(int w, int h) {
        if (rootUIComponent != null) {
            rootUIComponent.setSize(w, h);
            repaint();
        }
    }

    protected void showNotify() {
        kbHelper.show();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            repaint();
        }
        sizeChanged(getWidth(), getHeight());
    }
    
    protected void hideNotify() {
        kbHelper.hide();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(false);
            repaint();
        }
    }
    
    private class KeyboardHelper {
        private Object tillPressed = new Object();
        private int lastKey, pressCount;
        private boolean pressState;
        private boolean pressedAgainInDelay;
        private Thread repeatThread;
        private long lastEvent;

        public void show() {
            pressState = false;
            pressCount = 0;
            lastKey = 0;
            pressedAgainInDelay = false;
            
            repeatThread=new Thread() {
                public void run() {
                    try {
                        while(true) {
                            if(!pressState) {
                                synchronized(tillPressed) {
                                    tillPressed.wait();
                                }
                            }
                            
                            int k = lastKey;
                            pressedAgainInDelay = false;
                            
                            Thread.sleep(200);
                            while (!isLastEventOld()) {
                                pressedAgainInDelay = false;
                                Thread.sleep(200);
                            }
                            
                            while(pressState && lastKey == k) {
                                handleKeyRepeated(k, pressCount);
                                Thread.sleep(100);
                            }
                            
                            pressCount = 0;
                        }
                    } catch (InterruptedException e) { }
                }
            };
            repeatThread.start();
        }
        
        public void hide() {
            if(repeatThread != null) {
                repeatThread.interrupt();
            }
        }

        public void keyPressed(int k) {
            if (!isLastEventOld()) {
                pressCount++;
            } else {
                pressCount = 1;
            }
            
            updateLastEventTime();
            lastKey = k;
            pressState = true;
            synchronized(tillPressed) {
                tillPressed.notify();
            }
            handleKeyPressed(k, pressCount);
        }

        public void keyReleased(int k) {
            updateLastEventTime();
            pressedAgainInDelay = true;
            if(lastKey == k) {
                pressState = false;
            } else {
                pressCount = 0;
            }
        }
        
        private boolean isLastEventOld() {
            return System.currentTimeMillis() - lastEvent > 200;
        }
        
        private void updateLastEventTime() {
            lastEvent = System.currentTimeMillis();
        }
    }
}
