package acaciatide.commandbookmark.gui.widget;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import acaciatide.commandbookmark.gui.BookmarkListScreen;
import acaciatide.commandbookmark.gui.ConfirmDeleteScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import java.util.List;

public class BookmarkListWidget extends AlwaysSelectedEntryListWidget<BookmarkListWidget.Entry> {
    private final BookmarkListScreen parentScreen;

    public BookmarkListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, BookmarkListScreen parentScreen) {
        super(client, width, height, y, itemHeight);
        this.parentScreen = parentScreen;
        updateEntries();
    }

    public void updateEntries() {
        this.clearEntries();
        List<Bookmark> bookmarks = CommandBookmarkClient.MANAGER.getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            this.addEntry(new Entry(bookmark, this.parentScreen));
        }
    }

    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final Bookmark bookmark;
        private final BookmarkListScreen parentScreen;
        private final ButtonWidget executeButton;
        private final ButtonWidget deleteButton;

        public Entry(Bookmark bookmark, BookmarkListScreen parentScreen) {
            this.bookmark = bookmark;
            this.parentScreen = parentScreen;

            this.executeButton = ButtonWidget.builder(Text.literal("▶"), button -> {
                this.parentScreen.executeBookmark(this.bookmark);
            }).dimensions(0, 0, 20, 20).build();

            this.deleteButton = ButtonWidget.builder(Text.literal("🗑"), button -> {
                MinecraftClient.getInstance().setScreen(new ConfirmDeleteScreen(this.parentScreen, this.bookmark));
            }).dimensions(0, 0, 20, 20).build();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int entryWidth = BookmarkListWidget.this.getRowWidth();
            int entryHeight = 26; // BookmarkListScreenで指定したitemHeightと同じ値
            
            String displayText = this.bookmark.getLabel().isEmpty() ? this.bookmark.getCommand() : this.bookmark.getLabel();
            
            // テキストの描画（Y座標を文字の高さに合わせて中央揃え）
            int textY = y + (entryHeight - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayText, x + 5, textY, 0xFFFFFFFF);

            // ボタンのサイズに合わせてY座標を中央揃え
            int buttonSize = 20;
            int buttonY = y + (entryHeight - buttonSize) / 2;

            // 実行ボタンの配置と描画
            this.executeButton.setX(x + entryWidth - 55); // 少し左に寄せる
            this.executeButton.setY(buttonY);
            this.executeButton.render(context, mouseX, mouseY, tickDelta);

            // 削除ボタンの配置と描画
            this.deleteButton.setX(x + entryWidth - 30); // 少し左に寄せる
            this.deleteButton.setY(buttonY);
            this.deleteButton.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean bl) {
            if (this.executeButton.mouseClicked(click, bl)) {
                return true;
            }
            if (this.deleteButton.mouseClicked(click, bl)) {
                return true;
            }
            return super.mouseClicked(click, bl);
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.bookmark.getLabel());
        }
    }
}
