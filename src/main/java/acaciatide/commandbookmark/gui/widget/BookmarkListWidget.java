package acaciatide.commandbookmark.gui.widget;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import acaciatide.commandbookmark.gui.BookmarkListScreen;
import acaciatide.commandbookmark.gui.ConfirmDeleteScreen;
import acaciatide.commandbookmark.gui.EditBookmarkScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class BookmarkListWidget extends ObjectSelectionList<BookmarkListWidget.Entry> {
    private final BookmarkListScreen parentScreen;

    public BookmarkListWidget(Minecraft client, int width, int height, int y, int itemHeight, BookmarkListScreen parentScreen) {
        super(client, width, height, y, itemHeight);
        this.parentScreen = parentScreen;
        updateEntries();
    }

    public void updateEntries() {
        this.clearEntries();
        List<Bookmark> bookmarks = CommandBookmarkClient.MANAGER.getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            this.addEntry(new acaciatide.commandbookmark.gui.widget.BookmarkListWidget.Entry(bookmark, this.parentScreen));
        }
        for (int i = 0; i < this.children().size(); i++) {
            acaciatide.commandbookmark.gui.widget.BookmarkListWidget.Entry entry = this.children().get(i);
            entry.upButton.active = i > 0;
            entry.downButton.active = i < this.children().size() - 1;
        }
    }

    @Override
    public acaciatide.commandbookmark.gui.widget.BookmarkListWidget.Entry getHovered() {
        return super.getHovered();
    }
    public class Entry extends ObjectSelectionList.Entry<acaciatide.commandbookmark.gui.widget.BookmarkListWidget.Entry> {
        private final Bookmark bookmark;
        private final BookmarkListScreen parentScreen;
        private final Button executeButton;
        private final Button editButton;
        private final Button deleteButton;
        private final Button upButton;
        private final Button downButton;
        private final String displayText;
        private final List<Component> tooltipText;

        public Bookmark getBookmark() { return bookmark; }
        public List<Component> getTooltipText() { return tooltipText; }

        public Entry(Bookmark bookmark, BookmarkListScreen parentScreen) {
            this.bookmark = bookmark;
            this.parentScreen = parentScreen;

            this.executeButton = Button.builder(Component.literal("▶"), button -> {
                this.parentScreen.executeBookmark(this.bookmark);
            }).bounds(0, 0, 20, 20).build();

            this.editButton = Button.builder(Component.literal("✏"), button -> {
                Minecraft.getInstance().setScreen(new EditBookmarkScreen(this.parentScreen, this.bookmark));
            }).bounds(0, 0, 20, 20).build();

            this.deleteButton = Button.builder(Component.literal("🗑"), button -> {
                Minecraft.getInstance().setScreen(new ConfirmDeleteScreen(this.parentScreen, this.bookmark));
            }).bounds(0, 0, 20, 20).build();

            this.upButton = Button.builder(Component.literal("↑"), button -> {
                CommandBookmarkClient.MANAGER.moveUp(this.bookmark);
                BookmarkListWidget.this.updateEntries();
            }).bounds(0, 0, 20, 20).build();

            this.downButton = Button.builder(Component.literal("↓"), button -> {
                CommandBookmarkClient.MANAGER.moveDown(this.bookmark);
                BookmarkListWidget.this.updateEntries();
            }).bounds(0, 0, 20, 20).build();

            this.displayText = this.bookmark.getLabel().isEmpty() ? this.bookmark.getCommand() : this.bookmark.getLabel();
            this.tooltipText = List.of(
                Component.literal("§l" + (this.bookmark.getLabel().isEmpty() ? "No Label" : this.bookmark.getLabel())),
                Component.literal("§7" + this.bookmark.getCommand())
            );
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int entryWidth = BookmarkListWidget.this.getRowWidth();
            int entryHeight = 26; // BookmarkListScreenで指定したitemHeightと同じ値
            
            // テキストの描画（Y座標を文字の高さに合わせて中央揃え）
            int textY = y + (entryHeight - Minecraft.getInstance().font.lineHeight) / 2;
            context.text(Minecraft.getInstance().font, this.displayText, x + 5, textY, 0xFFFFFFFF);

            // ボタンのサイズに合わせてY座標を中央揃え
            int buttonSize = 20;
            int buttonY = y + (entryHeight - buttonSize) / 2;

            // 実行ボタンの配置と描画
            this.executeButton.setX(x + entryWidth - 130);
            this.executeButton.setY(buttonY);
            this.executeButton.extractRenderState(context, mouseX, mouseY, tickDelta);

            // 編集ボタンの配置と描画
            this.editButton.setX(x + entryWidth - 105);
            this.editButton.setY(buttonY);
            this.editButton.extractRenderState(context, mouseX, mouseY, tickDelta);

            // 削除ボタンの配置と描画
            this.deleteButton.setX(x + entryWidth - 80);
            this.deleteButton.setY(buttonY);
            this.deleteButton.extractRenderState(context, mouseX, mouseY, tickDelta);

            // 並び替えボタン（↑）の配置と描画
            this.upButton.setX(x + entryWidth - 55);
            this.upButton.setY(buttonY);
            this.upButton.extractRenderState(context, mouseX, mouseY, tickDelta);

            // 並び替えボタン（↓）の配置と描画
            this.downButton.setX(x + entryWidth - 30);
            this.downButton.setY(buttonY);
            this.downButton.extractRenderState(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean bl) {
            if (this.executeButton.mouseClicked(click, bl)) {
                return true;
            }
            if (this.editButton.mouseClicked(click, bl)) {
                return true;
            }
            if (this.deleteButton.mouseClicked(click, bl)) {
                return true;
            }
            if (this.upButton.mouseClicked(click, bl)) {
                return true;
            }
            if (this.downButton.mouseClicked(click, bl)) {
                return true;
            }
            return super.mouseClicked(click, bl);
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.bookmark.getLabel());
        }
    }
}
