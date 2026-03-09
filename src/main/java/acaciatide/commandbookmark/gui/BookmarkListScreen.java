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
    private ButtonWidget modeButton;

    public BookmarkListScreen(Screen parent) {
        super(Text.translatable("gui.commandbookmark.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        CommandBookmarkClient.MANAGER.updateLocalContext();

        this.modeButton = ButtonWidget.builder(Text.empty(), button -> this.toggleMode())
                .dimensions(this.width - 160 - 10, 10, 160, 20).build();
        this.updateModeButtonText();
        this.addDrawableChild(this.modeButton);

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

    private void toggleMode() {
        boolean current = CommandBookmarkClient.MANAGER.isGlobalMode();
        CommandBookmarkClient.MANAGER.setGlobalMode(!current);
        CommandBookmarkClient.MANAGER.updateLocalContext(); // 念のためフォールバック再チェック
        this.updateModeButtonText();
        this.listWidget.updateEntries();
    }

    private void updateModeButtonText() {
        if (CommandBookmarkClient.MANAGER.isGlobalMode()) {
            this.modeButton.setMessage(Text.translatable("gui.commandbookmark.mode.global"));
        } else {
            String worldId = CommandBookmarkClient.MANAGER.getCurrentLocalId();
            if (worldId == null) {
                // ローカルが取得不能な場合はグローバルに強制フォールバック
                CommandBookmarkClient.MANAGER.setGlobalMode(true);
                this.modeButton.setMessage(Text.translatable("gui.commandbookmark.mode.global"));
            } else {
                String shortId = worldId.replaceFirst("^(local|server)_", "");
                if (shortId.length() > 15) shortId = shortId.substring(0, 15) + "...";
                this.modeButton.setMessage(Text.translatable("gui.commandbookmark.mode.local", shortId));
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFFFF);

        if (this.listWidget == null || this.listWidget.children().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.empty"), this.width / 2, this.height / 2, 0xFFA0A0A0);
        } else if (this.listWidget.isMouseOver(mouseX, mouseY)) {
            BookmarkListWidget.Entry entry = this.listWidget.getHoveredEntry();
            if (entry != null) {
                int x = entry.getX();
                int entryWidth = this.listWidget.getRowWidth();
                if (mouseX >= x + 5 && mouseX <= x + entryWidth - 85) {
                    context.drawTooltip(this.textRenderer, entry.getTooltipText(), mouseX, mouseY);
                }
            }
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
