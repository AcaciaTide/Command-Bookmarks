package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import acaciatide.commandbookmark.gui.widget.BookmarkListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BookmarkListScreen extends Screen {
    private final Screen parent;
    private BookmarkListWidget listWidget;

    public BookmarkListScreen(Screen parent) {
        super(Text.translatable("gui.commandbookmark.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.listWidget = new BookmarkListWidget(this.client, this.width, this.height - 70, 32, 26, this);
        this.addDrawableChild(this.listWidget);

        int buttonWidth = 150;
        int closeWidth = 100;
        int totalWidth = buttonWidth + 10 + closeWidth;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 30;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.commandbookmark.add"), button -> {
            assert this.client != null;
            this.client.setScreen(new AddBookmarkScreen(this));
        }).dimensions(startX, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.commandbookmark.close"), button -> {
            this.close();
        }).dimensions(startX + buttonWidth + 10, buttonY, closeWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        if (CommandBookmarkClient.MANAGER.getBookmarks().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.empty"), this.width / 2, this.height / 2, 0xA0A0A0);
        }
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    public void executeBookmark(Bookmark bookmark) {
        assert this.client != null;
        String text = bookmark.getCommand().trim();

        this.close();

        if (text.startsWith("/")) {
            this.client.getNetworkHandler().sendChatCommand(text.substring(1));
        } else {
            this.client.getNetworkHandler().sendChatMessage(text);
        }
    }
}
