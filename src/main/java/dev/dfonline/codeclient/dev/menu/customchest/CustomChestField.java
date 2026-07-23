package dev.dfonline.codeclient.dev.menu.customchest;

import com.google.common.primitives.Doubles;
import dev.dfonline.codeclient.hypercube.Target;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.hypercube.item.Number;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CustomChestField<ItemType extends VarItem> extends AbstractWidget {
    private final List<Renderable> widgets;
    public ItemType item;

    public CustomChestField(Font textRender, int x, int y, int width, int height, Component message, ItemType item) {
        super(x, y, width, height, message);
        this.item = item;
        var widgets = new ArrayList<Renderable>();
        if (item instanceof NamedItem named) {
            int textboxWidth = width;
            if (item instanceof Variable var) {
                textboxWidth = textboxWidth - height;
                var scopeWidget = new CycleButton.Builder<>(scope -> Component.literal(scope.getShortName()).setStyle(Style.EMPTY.withColor(scope.color)), var::getScope).withValues(Scope.unsaved, Scope.saved, Scope.local, Scope.line).displayOnlyValue().create(x + textboxWidth, y, height, height, Component.literal(""));
                widgets.add(scopeWidget);
            }
//            if(item instanceof Parameter parameter) {
//                textboxWidth = textboxWidth - 18;
//
//                widgets.add(new FakeSlot(x+textboxWidth,y,Text.literal("Paramater Data"), handler));
//            }
            var text = new EditBox(textRender, x, y, textboxWidth, height, Component.literal(""));
            text.setMaxLength(10000);
            text.setValue(named.getName());
            text.setFocused(false);
            text.moveCursorToStart(false);
            widgets.add(0, text);
        }
        if (item instanceof GameValue value) {
            int targetWidth = 60;
            int textboxWidth = width - targetWidth;

            var text = new EditBox(textRender, x, y, textboxWidth, height, Component.literal(""));
            text.setMaxLength(10000);
            text.setValue(value.getType());
            widgets.add(text);

            var scopeWidget = new CycleButton.Builder<>(target -> Component.literal(target.name()).setStyle(Style.EMPTY.withColor(target.color)), value::getTarget).withValues(
                    Target.Selection,
                    Target.Default,
                    Target.Killer,
                    Target.Damager,
                    Target.Victim,
                    Target.Shooter,
                    Target.Projectile,
                    Target.LastEntity
            ).displayOnlyValue().create(x + textboxWidth, y, targetWidth, height, Component.literal(""));
            widgets.add(scopeWidget);
        }
        if (item instanceof Vector vec) {
            Double[] values = {vec.getX(), vec.getY(), vec.getZ()};
            int textboxWidth = width / 3;
            for (int i = 0; i < 3; i++) {
                var field = new NumberFieldWidget(textRender, x + (textboxWidth * i), y, textboxWidth, height, Component.literal(""));
                field.setMaxLength(10);
                field.setNumber(values[i]);
                field.moveCursorToStart(false);
                widgets.add(field);
            }
        }
        if (item instanceof Location loc) {
            Double[] values = {loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw()};
            int textboxWidth = width / 5;
            for (int i = 0; i < 5; i++) {
                var field = new NumberFieldWidget(textRender, x + (textboxWidth * i), y, textboxWidth, height, Component.literal(""));
                field.setMaxLength(10);
                field.setNumber(values[i]);
                field.moveCursorToStart(false);
                field.moveCursorToStart(false);
                widgets.add(field);
            }
        }
        if (item instanceof Potion pot) {
            int durationWidth = 45;
            int potencyWidth = 32;
            int textboxWidth = width - durationWidth - potencyWidth;
            var text = new EditBox(textRender, x, y, textboxWidth, height, Component.literal(""));
            text.setValue(pot.getPotion());
            text.moveCursorToStart(false);
            widgets.add(text);
            var potency = new NumberFieldWidget(textRender, x + textboxWidth, y, potencyWidth, height, Component.empty()).integer().min(-255).max(255);
            potency.setNumber(pot.getAmplifier() + 1);
            potency.moveCursorToStart(false);
            widgets.add(potency);
            var duration = new EditBox(textRender, x + textboxWidth + potencyWidth, y, durationWidth, height, Component.empty());
            duration.setValue(pot.duration().replaceAll(" ticks", ""));
            duration.moveCursorToStart(false);
            widgets.add(duration);
        }
        if (item instanceof Sound sound) {
            int volumeWidth = 30;
            int pitchWidth = 30;
            int textboxWidth = width - volumeWidth - pitchWidth;
            var text = new EditBox(textRender, x, y, textboxWidth, height, Component.literal(""));
            text.setValue(sound.getSound());
            text.moveCursorToStart(false);
            widgets.add(text);
            var volume = new NumberFieldWidget(textRender, x + textboxWidth, y, volumeWidth, height, Component.empty()).min(0);
            volume.setNumber(sound.getVolume());
            volume.moveCursorToStart(false);
            widgets.add(volume);
            var pitch = new NumberFieldWidget(textRender, x + textboxWidth + volumeWidth, y, pitchWidth, height, Component.empty()).min(0).max(2);
            pitch.setNumber(sound.getPitch());
            pitch.moveCursorToStart(false);
            widgets.add(pitch);
        }
        if (item instanceof BlockTag tag) {
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
            widgets.add(new StringWidget(x, y, width, height, CommonComponents.optionNameValue(Component.literal(tag.getTag()), Component.literal(tag.getOption())), textRender));
        }
        this.widgets = widgets;
    }

    private void updateItem() {
        if (item instanceof NamedItem named) {
            if (widgets.get(0) instanceof EditBox text) {
                named.setName(text.getValue());
            }
            if (named instanceof Variable var) {
                if (widgets.get(1) instanceof CycleButton<?> cycle) {
                    var.setScope((Scope) cycle.getValue());
                }
            }
        }
        if (item instanceof GameValue value) {
            if (widgets.get(0) instanceof EditBox text) {
                value.setType(text.getValue());
            }
            if (widgets.get(1) instanceof CycleButton<?> cycle) {
                value.setTarget((Target) cycle.getValue());
            }
        }
        if (item instanceof Vector vec) {
            if (widgets.get(0) instanceof NumberFieldWidget num1
                    && widgets.get(1) instanceof NumberFieldWidget num2
                    && widgets.get(2) instanceof NumberFieldWidget num3)
                vec.setCoords(num1.getNumber(), num2.getNumber(), num3.getNumber());
            this.item = (ItemType) vec;
        }
        if (item instanceof Location loc) {
            if (
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
        if (item instanceof Sound sound) {
            if (widgets.get(0) instanceof EditBox text) sound.setSound(text.getValue());
            if (widgets.get(1) instanceof NumberFieldWidget num) sound.setVolume(num.getNumber());
            if (widgets.get(2) instanceof NumberFieldWidget num) sound.setPitch(num.getNumber());
        }
        if (item instanceof Potion pot) {
            if (widgets.get(0) instanceof EditBox text) pot.setPotion(text.getValue());
            if (widgets.get(1) instanceof NumberFieldWidget num) pot.setAmplifier(num.getInt() - 1);
            if (widgets.get(2) instanceof EditBox text) {
                String value = text.getValue();
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
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget click) click.setFocused(focused);
        }
        super.setFocused(focused);
    }

    public void select(boolean start) {
        super.setFocused(true);
        if(widgets.get(!start ? widgets.size() - 1 : 0) instanceof AbstractWidget next) {
            next.setFocused(true);
        }
    }

    public void selectAll() {
        for (var widget: widgets) {
            if(widget instanceof EditBox text) {
                text.moveCursorToStart(false);
                text.moveCursorToEnd(true);
            }
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        for (var widget : widgets) {
            widget.extractRenderState(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget clickable) {
                if (clickable.isMouseOver(click.x(), click.y())) {
                    clickable.setFocused(true);
                    clickable.mouseClicked(click, doubled);
                } else clickable.setFocused(false);
            }
        }
        updateItem();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.USAGE, "Value Editor");
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget click && click.isMouseOver(mouseX, mouseY)) {
                if(click.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                    updateItem();
                }
                if(item instanceof Number && widget instanceof EditBox text) {
                    var num = Doubles.tryParse(text.getValue().isBlank() ? "0" : text.getValue());
                    if(num != null) {
                        var format = new DecimalFormat("0.#");
                        format.setMinimumFractionDigits(0);
                        text.setValue(format.format(num + (verticalAmount > 0 ? 1 : -1)));
                        updateItem();
                    }
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        var keyCode = input.key();
        var modifiers = input.modifiers();
        if(keyCode == GLFW.GLFW_KEY_TAB) {
            Integer selected = null;
            for (int i = 0; i < widgets.size(); i++) {
                var widget = widgets.get(i);
                if (widget instanceof AbstractWidget clickable && clickable.isFocused()) {
                    if(selected == null) selected = i;
                    clickable.setFocused(false);
                }
            }
            if(selected == null) return false;
            var next = selected + ((modifiers & GLFW.GLFW_MOD_SHIFT) == 1 ? -1 : 1);
            if(next < 0 || next == widgets.size()) return false;
            if(widgets.get(next) instanceof AbstractWidget widget) {
                widget.setFocused(true);
                return true;
            }
            return false;
        }
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget click && click.isFocused()) {
                return click.keyPressed(input);
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget click && click.isFocused()) {
                updateItem();
                return click.keyReleased(input);
            }
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        for (var widget : widgets) {
            if (widget instanceof AbstractWidget click && click.isFocused()) {
                updateItem();
                return click.charTyped(input);
            }
        }
        return super.charTyped(input);
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return super.getCurrentFocusPath();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }
}
