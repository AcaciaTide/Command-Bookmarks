package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AddBookmarkScreen extends Screen {
    private final BookmarkListScreen parent;
    private TextFieldWidget labelField;
    private TextFieldWidget commandField;
    private ButtonWidget saveButton;
    private boolean isDuplicate = false;

    public AddBookmarkScreen(BookmarkListScreen parent) {
        super(Text.translatable("gui.commandbookmark.add.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int fieldWidth = 200;

        this.labelField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, 50, fieldWidth, 20, Text.translatable("gui.commandbookmark.add.label"));
        this.labelField.setMaxLength(32);
        this.addDrawableChild(this.labelField);

        this.commandField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, 100, fieldWidth, 20, Text.translatable("gui.commandbookmark.add.command"));
        this.commandField.setMaxLength(500);
        this.commandField.setChangedListener(text -> this.updateSaveButton());
        this.addDrawableChild(this.commandField);

        int buttonWidth = 95;
        this.saveButton = ButtonWidget.builder(Text.translatable("gui.commandbookmark.add.save"), button -> {
            this.save();
        }).dimensions(centerX - 100, 140, buttonWidth, 20).build();
        this.addDrawableChild(this.saveButton);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.commandbookmark.add.cancel"), button -> {
            this.close();
        }).dimensions(centerX + 5, 140, buttonWidth, 20).build());

        this.setInitialFocus(this.commandField);
        this.updateSaveButton();
    }

    private void updateSaveButton() {
        String text = this.commandField.getText().trim();
        this.saveButton.active = !text.isEmpty();
        
        // 既存のブックマークとコマンド文字列が完全に一致するか判定する（空白のみの違いは同一とみなす）
        this.isDuplicate = CommandBookmarkClient.MANAGER.getBookmarks().stream()
                .anyMatch(b -> b.getCommand().trim().equals(text));
    }

    private void save() {
        String label = this.labelField.getText().trim();
        String command = this.commandField.getText().trim();
        if (!command.isEmpty()) {
            CommandBookmarkClient.MANAGER.add(new Bookmark(label, command));
            assert this.client != null;
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFFFF);
        
        context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.add.label"), this.width / 2 - 100, 35, 0xFFA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.add.command"), this.width / 2 - 100, 85, 0xFFA0A0A0);
        
        if (this.isDuplicate) {
            context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.commandbookmark.add.duplicate_warning"), this.width / 2 - 100, 122, 0xFFFFAA00);
        }
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }
}
