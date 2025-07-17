package xland.mcmod.neospeedzero.view;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import xland.mcmod.neospeedzero.NeoSpeedZero;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class ViewChallengeScreen extends AbstractSlottedScreen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "textures/gui/view.png");
    private static final int COLOR_COMPLETED = 0x45c545, COLOR_NOT_COMPLETED = 0xc54545;

    private final ChallengeSnapshot snapshot;
    int page;
    private final TriState[] cachedConditions;

    private PageButton prevButton, nextButton;

    public ViewChallengeScreen(ChallengeSnapshot challengeSnapshot) {
        super(challengeSnapshot.title());
        this.snapshot = challengeSnapshot;

        this.cachedConditions = new TriState[63];
        Arrays.fill(this.cachedConditions, TriState.DEFAULT);
    }

    @Override
    protected void init() {
        super.init();
        addSlots();
        prevButton = this.addRenderableWidget(new PageButton(leftPos + 7, topPos + 149, false, button -> this.previousPage(), false));
        nextButton = this.addRenderableWidget(new PageButton(leftPos + 151, topPos + 149, true, button -> this.nextPage(), false));
        updateThisPage();
    }

    public void onDataUpdate(ChallengeSnapshot.Change change) {
        if (change.applyTo(snapshot)) {
            updateThisPage();
        }
    }

    private void addSlots() {
        for (int i = 0; i < 63; i++) {
            this.slots.add(new FakeSlot(8 + 18 * (i % 9), 19 + 18 * (i / 9)));
        }
    }

    public int totalPageCount() {
        return this.snapshot.totalPageCount();
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            page--;
            updateThisPage();
        }
    }

    public boolean hasPreviousPage() {
        int newPage = page - 1;
        return newPage >= 0 && newPage < totalPageCount();
    }

    public void nextPage() {
        if (hasNextPage()) {
            page++;
            updateThisPage();
        }
    }

    public boolean hasNextPage() {
        int newPage = page + 1;
        return newPage >= 0 && newPage < totalPageCount();
    }

    protected void updateThisPage() {
        // [indexFrom, indexTo)
        final int indexFrom = Mth.clamp(snapshot.challenges().size() - 1, 0, page * 63);
        final int indexTo = Mth.clamp(snapshot.challenges().size(), 0, (page + 1) * 63);

        // Clear existing ones
        Arrays.fill(cachedConditions, TriState.DEFAULT);

        for (int rawIndex = indexFrom, slotIndex = 0; rawIndex < indexTo; rawIndex++, slotIndex++) {
            slots.get(slotIndex).setItemStack(snapshot.challenges().get(rawIndex));
            cachedConditions[slotIndex] = snapshot.successTimeMap()[rawIndex] >= 0 ? TriState.TRUE : TriState.FALSE;
        }

        // Also update button
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.prevButton.visible = hasPreviousPage();
        this.nextButton.visible = hasNextPage();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderExtraBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0F, 0F, imageWidth, imageHeight, 176, 166);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);
        // Coloring
        for (int i = 0; i < 63; i++) {
            FakeSlot fakeSlot = slots.get(i);
            switch (cachedConditions[i]) {
                case TRUE -> guiGraphics.fill(fakeSlot.x(), fakeSlot.y(), fakeSlot.x() + 16, fakeSlot.y() + 16, COLOR_COMPLETED);
                case FALSE -> guiGraphics.fill(fakeSlot.x(), fakeSlot.y(), fakeSlot.x() + 16, fakeSlot.y() + 16, COLOR_NOT_COMPLETED);
            }
        }
        guiGraphics.pose().popMatrix();
    }
}
