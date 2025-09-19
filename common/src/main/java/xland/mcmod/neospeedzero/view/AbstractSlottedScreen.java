package xland.mcmod.neospeedzero.view;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
abstract class AbstractSlottedScreen extends Screen {
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int leftPos, topPos;

    protected final List<@NotNull FakeSlot> slots = Lists.newArrayList();
    protected int hoveredSlotIndex = -1;

    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");

    protected AbstractSlottedScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        final int leftPos = this.leftPos;
        final int topPos = this.topPos;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);
        guiGraphics.drawString(this.font, this.title, 8, 6, 0xff404040, false);
        this.findHoveredSlot(mouseX, mouseY);
        renderSlotHighlightBack(guiGraphics);
        this.renderSlots(guiGraphics);
        renderSlotHighlightFront(guiGraphics);
        guiGraphics.pose().popMatrix();

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderExtraBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected abstract void renderExtraBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected void renderSlots(GuiGraphics guiGraphics) {
        for (FakeSlot slot : this.slots) {
            slot.render(guiGraphics, getFont());
        }
    }

    private void findHoveredSlot(double mouseX, double mouseY) {
        for (int i = 0; i < slots.size(); i++) {
            if (isHovering(slots.get(i), mouseX, mouseY)) {
                hoveredSlotIndex = i;
                return;
            }
        }
        // found nothing
        hoveredSlotIndex = -1;
    }

    protected boolean isHovering(int x, int y, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        mouseX -= i;
        mouseY -= j;
        return mouseX >= x - 1 && mouseX < x + 16 + 1 && mouseY >= y - 1 && mouseY < y + 16 + 1;
    }

    protected boolean isHovering(FakeSlot fakeSlot, double mouseX, double mouseY) {
        return isHovering(fakeSlot.x(), fakeSlot.y(), mouseX, mouseY);
    }

    private @Nullable FakeSlot getCachedHoveredSlot() {
        final int index = hoveredSlotIndex;
        if (index < 0 || index >= slots.size()) return null;
        return slots.get(index);
    }

    private void renderSlotHighlightBack(GuiGraphics guiGraphics) {
        FakeSlot cachedHoveredSlot = this.getCachedHoveredSlot();
        if (cachedHoveredSlot != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, cachedHoveredSlot.x() - 4, cachedHoveredSlot.y() - 4, 24, 24);
        }
    }

    private void renderSlotHighlightFront(GuiGraphics guiGraphics) {
        FakeSlot cachedHoveredSlot = this.getCachedHoveredSlot();
        if (cachedHoveredSlot != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, cachedHoveredSlot.x() - 4, cachedHoveredSlot.y() - 4, 24, 24);
        }
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        FakeSlot cachedHoveredSlot = this.getCachedHoveredSlot();
        if (cachedHoveredSlot != null) {
            ItemStack itemStack = cachedHoveredSlot.getItemStack();
            if (itemStack.isEmpty()) return;
            assert this.minecraft != null;
            guiGraphics.setTooltipForNextFrame(
                    this.font, getTooltipFromItem(this.minecraft, itemStack), itemStack.getTooltipImage(), x, y, itemStack.get(DataComponents.TOOLTIP_STYLE)
            );
        }
    }

    protected static class FakeSlot {
        private final int x, y;
        private @NotNull ItemStack itemStack;

        public FakeSlot(int x, int y, @Nullable ItemStack itemStack) {
            this.x = x;
            this.y = y;
            this.itemStack = orEmpty(itemStack);
        }

        private static @NotNull ItemStack orEmpty(@Nullable ItemStack stack) {
            return Objects.requireNonNullElse(stack, ItemStack.EMPTY);
        }

        public FakeSlot(int x, int y) {
            this(x, y, ItemStack.EMPTY);
        }

        public int x() { return x; }
        public int y() { return y; }

        public @NotNull ItemStack getItemStack() {
            return itemStack;
        }

        public void setItemStack(@Nullable ItemStack itemStack) {
            this.itemStack = orEmpty(itemStack);
        }

        public void render(GuiGraphics guiGraphics, Font font) {
            guiGraphics.renderItem(getItemStack(), x(), y());
            guiGraphics.renderItemDecorations(font, getItemStack(), x(), y());
        }
    }
}
