package acaciatide.commandbookmark.gui;

import acaciatide.commandbookmark.CommandBookmarkClient;
import acaciatide.commandbookmark.data.Bookmark;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddBookmarkScreen extends Screen {
    private final BookmarkListScreen parent;
    private EditBox labelField;
    private EditBox commandField;
    private Button saveButton;
    private boolean isDuplicate = false;

    public AddBookmarkScreen(BookmarkListScreen parent) {
        super(Component.translatable("gui.commandbookmark.add.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int fieldWidth = 200;

        this.labelField = new EditBox(this.font, centerX - fieldWidth / 2, 50, fieldWidth, 20, Component.translatable("gui.commandbookmark.add.label"));
        this.labelField.setMaxLength(32);
        this.addRenderableWidget(this.labelField);

        this.commandField = new EditBox(this.font, centerX - fieldWidth / 2, 100, fieldWidth, 20, Component.translatable("gui.commandbookmark.add.command"));
        this.commandField.setMaxLength(500);
        this.commandField.setResponder(text -> this.updateSaveButton());
        this.addRenderableWidget(this.commandField);

        int buttonWidth = 95;
        this.saveButton = Button.builder(Component.translatable("gui.commandbookmark.add.save"), button -> {
            this.save();
        }).bounds(centerX - 100, 140, buttonWidth, 20).build();
        this.addRenderableWidget(this.saveButton);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.commandbookmark.add.cancel"), button -> {
            this.onClose();
        }).bounds(centerX + 5, 140, buttonWidth, 20).build());

        this.setInitialFocus(this.commandField);
        this.updateSaveButton();
    }

    private void updateSaveButton() {
        String text = this.commandField.getValue().trim();
        this.saveButton.active = !text.isEmpty();
        
        // 既存のブックマークとコマンド文字列が完全に一致するか判定する（空白のみの違いは同一とみなす）
        this.isDuplicate = CommandBookmarkClient.MANAGER.getBookmarks().stream()
                .anyMatch(b -> b.getCommand().trim().equals(text));
    }

    private void save() {
        String label = this.labelField.getValue().trim();
        String command = this.commandField.getValue().trim();
        if (!command.isEmpty()) {
            CommandBookmarkClient.MANAGER.add(new Bookmark(label, command));
            assert this.minecraft != null;
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
        
        context.text(this.font, Component.translatable("gui.commandbookmark.add.label"), this.width / 2 - 100, 35, 0xFFA0A0A0);
        context.text(this.font, Component.translatable("gui.commandbookmark.add.command"), this.width / 2 - 100, 85, 0xFFA0A0A0);
        
        if (this.isDuplicate) {
            context.text(this.font, Component.translatable("gui.commandbookmark.add.duplicate_warning"), this.width / 2 - 100, 122, 0xFFFFAA00);
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}
