package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.item.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomChestField<ItemType extends VarItem> extends ClickableWidget {
    private final List<Drawable> widgets;
    public ItemType item;

    public CustomChestField(TextRenderer textRender, int x, int y, int width, int height, Text message, ItemType item) {
        super(x, y, width, height, message);
        this.item = item;
        var widgets = new ArrayList<Drawable>();
        if(item instanceof NamedItem named) {
            int textboxWidth = width;
            if(item instanceof Variable) {
                textboxWidth = textboxWidth - height;
                widgets.add(new CheckboxWidget(x+textboxWidth,y,height,height,Text.literal(""),false));
            }
            var text = new TextFieldWidget(textRender,x,y,textboxWidth,height,Text.literal(""));
            text.setMaxLength(10000);
            text.setText(named.getName());
            text.setFocused(false);
            widgets.add(0,text);
        }
        if(item instanceof Vector vec) {
            Double[] values = {vec.getX(), vec.getY(), vec.getZ()};
            int textboxWidth = width / 3;
            for (int i = 0; i < 3; i++) {
                var field = new NumberFieldWidget(textRender,x + (textboxWidth * i),y,textboxWidth,height,Text.literal(""));
                field.setMaxLength(10);
                field.setNumber(values[i]);
                widgets.add(field);
            }
        }
        if(item instanceof Location loc) {
            Double[] values = {loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw()};
            int textboxWidth = width / 5;
            for (int i = 0; i < 5; i++) {
                var field = new NumberFieldWidget(textRender,x + (textboxWidth * i),y,textboxWidth,height,Text.literal(""));
                field.setMaxLength(10);
                field.setNumber(values[i]);
                field.setCursorToStart(false);
                widgets.add(field);
            }
        }
        this.widgets = widgets;
    }

    private void updateItem() {
        if(item instanceof NamedItem named) {
            if(widgets.get(0) instanceof TextFieldWidget text) {
                named.setName(text.getText());
            }
            if(named instanceof Variable variable) {
                CodeClient.LOGGER.info(variable.getScope().longName); // Problem for later
            }
        }
        if(item instanceof Vector vec) {
            if (widgets.get(0) instanceof NumberFieldWidget num1
                    && widgets.get(1) instanceof NumberFieldWidget num2
                    && widgets.get(2) instanceof NumberFieldWidget num3)
                vec.setCoords(num1.getNumber(), num2.getNumber(), num3.getNumber());
            this.item = (ItemType) vec;
        }
        if(item instanceof Location loc) {
            if(
                        widgets.get(0) instanceof NumberFieldWidget num0
                    && widgets.get(1) instanceof NumberFieldWidget num1
                    && widgets.get(2) instanceof NumberFieldWidget num2
                    && widgets.get(3) instanceof NumberFieldWidget num3
                    && widgets.get(4) instanceof NumberFieldWidget num4
            ) {
                loc.setCoords(
                        num0.getNumber(),
                        num1.getNumber(),
                        num2.getNumber(),
                        num3.getNumber(),
                        num4.getNumber()
                );
                this.item = (ItemType) loc;
            }
        }
    }

    @Override
    public void setFocused(boolean focused) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget click) click.setFocused(focused);
        }
        super.setFocused(focused);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        for (var widget: widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClick(mouseX,mouseY);
        return true;
//        return super.mouseClicked(mouseX, mouseY, button);
    }




    @Override
    public void onClick(double mouseX, double mouseY) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget clickable) {
                if(clickable.isMouseOver(mouseX,mouseY)) {
                    clickable.setFocused(true);
                    clickable.onClick(mouseX, mouseY);
                }
                else clickable.setFocused(false);
            }
        }
        updateItem();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.USAGE, "Value Editor");
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget click && click.isMouseOver(mouseX,mouseY) && click.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount)) {
                updateItem();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget click && click.isFocused()) {
                updateItem();
                return click.keyPressed(keyCode,scanCode,modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget click && click.isFocused()) {
                updateItem();
                return click.keyReleased(keyCode,scanCode,modifiers);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget click && click.isFocused()) {
                updateItem();
                return click.charTyped(chr,modifiers);
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Nullable
    @Override
    public GuiNavigationPath getFocusedPath() {
        return super.getFocusedPath();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }
}
