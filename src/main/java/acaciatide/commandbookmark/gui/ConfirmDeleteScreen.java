package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmDeleteScreen extends Screen {
    private final BookmarkListScreen parent;
    private final Bookmark bookmark;

    public ConfirmDeleteScreen(BookmarkListScreen parent, Bookmark bookmark) {
        super(Text.translatable("gui.commandbookmark.delete.title"));
        this.parent = parent;
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 100;
        int centerX = this.width / 2;
        int buttonY = this.height / 2 + 40;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.commandbookmark.delete.cancel"), button -> {
            this.close();
        }).dimensions(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.commandbookmark.delete.confirm"), button -> {
            CommandBookmarkClient.MANAGER.remove(this.bookmark.getId());
            assert this.client != null;
            this.client.setScreen(this.parent);
        }).dimensions(centerX + 5, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFF5555);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.delete.message"), this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
        
        String displayText = this.bookmark.getLabel().isEmpty() ? this.bookmark.getCommand() : this.bookmark.getLabel();
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(displayText), this.width / 2, this.height / 2, 0xFFA0A0A0);
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }
}
