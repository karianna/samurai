/*
 * Copyright 2003-2021 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.cafebabe.samurai.swing;

import one.cafebabe.samurai.util.GUIResourceBundle;
import one.cafebabe.samurai.util.ImageLoader;
import one.cafebabe.samurai.util.OSDetector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TileTabPanel<T extends JComponent> extends JPanel implements MouseListener, MouseMotionListener {

    private static final GUIResourceBundle resources = GUIResourceBundle.getInstance();
    final JPopupMenu popupMenu = new JPopupMenu();
    private final JMenuItem menuCloseTab = new JMenuItem(resources.getMessage("TileTabPanel.closeTab"));
    private final JMenuItem menuTab = new JMenuItem(resources.getMessage("TileTabPanel.tab"));
    private final JMenuItem menuHorizontal = new JMenuItem(resources.getMessage("TileTabPanel.splitHorizontal"));
    private final JMenuItem menuVertical = new JMenuItem(resources.getMessage("TileTabPanel.splitVertical"));


    private static final ImageIcon closeIcon;
    private static final ImageIcon closePushedIcon;
    private static final ImageIcon closeHoverIcon;
    private static final ImageIcon nullIcon = new ImageIcon(Objects.requireNonNull(TileTabPanel.class.getResource(
            "null.gif")));

    static {
        if (OSDetector.isMac()) {
            closeIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/close.gif");
            closeHoverIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/close_hover.gif");
            closePushedIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/close_push.gif");
        } else {
            closeIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/winclose.gif");
            closeHoverIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/winclose_hover.gif");
            closePushedIcon = ImageLoader.get("/one/cafebabe/samurai/swing/images/winclose_push.gif");
        }
    }

    private boolean closable = false;
    private CloseListener closeListener = null;

    public void enableClosable(CloseListener listener) {
        if (!closable) {
            popupMenu.insert(new JSeparator(), 0);
            popupMenu.insert(menuCloseTab, 0);
        }
        closeListener = listener;
        this.closable = true;
    }

    public void disableClosable() {
        if (closable) {
            popupMenu.remove(0);
            popupMenu.remove(0);
        }
        closeListener = null;
        this.closable = false;
    }

    public boolean isClosable() {
        return this.closable;
    }

    private final List<ComponentInfo<T>> components = new ArrayList<>(3);

    private final TilePanel tilePanel;
    final JTabbedPane tab = new JTabbedPane();
    BorderLayout borderLayout1;
    private TileTabLayout layout = TileTabLayout.TAB;
    private boolean showTitleWithSingleComponent = true;

    public void setShowTitleWithSingleComponent(boolean show) {
        this.showTitleWithSingleComponent = show;
    }

    public TileTabPanel() {
        this(false);
    }

    public TileTabPanel(boolean supportsFocusable) {

        tilePanel = new TilePanel(supportsFocusable);
        tab.setForeground(Color.black);

        setDoubleBuffered(true);
        borderLayout1 = new BorderLayout();
        setLayout(borderLayout1);
        tab.addMouseListener(this);
        tab.addMouseMotionListener(this);
        tilePanel.addMouseListnerToTitles(this);
        tilePanel.addMouseMotionListnerToTitles(this);
        menuCloseTab.addActionListener(closeAction);
        menuTab.setEnabled(false);
        menuTab.addActionListener(e -> setOrientation(TileTabLayout.TAB));
        menuHorizontal.addActionListener(e -> setOrientation(TileTabLayout.HORIZONTAL));
        menuVertical.addActionListener(e -> setOrientation(TileTabLayout.VERTICAL));

        popupMenu.add(menuTab);
        popupMenu.add(menuHorizontal);
        popupMenu.add(menuVertical);

    }

    public void setForegroundAt(int index, Color color) {
        if (layout == TileTabLayout.TAB) {
            tab.setForegroundAt(index, color);
        } else {
            tilePanel.setForegroundAt(index, color);
        }
    }

    public Color getForegroundAt(int index) {
        if (layout == TileTabLayout.TAB) {
            return tab.getForegroundAt(index);
        } else {
            return tilePanel.getForegroundAt(index);
        }
    }

    private final CloseAction closeAction = new CloseAction(-1);

    class CloseAction implements ActionListener {
        private int index;

        CloseAction(int index) {
            this.index = index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            closeListener.closePushed(index);
        }
    }

    public int getComponentSize() {
        return components.size();
    }

    public void addComponent(String name, T component) {
        addComponent(name, component, null);
    }

    public void addComponent(String name, T component, ImageIcon icon) {
        ComponentInfo<T> componentInfo = new ComponentInfo<>(component, name, icon);
        if (null == icon && closable) {
            componentInfo.setIcon(nullIcon);
        }
        this.components.add(componentInfo);
        switch (this.components.size()) {
            case 1:
                if (!this.showTitleWithSingleComponent) {
                    add(this.components.get(0).getComponent(), BorderLayout.CENTER);
                    validate();
                } else {
                    if (this.layout == TileTabLayout.TAB) {
                        add(tab, BorderLayout.CENTER);
                        validate();
                    } else {
                        add(tilePanel, BorderLayout.CENTER);
                        validate();
                    }
                }
                break;
            case 2:
                if (!this.showTitleWithSingleComponent) {
                    remove(components.get(0).getComponent());
                    if (this.layout == TileTabLayout.TAB) {
                        remove(components.get(0).getComponent());
                        add(tab, BorderLayout.CENTER);
                        tab.addTab(components.get(0).getName(), components.get(0).getIcon(),
                                components.get(0).getComponent());
                        validate();
                    } else {
                        remove(components.get(0).getComponent());
                        add(tilePanel, BorderLayout.CENTER);
                        validate();
                        tilePanel.addComponent(components.get(0).getName(), components.get(0).getIcon(),
                                components.get(0).getComponent());
                    }
                }
                break;
            default:
        }

        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (this.layout == TileTabLayout.TAB) {
                tab.add(name, component);
                tab.setIconAt(tab.getTabCount() - 1, componentInfo.getIcon());
                this.currentSelectedIndex = tab.getSelectedIndex();
            } else {
                tilePanel.addComponent(name, component);
                tilePanel.setLeftIconAt(tilePanel.getTileCount() - 1, componentInfo.getIcon());
                this.currentSelectedIndex = tilePanel.getSelectedIndex();
            }
        }
    }

    public void removeComponent(T component) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).getComponent() == component) {
                removeComponentAt(i);
                break;
            }
        }
    }

    public void removeComponentAt(int index) {
        T component = getComponentAt(index);
        if (component instanceof RemoveListener) {
            ((RemoveListener) component).willBeRemoved();
        }
        components.remove(index);
        if (this.layout == TileTabLayout.TAB) {
            tab.remove(component);
            this.currentSelectedIndex = tab.getSelectedIndex();
        } else {
            this.tilePanel.removeComponent(component);
            this.currentSelectedIndex = tilePanel.getSelectedIndex();
        }
        if (!this.showTitleWithSingleComponent && components.size() == 1) {
            if (this.layout == TileTabLayout.TAB) {
                tab.removeTabAt(0);
                this.remove(tab);
            } else {
                tilePanel.removeTileAt(0);
                this.remove(tilePanel);
            }
            this.add(components.get(0).getComponent(), BorderLayout.CENTER);
        }
        if (components.size() == 0) {
            if (!this.showTitleWithSingleComponent) {
                this.remove(component);
            } else if (layout == TileTabLayout.TAB) {
                this.remove(tab);
            } else {
                this.remove(tilePanel);
            }
        }
        validate();
        repaint();
    }

    public void setIconAt(int index, ImageIcon icon) {
        components.get(index).setIcon(icon);
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (this.layout == TileTabLayout.TAB) {
                this.tab.setIconAt(index, icon);
            } else {
                this.tilePanel.setLeftIconAt(index, icon);
            }
        }
    }

    public void setTitleAt(int index, String text) {
        components.get(index).setName(text);
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (this.layout == TileTabLayout.TAB) {
                this.tab.setTitleAt(index, text);
            } else {
                this.tilePanel.setTitleAt(index, text);
            }
        }
    }

    public void setComponentAt(int index, T component) {
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (this.layout == TileTabLayout.TAB) {
                this.tab.setComponentAt(index, component);
            } else {
                this.tilePanel.setComponentAt(index, component);
            }
        } else {
            this.remove(components.get(index).getComponent());
            this.add(component, BorderLayout.CENTER);
        }
        components.get(index).setComponent(component);
    }

    public void setSelectedIndex(int index) {
        this.currentSelectedIndex = index;
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (TileTabLayout.TAB == layout) {
                tab.setSelectedIndex(index);
            } else {
                tilePanel.setSelectedIndex(index);
            }
            selectedIndexChanged(index);
        } else {
            // do nothing
        }
    }

    protected void selectedIndexChanged(int index) {
    }

    public int getSelectedIndex() {
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (TileTabLayout.TAB == layout) {
                return tab.getSelectedIndex();
            } else {
                return tilePanel.getSelectedIndex();
            }
        } else {
            return 0;
        }
    }

    public T getSelectedComponent() {
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (TileTabLayout.TAB == layout) {
                return (T) tab.getSelectedComponent();
            } else {
                return (T) tilePanel.getSelectedComponent();
            }
        } else {
            return components.get(0).getComponent();
        }
    }

    public T getComponentAt(int index) {
        if (showTitleWithSingleComponent || 1 < components.size()) {
            if (TileTabLayout.TAB == layout) {
                return (T) tab.getComponentAt(index);
            } else {
                return (T) tilePanel.getComponentAt(index);
            }
        } else {
            return components.get(index).getComponent();
        }
    }


    private int currentSelectedIndex = 0;

    public ImageIcon getIconAt(int index) {
        return components.get(index).getIcon();
    }

    public void setOrientation(TileTabLayout orientation) {
        if (orientation == TileTabLayout.HORIZONTAL) {
            tilePanel.setOrientation(TileTabLayout.HORIZONTAL);
        } else {
            tilePanel.setOrientation(TileTabLayout.VERTICAL);
        }
        this.currentSelectedIndex = getSelectedIndex();
        if (this.layout != orientation) {
            switch (this.components.size()) {
                case 0:
                    break;
                case 1:
                    if (!this.showTitleWithSingleComponent) {
                        break;
                    }
                default:
                    if (TileTabLayout.TAB == orientation) {
                        remove(tilePanel);
                        add(tab, BorderLayout.CENTER);
                        tab.removeAll();
                        for (ComponentInfo<T> component : components) {
                            tab.addTab(component.getName(), component.getIcon(),
                                    component.getComponent());
                        }
                        tab.setSelectedIndex(this.currentSelectedIndex);
                        validate();
                    } else {
                        //layout is tile
                        if (TileTabLayout.TAB == layout) {
                            // layout used to be TileTabLayout.TAB
                            remove(tab);
                            add(tilePanel, BorderLayout.CENTER);
                            tilePanel.removeAll();
                            validate();
                            for (ComponentInfo<T> component : components) {
                                tilePanel.addComponent(component.getName(), component.getIcon(),
                                        component.getComponent());
                            }
                            tilePanel.setSelectedIndex(this.currentSelectedIndex);
                        }
//                        if (orientation == TILE_HORIZONTAL) {
//                            tilePanel.setOrientation(TilePanel.HORIZONTAL);
//                        } else {
//                            tilePanel.setOrientation(TilePanel.VERTICAL);
//                        }
                        validate();
                    }
            }
            this.layout = orientation;
            setMenuAvailability();
        }
    }

    private void setMenuAvailability() {
        menuTab.setEnabled(layout != TileTabLayout.TAB);
        menuVertical.setEnabled(layout != TileTabLayout.VERTICAL);
        menuHorizontal.setEnabled(layout != TileTabLayout.HORIZONTAL);
    }

    public void setDividerSize(int size) {
        this.tilePanel.setDeviderSize(size);
    }

    public int indexAtLocation(int x, int y) {
        if (layout == TileTabLayout.TAB) {
            return tab.indexAtLocation(x, y);
        } else {
            return -1;
        }
    }

    private int indexAtLocation(MouseEvent event) {
        if (layout == TileTabLayout.TAB) {
            return tab.indexAtLocation(event.getX(), event.getY());
        } else {
            return tilePanel.indexAtLocation(event);
        }
    }

    public String getTitleAt(int index) {
        return components.get(index).getName();
    }

    public void mouseClicked(MouseEvent event) {
        handlePopup(event);
    }

    private void handlePopup(MouseEvent event) {
        if (event.isPopupTrigger()) {
            event.consume();
            int clickedTabIndex = this.indexAtLocation(event);
            if (clickedTabIndex != -1) {
                closeAction.setIndex(clickedTabIndex);
                menuCloseTab.setText(resources.getMessage("TileTabPanel.closeTab", this.getTitleAt(clickedTabIndex)));
            }
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    private int draggingPaneIndex = -1;

    boolean pressedOnButton = false;
    int pressedIndex = -1;

    /**
     * method for MouseMotionListener
     *
     * @param event MouseEvent
     */
    public void mousePressed(MouseEvent event) {
        this.draggingPaneIndex = indexAtLocation(event);
        if (isClosable()) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                pressedOnButton = isOnButton(draggingPaneIndex, event);
                pressedIndex = indexAtLocation(event);
                if (-1 != pressedIndex) {
                    if (pressedOnButton) {
                        event.consume();
                        if (layout == TileTabLayout.TAB) {
                            tab.setIconAt(draggingPaneIndex, isOnButton(draggingPaneIndex, event) ? closePushedIcon : closeIcon);
                        } else {
                            if (OSDetector.isMac()) {
                                this.tilePanel.setLeftIconAt(draggingPaneIndex, isOnButton(draggingPaneIndex, event) ? closePushedIcon : closeIcon);
                            } else {
                                this.tilePanel.setRightIconAt(draggingPaneIndex, isOnButton(draggingPaneIndex, event) ? closePushedIcon : closeIcon);
                            }
                        }
                    }
                }
            }
        }
        if (!event.isConsumed()) {
            handlePopup(event);
        }
    }

    public void mouseDragged(MouseEvent event) {
        if (draggingPaneIndex == -1) {
            return;
        } else {
            if (isClosable()) {
                if (0 != (event.getButton() & MouseEvent.BUTTON1)) {
                    if (-1 == indexAtLocation(event)) {
                        restoreIcon();
                    } else if (layout == TileTabLayout.TAB) {
                        tab.setIconAt(draggingPaneIndex, pressedOnButton & isOnButton(pressedIndex, event) ? closePushedIcon : closeIcon);
                    } else {
                        if (OSDetector.isMac()) {
                            tilePanel.setLeftIconAt(draggingPaneIndex, pressedOnButton & isOnButton(pressedIndex, event) ? closePushedIcon : closeIcon);
                        } else {
                            tilePanel.setRightIconAt(draggingPaneIndex, pressedOnButton & isOnButton(pressedIndex, event) ? closePushedIcon : closeIcon);
                        }
                    }
                }
            }
        }
        int targetTabIndex = indexAtLocation(event);
        if (!pressedOnButton) {
            movePane(targetTabIndex);
        }
    }

    /**
     * method for MouseMotionListener
     *
     * @param event MouseEvent
     */
    public void mouseReleased(MouseEvent event) {
        if (isClosable()) {
            if ((0 != (event.getButton() & MouseEvent.BUTTON1)) && draggingPaneIndex == indexAtLocation(event) && isOnButton(pressedIndex, event) && pressedOnButton) {
                restoreIcon();
                closeListener.closePushed(indexAtLocation(event));
                event.consume();
            } else {
                if (-1 == indexAtLocation(event)) {
                    restoreIcon();
                } else {
                    setCloseIcon(event);
                }
            }
        }
        if (!event.isConsumed()) {
            handlePopup(event);
        }
        draggingPaneIndex = -1;
        pressedIndex = -1;
        pressedOnButton = false;
    }

    public void mouseEntered(MouseEvent event) {
        if (isClosable()) {
            setCloseIcon(event);
        }
    }

    public void mouseExited(MouseEvent mouseevent) {
        if (isClosable()) {
            restoreIcon();
        }
    }

    /**
     * method for MouseMotionListener
     *
     * @param event MouseEvent
     */
    public void mouseMoved(MouseEvent event) {
        if (isClosable()) {
            setCloseIcon(event);
        }
    }

    private boolean isOnButton(int index, MouseEvent event) {
        int delta;
        int currentIndex = indexAtLocation(event);
        if (-1 == currentIndex || currentIndex != index) {
            return false;
        }
        if (layout == TileTabLayout.TAB) {
            delta = event.getX() - (int) tab.getBoundsAt(currentIndex).getX() - 12;
            return 0 <= delta && delta < 18;
        } else {
            return tilePanel.isOnButton(index, event);
        }
    }

    int lastEnteredIndex = -1;

    private void setCloseIcon(MouseEvent event) {
        int index = indexAtLocation(event);
        if (index != lastEnteredIndex && -1 != lastEnteredIndex) {
            restoreIcon();
        } else {
            if (index >= 0) {
                if (layout == TileTabLayout.TAB) {
                    tab.setIconAt(index, isOnButton(index, event) ? closeHoverIcon : closeIcon);
                } else {
                    if (OSDetector.isMac()) {
                        tilePanel.setLeftIconAt(index, isOnButton(index, event) ? closeHoverIcon : closeIcon);
                    } else {
                        tilePanel.setRightIconAt(index, isOnButton(index, event) ? closeHoverIcon : closeIcon);
                    }
                }
            }
        }
        lastEnteredIndex = index;
    }

    private void restoreIcon() {
        if (lastEnteredIndex >= 0 && lastEnteredIndex < components.size()) {
            if (layout == TileTabLayout.TAB) {
                tab.setIconAt(lastEnteredIndex, getIconAt(lastEnteredIndex));
            } else {
                if (OSDetector.isMac()) {
                    tilePanel.setLeftIconAt(lastEnteredIndex, getIconAt(lastEnteredIndex));
                } else {
                    tilePanel.setRightIconAt(lastEnteredIndex, nullIcon);
                }
            }
        }
    }

    private void movePane(int destination) {
        if (destination != -1 && destination != draggingPaneIndex) {
            boolean isForwardDrag = destination > draggingPaneIndex;
            tab.insertTab(tab.getTitleAt(draggingPaneIndex),
                    tab.getIconAt(draggingPaneIndex),
                    tab.getComponentAt(draggingPaneIndex),
                    tab.getToolTipTextAt(draggingPaneIndex),
                    isForwardDrag ? destination + 1 : destination);
            draggingPaneIndex = destination;
            tab.setSelectedIndex(draggingPaneIndex);
            ComponentInfo<T> ci1 = components.get(draggingPaneIndex);
            ComponentInfo<T> ci2 = components.get(destination);
            components.set(draggingPaneIndex, ci2);
            components.set(destination, ci1);
        }
    }
}

class ComponentInfo<T2 extends Component> {
    private T2 component;
    private String name;
    private ImageIcon icon;
    private double width;
    private double height;

    ComponentInfo(T2 component, String name, ImageIcon icon) {
        this.component = component;
        this.name = name;
        this.icon = icon;
    }

    public T2 getComponent() {
        return this.component;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public ImageIcon getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComponent(T2 component) {
        this.component = component;
    }

    public void setWidth(double size) {
        this.width = size;
    }

    public void setHeight(double size) {
        this.height = size;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }
}
