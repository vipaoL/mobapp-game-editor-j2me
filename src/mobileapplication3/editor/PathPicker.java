/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.editor;

import mobileapplication3.utils.Utils;
import mobileapplication3.utils.FileUtils;
import mobileapplication3.editor.ui.Button;
import mobileapplication3.editor.ui.ButtonRow;
import mobileapplication3.editor.ui.ButtonCol;
import java.io.IOException;
import java.util.Calendar;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import mobileapplication3.editor.ui.Container;
import mobileapplication3.editor.ui.IUIComponent;
import mobileapplication3.editor.ui.TextComponent;

/**
 *
 * @author vipaol
 */


// TODO: move list to FileManager


public class PathPicker extends Container {
    
    public static final String QUESTION_REPLACE_WITH_PATH = ".curr_folder.";
    private static final int TARGET_SAVE = 0, TARGET_OPEN = 1;
    
    private Button okBtn, cancelBtn;
    private TextComponent title = null;
    private TextComponent question = null;
    private ButtonCol list;
    private ButtonRow actionButtonPanel;
    private Button[] actionButtons;
    private Feedback feedback;
    
    private int currentTarget = TARGET_SAVE, btnH;
    private String currentFolder = null, pickedPath, fileName = "";
    private String questionTemplate = "";
    
    public PathPicker() {
        initUI();
    }
    
    public PathPicker pickFolder(String question, Feedback onComplete) {
        return pickFolder(null, question, onComplete);
    }
    
    public PathPicker pickFolder(String initialPath, String question, Feedback onComplete) {
        questionTemplate = question;
        currentTarget = TARGET_SAVE;
        currentFolder = initialPath;
        feedback = onComplete;
        initFM();
        return this;
    }
    
    public PathPicker pickFile(String question, Feedback onComplete) {
        return pickFile(null, question, onComplete);
    }
    
    public PathPicker pickFile(String initialPath, String question, Feedback onComplete) {
        questionTemplate = question;
        currentTarget = TARGET_OPEN;
        this.currentFolder = initialPath;
        feedback = onComplete;
        initFM();
        return this;
    }
    
    private void initFM() {
        question.setText("");
        if (currentTarget == TARGET_SAVE) {
            title.setText("Choose a folder");
        } else if (currentTarget == TARGET_OPEN) {
            title.setText("Choose a file");
        }
        
        if (currentTarget == TARGET_SAVE) {
            fileName = getTodaysFileName();
            System.out.println(fileName);
        }
        
        (new Thread(new Runnable() {
            public void run() {
                if (currentFolder == null) {
                    currentFolder = FileUtils.PREFIX;
                    String[] roots = FileUtils.getRoots();
                    setPaths(roots);
                } else {
                    if (currentTarget == TARGET_SAVE) {
                        pickPath(currentFolder + fileName);
                    }
                    getNewList();
                }
            }
        })).start();
    }
    
    private void getNewList() {
        title.setText(currentFolder);
        
        (new Thread(new Runnable() {
            public void run() {
                try {
                    setPaths(FileUtils.list(currentFolder));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        })).start();
    }
    
    private void pickPath(String path) {
        pickedPath = path;
        if (!fileName.equals("")) {
            question.setText(Utils.replace(questionTemplate, QUESTION_REPLACE_WITH_PATH, pickedPath));
        } else {
            question.setText("");
        }
    }
    
    private void setPaths(String[] paths) {
        Button[] listButtons = new Button[paths.length];
        for (int i = 0; i < paths.length; i++) {
            final String name = paths[i];
            listButtons[i] = new Button(name, new Button.ButtonFeedback() {
                public void buttonPressed() {
                    // folder or file
                    if (name.endsWith(String.valueOf(FileUtils.SEP))) {
                        if (currentTarget == TARGET_OPEN) {
                            fileName = "";
                        }
                        currentFolder = currentFolder + name;
                        pickPath(currentFolder + fileName);
                        getNewList();
                    } else {
                        if (currentTarget == TARGET_OPEN) {
                            fileName = name;
                        }
                        
                        pickPath(currentFolder + name);
                    }
                }
            });
        }
        setListButtons(listButtons);
    }
    
    private void setListButtons(Button[] buttons) {
        list.setButtons(buttons).setParent(this);
        setSize(w, h);
        repaint();
    }
    
    private void initUI() {
        setBgColor(COLOR_TRANSPARENT);
        title = new TextComponent();
        title.setBgColor(COLOR_TRANSPARENT);
        
        if (list == null) {
            list = (ButtonCol) new ButtonCol()
                    .enableScrolling(true, true)
                    .setIsSelectionEnabled(true)
                    .setIsSelectionVisible(!Main.hasPointerEvents)
                    .setButtonsBgPadding(3)
                    .setButtonsBgColor(0x555555)
                    .setBgColor(COLOR_TRANSPARENT);
        }
        
        okBtn = new Button("OK", new Button.ButtonFeedback() {
            public void buttonPressed() {
                if (currentTarget == TARGET_OPEN && pickedPath.endsWith(String.valueOf(FileUtils.SEP))) {
                    return;
                }
                feedback.onComplete(pickedPath);
            }
        });
        
        cancelBtn = new Button("Cancel", new Button.ButtonFeedback() {
            public void buttonPressed() {
                setVisible(false);
                feedback.onCancel();
            }
        });
        
        
        actionButtons = new Button[]{okBtn, cancelBtn};
        actionButtonPanel = (ButtonRow) new ButtonRow(actionButtons)
                .setButtonsBgColor(COLOR_TRANSPARENT)
                .setBgColor(COLOR_TRANSPARENT);
        
        question = (TextComponent) new TextComponent()
                .enableHorizontalScrolling(true)
                .setBgColor(COLOR_TRANSPARENT);
        
        setComponents(new IUIComponent[]{list, actionButtonPanel, question, title});
    }
    
    public Container setBgImage(Image bg) {
        blurImg(bg);
        return super.setBgImage(bg);
    }
    
    void blurImg(Image img) {
        Graphics g = img.getGraphics();
        int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        int a = 3;
        for (int i = 0; i < (w + h) / a; i++) {
            g.setColor(0x110033);
            g.drawLine(x1 + x0, y1 + y0, x2 + x0, y2 + y0);
            g.drawLine(x1 + x0, h - (y1 + y0), x2 + x0, h - (y2 + y0));
            
            if (y1 < h) {
                y1 += a;
            } else {
                x1 += a;
            }
            
            if (x2 < w) {
                x2 += a;
            } else {
                y2 += a;
            }
        }
    }
    
    public PathPicker setSizes(int w, int h, int btnH) {
        this.btnH = btnH;
        super.setSize(w, h);
        return this;
    }

    public boolean keyPressed(int keyCode, int count) {
        if (!isVisible) {
            return false;
        }
        
        switch (keyCode) {
            case -6:
                okBtn.invokePressed(false, false);
                break;
            case -7:
                cancelBtn.invokePressed(false, false);
                break;
            default:
                return super.keyPressed(keyCode, count);
        }
        
        return true;
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        title
                .setSize(w, TextComponent.HEIGHT_AUTO)
                .setPos(x0 + w/2, y0 + Font.getDefaultFont().getHeight(), Graphics.HCENTER | Graphics.TOP);
        actionButtonPanel
                .setSize(w, btnH)
                .setPos(x0, y0 + h, ButtonRow.BOTTOM | ButtonRow.LEFT);
        list
                .setSizes(w, h - btnH*3/2 - btnH*3/2, btnH, false)
                .setPos(x0, y0 + h - btnH*3/2, ButtonCol.LEFT | ButtonCol.BOTTOM);
        question
                .setSize(w, Font.getDefaultFont().getHeight())
                .setPos(x0 + w/2, y0 + h - btnH*5/4, TextComponent.HCENTER | TextComponent.VCENTER);
    }

    private String getTodaysFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)
                // it counts months from 0 while everything else from 1.
                + "-" + (calendar.get(Calendar.MONTH) + 1) // why? who knows...
                + "-" + calendar.get(Calendar.DAY_OF_MONTH)
                + "_" + calendar.get(Calendar.HOUR_OF_DAY)
                + "-" + calendar.get(Calendar.MINUTE)
                + "-" + calendar.get(Calendar.SECOND)
                + ".mgstruct";
    }
    
    public interface Feedback {
        void onComplete(final String path);
        void onCancel();
        void needRepaint();
    }
    
}
