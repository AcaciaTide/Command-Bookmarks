package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import acaciatide.commandbookmark.gui.widget.BookmarkListWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BookmarkListScreen extends Screen {
    private final Screen parent;
    private BookmarkListWidget listWidget;
    private Button modeButton;

    public BookmarkListScreen(Screen parent) {
        super(Component.translatable("gui.commandbookmark.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        CommandBookmarkClient.MANAGER.updateLocalContext();

        this.modeButton = Button.builder(Component.empty(), button -> this.toggleMode())
                .bounds(this.width - 160 - 10, 10, 160, 20).build();
        this.updateModeButtonText();
        this.addRenderableWidget(this.modeButton);

        this.listWidget = new BookmarkListWidget(this.minecraft, this.width, this.height - 70, 32, 26, this);
        this.addRenderableWidget(this.listWidget);

        int buttonWidth = 150;
        int closeWidth = 100;
        int totalWidth = buttonWidth + 10 + closeWidth;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 30;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.commandbookmark.add"), button -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new AddBookmarkScreen(this));
        }).bounds(startX, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.commandbookmark.close"), button -> {
            this.onClose();
        }).bounds(startX + buttonWidth + 10, buttonY, closeWidth, 20).build());
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
            this.modeButton.setMessage(Component.translatable("gui.commandbookmark.mode.global"));
        } else {
            String worldId = CommandBookmarkClient.MANAGER.getCurrentLocalId();
            if (worldId == null) {
                // ローカルが取得不能な場合はグローバルに強制フォールバック
                CommandBookmarkClient.MANAGER.setGlobalMode(true);
                this.modeButton.setMessage(Component.translatable("gui.commandbookmark.mode.global"));
            } else {
                String shortId = worldId.replaceFirst("^(local|server)_", "");
                if (shortId.length() > 15) shortId = shortId.substring(0, 15) + "...";
                this.modeButton.setMessage(Component.translatable("gui.commandbookmark.mode.local", shortId));
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        if (this.listWidget == null || this.listWidget.children().isEmpty()) {
            context.centeredText(this.font, Component.translatable("gui.commandbookmark.empty"), this.width / 2, this.height / 2, 0xFFA0A0A0);
        } else if (this.listWidget.isMouseOver(mouseX, mouseY)) {
            BookmarkListWidget.Entry entry = this.listWidget.getHovered();
            if (entry != null) {
                int x = entry.getX();
                int entryWidth = this.listWidget.getRowWidth();
                if (mouseX >= x + 5 && mouseX <= x + entryWidth - 85) {
                    context.setComponentTooltipForNextFrame(this.font, entry.getTooltipText(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    public void executeBookmark(Bookmark bookmark) {
        assert this.minecraft != null;
        String text = bookmark.getCommand().trim();

        this.onClose();

        if (text.startsWith("/")) {
            this.minecraft.getConnection().sendCommand(text.substring(1));
        } else {
            this.minecraft.getConnection().sendChat(text);
        }
    }
}
