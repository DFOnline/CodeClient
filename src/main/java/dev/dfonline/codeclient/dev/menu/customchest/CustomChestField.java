package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.Target;
import dev.dfonline.codeclient.hypercube.item.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomChestField<ItemType extends VarItem> extends ClickableWidget {
    private final List<Drawable> widgets;
    public ItemType item;

    public CustomChestField(TextRenderer textRender, int x, int y, int width, int height, Text message, ItemStack stack, ItemType item, ScreenHandler handler) {
        super(x, y, width, height, message);
        this.item = item;
        var widgets = new ArrayList<Drawable>();
        if(item instanceof NamedItem named) {
            int textboxWidth = width;
            if(item instanceof Variable var) { 
                textboxWidth = textboxWidth - height;
                var scopeWidget = new CyclingButtonWidget.Builder<Scope>(scope -> Text.literal(scope.getShortName()).setStyle(Style.EMPTY.withColor(scope.color))).values(Scope.unsaved, Scope.saved, Scope.local, Scope.line).omitKeyText().build(x+textboxWidth,y,height,height,Text.literal(""));
                scopeWidget.setValue(var.getScope());
                widgets.add(scopeWidget);
            }
//            if(item instanceof Parameter parameter) {
//                textboxWidth = textboxWidth - 18;
//
//                widgets.add(new FakeSlot(x+textboxWidth,y,Text.literal("Paramater Data"), handler));
//            }
            var text = new TextFieldWidget(textRender,x,y,textboxWidth,height,Text.literal(""));
            text.setMaxLength(10000);
            text.setText(named.getName());
            text.setFocused(false);
            widgets.add(0,text);
        }
        if(item instanceof GameValue value) {
            int targetWidth = 60;
            int textboxWidth = width - targetWidth;

            var text = new TextFieldWidget(textRender,x,y,textboxWidth,height,Text.literal(""));
            text.setMaxLength(10000);
            text.setText(value.getType());
            widgets.add(text);

            var scopeWidget = new CyclingButtonWidget.Builder<Target>(target -> Text.literal(target.name()).setStyle(Style.EMPTY.withColor(target.color))).values(
                    Target.Selection,
                    Target.Default,
                    Target.Killer,
                    Target.Damager,
                    Target.Victim,
                    Target.Shooter,
                    Target.Projectile,
                    Target.LastEntity
            ).omitKeyText().build(x+textboxWidth,y,targetWidth,height,Text.literal(""));
            scopeWidget.setValue(value.getTarget());
            widgets.add(scopeWidget);
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
        if(item instanceof Potion pot) {
            int durationWidth = 45;
            int potencyWidth = 32;
            int textboxWidth = width - durationWidth - potencyWidth;
            var text = new TextFieldWidget(textRender,x,y,textboxWidth,height,Text.literal(""));
            text.setText(pot.getPotion());
            widgets.add(text);
            var potency = new NumberFieldWidget(textRender,x+textboxWidth,y,potencyWidth,height,Text.empty()).integer().min(-255).max(255);
            potency.setNumber(pot.getAmplifier() + 1);
            widgets.add(potency);
            var duration = new TextFieldWidget(textRender,x+textboxWidth+potencyWidth,y,durationWidth,height,Text.empty());
            duration.setText(pot.duration().replaceAll(" ticks",""));
            widgets.add(duration);
        }
        if(item instanceof Sound sound) {
            int volumeWidth = 30;
            int pitchWidth = 30;
            int textboxWidth = width - volumeWidth - pitchWidth;
            var text = new TextFieldWidget(textRender,x,y,textboxWidth,height,Text.literal(""));
            text.setText(sound.getSound());
            widgets.add(text);
            var volume = new NumberFieldWidget(textRender,x+textboxWidth,y,volumeWidth,height,Text.empty()).min(0);
            volume.setNumber(sound.getVolume());
            widgets.add(volume);
            var pitch = new NumberFieldWidget(textRender,x+textboxWidth+volumeWidth,y,pitchWidth,height,Text.empty()).min(0).max(2);
            pitch.setNumber(sound.getPitch());
            widgets.add(pitch);
        }
        if(item instanceof BlockTag tag) {
//            int slotSize = 18;
//            int textboxWidth = width - slotSize;
//            try {
//                var lines = Utility.getBlockTagLines(stack).values();
//                var builder = new CyclingButtonWidget.Builder<>(Text::literal).values(lines);
//                for (var line: lines) {
//                    if(textRender.getWidth(ScreenTexts.composeGenericOptionText(Text.literal(tag.getTag()),Text.literal(line))) > width - 10) {
//                        builder.omitKeyText();
//                        break;
//                    }
//                }
//                widgets.add(builder.build(x,y,textboxWidth,height,Text.literal(tag.getTag())));
//            }
//            catch (Exception ignored) {}
//            var varItem = new FakeSlot(x + textboxWidth, y, Text.empty(), handler);
//            if(tag.getVariable() != null) varItem.item = tag.getVariable().toStack();
//            widgets.add(varItem);
            widgets.add(new TextWidget(x,y,width,height,ScreenTexts.composeGenericOptionText(Text.literal(tag.getTag()),Text.literal(tag.getOption())),textRender).alignCenter());
        }
        this.widgets = widgets;
    }

    private void updateItem() {
        if(item instanceof NamedItem named) {
            if(widgets.get(0) instanceof TextFieldWidget text) {
                named.setName(text.getText());
            }
            if(named instanceof Variable var) {
                if(widgets.get(1) instanceof CyclingButtonWidget<?> cycle) {
                    var.setScope((Scope) cycle.getValue());
                }
            }
        }
        if(item instanceof GameValue value) {
            if(widgets.get(0) instanceof TextFieldWidget text) {
                value.setType(text.getText());
            }
            if(widgets.get(1) instanceof CyclingButtonWidget<?> cycle) {
                value.setTarget((Target) cycle.getValue());
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
        if(item instanceof Sound sound) {
            if(widgets.get(0) instanceof TextFieldWidget text) sound.setSound(text.getText());
            if(widgets.get(1) instanceof NumberFieldWidget num) sound.setVolume(num.getNumber());
            if(widgets.get(2) instanceof NumberFieldWidget num) sound.setPitch(num.getNumber());
        }
        if(item instanceof Potion pot) {
            if(widgets.get(0) instanceof TextFieldWidget text) pot.setPotion(text.getText());
            if(widgets.get(1) instanceof NumberFieldWidget num) pot.setAmplifier(num.getInt() - 1);
            if(widgets.get(2) instanceof TextFieldWidget text) {
                String value = text.getText();
                try {
                    pot.setDuration(Integer.parseInt(value));
                } catch (Exception ignored) {
                    try {
                        var values = value.split(":");
                        pot.setDuration((Integer.parseInt(values[0]) * 60 + Integer.parseInt(values[1])) * 20);
                    } catch (Exception ignored2) {
                        pot.setDuration(1000000);
                    }
                }
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        for (var widget: widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var widget: widgets) {
            if(widget instanceof ClickableWidget clickable) {
                if(clickable.isMouseOver(mouseX,mouseY)) {
                    clickable.setFocused(true);
                    clickable.mouseClicked(mouseX, mouseY, button);
                }
                else clickable.setFocused(false);
            }
        }
        updateItem();
        return true;
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
