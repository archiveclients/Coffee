/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.widget;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.gui.DoesMSAA;
import cf.coffee.client.feature.gui.HasSpecialCursor;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.ClipStack;
import cf.coffee.client.helper.render.Cursor;
import cf.coffee.client.helper.render.Rectangle;
import cf.coffee.client.helper.render.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class RoundTextFieldWidget implements Element, Drawable, Selectable, DoesMSAA, HasSpecialCursor {
    protected final String suggestion;
    public Runnable changeListener = () -> {
    };
    protected String text = "";
    protected boolean focused;
    protected int cursor;
    protected double textStart;
    protected int selectionStart, selectionEnd;
    boolean mouseOver = false;
    double x, y, width, height;

    public RoundTextFieldWidget(double x, double y, double width, double height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.suggestion = text;
    }

    @Override
    public boolean shouldApplyCustomCursor() {
        return mouseOver;
    }

    @Override
    public long getCursor() {
        return Cursor.TEXT_EDIT;
    }

    protected double maxTextWidth() {
        return width - pad() * 2 - 1;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int preSelectionCursor = 0;
        if (selectionStart < preSelectionCursor && preSelectionCursor == selectionEnd) {
            cursor = selectionStart;
        } else if (selectionEnd > preSelectionCursor && preSelectionCursor == selectionStart) {
            cursor = selectionEnd;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods) {
        if (!focused) {
            return false;
        }

        boolean control = MinecraftClient.IS_SYSTEM_MAC ? mods == GLFW.GLFW_MOD_SUPER : mods == GLFW.GLFW_MOD_CONTROL;

        if (control && key == GLFW.GLFW_KEY_C) {
            if (cursor != selectionStart || cursor != selectionEnd) {
                CoffeeMain.client.keyboard.setClipboard(text.substring(selectionStart, selectionEnd));
            }
            return true;
        } else if (control && key == GLFW.GLFW_KEY_X) {
            if (cursor != selectionStart || cursor != selectionEnd) {
                CoffeeMain.client.keyboard.setClipboard(text.substring(selectionStart, selectionEnd));
                clearSelection();
            }

            return true;
        } else if (control && key == GLFW.GLFW_KEY_A) {
            cursor = text.length();
            selectionStart = 0;
            selectionEnd = cursor;
        } else if (mods == ((MinecraftClient.IS_SYSTEM_MAC ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL) | GLFW.GLFW_MOD_SHIFT) && key == GLFW.GLFW_KEY_A) {
            resetSelection();
        } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            setFocused(false);
            return true;
        }

        return onKeyRepeated(key, mods);
    }

    public boolean onKeyRepeated(int key, int mods) {
        if (!focused) {
            return false;
        }

        boolean control = MinecraftClient.IS_SYSTEM_MAC ? mods == GLFW.GLFW_MOD_SUPER : mods == GLFW.GLFW_MOD_CONTROL;
        boolean shift = mods == GLFW.GLFW_MOD_SHIFT;
        int isCtrlPressed = SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_ALT : MinecraftClient.IS_SYSTEM_MAC ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
        boolean controlShift = mods == (isCtrlPressed | GLFW.GLFW_MOD_SHIFT);
        boolean altShift = mods == ((SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT) | GLFW.GLFW_MOD_SHIFT);

        if (control && key == GLFW.GLFW_KEY_V) {
            clearSelection();

            String preText = text;
            String clipboard = CoffeeMain.client.keyboard.getClipboard();
            int addedChars = 0;

            StringBuilder sb = new StringBuilder(text.length() + clipboard.length());
            sb.append(text, 0, cursor);

            for (int i = 0; i < clipboard.length(); i++) {
                char c = clipboard.charAt(i);
                sb.append(c);
                addedChars++;
            }

            sb.append(text, cursor, text.length());

            text = sb.toString();
            cursor += addedChars;
            resetSelection();

            if (!text.equals(preText)) {
                runAction();
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursor > 0 && cursor == selectionStart && cursor == selectionEnd) {
                String preText = text;

                int count = (mods == isCtrlPressed) ? cursor : (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) ? countToNextSpace(true) : 1;

                text = text.substring(0, cursor - count) + text.substring(cursor);
                cursor -= count;
                resetSelection();

                if (!text.equals(preText)) {
                    runAction();
                }
            } else if (cursor != selectionStart || cursor != selectionEnd) {
                clearSelection();
            }

            return true;
        } else {
            boolean ctrl = mods == isCtrlPressed;
            if (key == GLFW.GLFW_KEY_DELETE) {
                if (cursor < text.length()) {
                    if (cursor == selectionStart && cursor == selectionEnd) {
                        String preText = text;

                        int count = ctrl ? text.length() - cursor : (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) ? countToNextSpace(false) : 1;

                        text = text.substring(0, cursor) + text.substring(cursor + count);

                        if (!text.equals(preText)) {
                            runAction();
                        }
                    } else {
                        clearSelection();
                    }
                }

                return true;
            } else if (key == GLFW.GLFW_KEY_LEFT) {
                if (cursor > 0) {
                    if (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) {
                        cursor -= countToNextSpace(true);
                        resetSelection();
                    } else if (ctrl) {
                        cursor = 0;
                        resetSelection();
                    } else if (altShift) {
                        if (cursor == selectionEnd && cursor != selectionStart) {
                            cursor -= countToNextSpace(true);
                            selectionEnd = cursor;
                        } else {
                            cursor -= countToNextSpace(true);
                            selectionStart = cursor;
                        }
                    } else if (controlShift) {
                        if (cursor == selectionEnd && cursor != selectionStart) {
                            selectionEnd = selectionStart;
                        }
                        selectionStart = 0;

                        cursor = 0;
                    } else if (shift) {
                        if (cursor == selectionEnd && cursor != selectionStart) {
                            selectionEnd = cursor - 1;
                        } else {
                            selectionStart = cursor - 1;
                        }

                        cursor--;
                    } else {
                        if (cursor == selectionEnd && cursor != selectionStart) {
                            cursor = selectionStart;
                        } else {
                            cursor--;
                        }

                        resetSelection();
                    }

                    cursorChanged();
                } else if (selectionStart != selectionEnd && selectionStart == 0 && mods == 0) {
                    cursor = 0;
                    resetSelection();
                    cursorChanged();
                }

                return true;
            } else if (key == GLFW.GLFW_KEY_RIGHT) {
                if (cursor < text.length()) {
                    if (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) {
                        cursor += countToNextSpace(false);
                        resetSelection();
                    } else if (ctrl) {
                        cursor = text.length();
                        resetSelection();
                    } else if (altShift) {
                        if (cursor == selectionStart && cursor != selectionEnd) {
                            cursor += countToNextSpace(false);
                            selectionStart = cursor;
                        } else {
                            cursor += countToNextSpace(false);
                            selectionEnd = cursor;
                        }
                    } else if (controlShift) {
                        if (cursor == selectionStart && cursor != selectionEnd) {
                            selectionStart = selectionEnd;
                        }
                        cursor = text.length();
                        selectionEnd = cursor;
                    } else if (shift) {
                        if (cursor == selectionStart && cursor != selectionEnd) {
                            selectionStart = cursor + 1;
                        } else {
                            selectionEnd = cursor + 1;
                        }

                        cursor++;
                    } else {
                        if (cursor == selectionStart && cursor != selectionEnd) {
                            cursor = selectionEnd;
                        } else {
                            cursor++;
                        }

                        resetSelection();
                    }

                    cursorChanged();
                } else if (selectionStart != selectionEnd && selectionEnd == text.length() && mods == 0) {
                    cursor = text.length();
                    resetSelection();
                    cursorChanged();
                }

                return true;
            }
        }

        return false;
    }


    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        clearSelection();
        this.text = text;
        cursor = this.text.length();
        resetSelection();
        runAction();
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (!focused) {
            return false;
        }

        clearSelection();

        text = text.substring(0, cursor) + c + text.substring(cursor);

        cursor++;
        resetSelection();

        runAction();
        return true;
    }

    boolean inBounds(double cx, double cy) {
        return cx >= x && cx < x + width && cy >= y && cy < y + height;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        mouseOver = inBounds(mouseX, mouseY);
        //        double pad = pad();
        double pad = 2;
        double overflowWidth = getOverflowWidthForRender();
        double innerHeight = FontRenderers.getRenderer().getFontHeight();
        double centerY = y + height / 2d - innerHeight / 2d;

        //        Renderer.R2D.renderQuad(stack,Color.RED,x,y+height,x+width,y+height+.5);
        Renderer.R2D.renderRoundedQuad(stack, new Color(40, 40, 40), x, y, x + width, y + height, 5, 20);
        ClipStack.globalInstance.addWindow(stack, new Rectangle(x + pad, y, x + width - pad, y + height));
        //Renderer.R2D.beginScissor(stack, x + pad, y, x + width - pad, y + height);
        // Text content
        if (!text.isEmpty()) {
            FontRenderers.getRenderer().drawString(stack, text, (float) (x + pad - overflowWidth), (float) (centerY), 0xFFFFFF, false);
        } else {
            FontRenderers.getRenderer().drawString(stack, suggestion, (float) (x + pad - overflowWidth), (float) (centerY), 0xAAAAAA, false);
        }

        // Text highlighting
        if (focused && (cursor != selectionStart || cursor != selectionEnd)) {
            double selStart = x + pad + getTextWidth(selectionStart) - overflowWidth;
            double selEnd = x + pad + getTextWidth(selectionEnd) - overflowWidth;
            Renderer.R2D.renderQuad(stack, new Color(50, 50, 255, 100), selStart, centerY, selEnd, centerY + FontRenderers.getRenderer().getMarginHeight());
        }
        ClipStack.globalInstance.popWindow();
        //Renderer.R2D.endScissor();
        boolean renderCursor = (System.currentTimeMillis() % 1000) / 500d > 1;
        if (focused && renderCursor) {
            Renderer.R2D.renderQuad(stack, Color.WHITE, x + pad + getTextWidth(cursor) - overflowWidth, centerY, x + pad + getTextWidth(cursor) - overflowWidth + 1, centerY + FontRenderers.getRenderer().getMarginHeight());
        }

    }

    private void clearSelection() {
        if (selectionStart == selectionEnd) {
            return;
        }

        String preText = text;

        text = text.substring(0, selectionStart) + text.substring(selectionEnd);

        cursor = selectionStart;
        selectionEnd = cursor;

        if (!text.equals(preText)) {
            runAction();
        }
    }

    private void resetSelection() {
        selectionStart = cursor;
        selectionEnd = cursor;
    }

    private int countToNextSpace(boolean toLeft) {
        int count = 0;
        boolean hadNonSpace = false;

        for (int i = cursor; toLeft ? i >= 0 : i < text.length(); i += toLeft ? -1 : 1) {
            int j = i;
            if (toLeft) {
                j--;
            }

            if (j >= text.length()) {
                continue;
            }
            if (j < 0) {
                break;
            }

            if (hadNonSpace && Character.isWhitespace(text.charAt(j))) {
                break;
            } else if (!Character.isWhitespace(text.charAt(j))) {
                hadNonSpace = true;
            }

            count++;
        }

        return count;
    }

    private void runAction() {
        cursorChanged();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    private double textWidth() {
        return FontRenderers.getRenderer().getStringWidth(text);
    }

    private void cursorChanged() {
        double cursor = getCursorTextWidth(-2);
        if (cursor < textStart) {
            textStart -= textStart - cursor;
        }

        cursor = getCursorTextWidth(2);
        if (cursor > textStart + maxTextWidth()) {
            textStart += cursor - (textStart + maxTextWidth());
        }

        textStart = MathHelper.clamp(textStart, 0, Math.max(textWidth() - maxTextWidth(), 0));
    }

    protected double getTextWidth(int pos) {
        if (pos < 0) {
            return 0;
        }
        int pos1 = Math.min(text.length(), pos);
        return FontRenderers.getRenderer().getStringWidth(text.substring(0, pos1));
    }

    protected double getCursorTextWidth(int offset) {
        return getTextWidth(cursor + offset);
    }

    protected double getOverflowWidthForRender() {
        return textStart;
    }

    public String get() {
        return text;
    }

    public void set(String text) {
        this.text = text;

        cursor = MathHelper.clamp(cursor, 0, text.length());
        selectionStart = cursor;
        selectionEnd = cursor;

        cursorChanged();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {

        this.focused = focused;

        resetSelection();
    }

    public void setCursorMax() {
        cursor = text.length();
    }

    double pad() {
        return 4;
    }

    @Override
    public SelectionType getType() {
        return mouseOver ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOver) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (!text.isEmpty()) {
                    text = "";
                    cursor = 0;
                    selectionStart = 0;
                    selectionEnd = 0;

                    runAction();
                }
            }

            setFocused(true);
            return true;
        }

        if (focused) {
            setFocused(false);
        }

        return false;
    }
}
