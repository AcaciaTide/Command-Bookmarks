package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmDeleteScreen extends Screen {
    private final BookmarkListScreen parent;
    private final Bookmark bookmark;

    public ConfirmDeleteScreen(BookmarkListScreen parent, Bookmark bookmark) {
        super(Component.translatable("gui.commandbookmark.delete.title"));
        this.parent = parent;
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 100;
        int centerX = this.width / 2;
        int buttonY = this.height / 2 + 40;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.commandbookmark.delete.cancel"), button -> {
            this.onClose();
        }).bounds(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.commandbookmark.delete.confirm"), button -> {
            CommandBookmarkClient.MANAGER.remove(this.bookmark.getId());
            assert this.minecraft != null;
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX + 5, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFF5555);
        context.centeredText(this.font, Component.translatable("gui.commandbookmark.delete.message"), this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
        
        String displayText = this.bookmark.getLabel().isEmpty() ? this.bookmark.getCommand() : this.bookmark.getLabel();
        context.centeredText(this.font, Component.literal(displayText), this.width / 2, this.height / 2, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}
