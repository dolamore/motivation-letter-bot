package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import com.test.motivationletterbot.entity.keyboard.InlineKeyboards;
import com.test.motivationletterbot.entity.textentry.TextEntry;
import com.test.motivationletterbot.entity.textentry.TextEntryType;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static com.test.motivationletterbot.constants.MessageConstants.MENU_MESSAGE;
import static com.test.motivationletterbot.constants.MessageConstants.STARTING_MESSAGE;
import static com.test.motivationletterbot.entity.textentry.TextEntryType.VACANCY_TEXT_ENTRY;
import static com.test.motivationletterbot.entity.commands.BotMenuStateEnum.*;
import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.GENERATE_ROW;

@Getter
@Setter
public class UserSession {
    @Setter
    private static InlineKeyboards inlineKeyboards;

    private boolean messageOnWork = false;
    private boolean sessionStarted = false;

    private final EnumMap<TextEntryType, TextEntry> entries = new EnumMap<>(TextEntryType.class);

    private int lastKeyboardMessageId;
    private boolean lastMessageHadKeyboard = false;

    private EnumSet<CommandsEnum> menuState = START_MENU_STATE.getStateCommands();

    public UserSession() {
        for (TextEntryType type : TextEntryType.values()) {
            entries.put(type, new TextEntry(type));
        }
    }

    public List<BotCommand> getBotCommands() {
        return menuState.stream()
                .map(CommandsEnum::getBotCommand)
                .toList();
    }

    public void resetLastMessageKeyboardInfo() {
        lastKeyboardMessageId = 0;
        lastMessageHadKeyboard = false;
    }

    public void setLastMessageKeyboardInfo(int messageId) {
        lastKeyboardMessageId = messageId;
        lastMessageHadKeyboard = true;
    }

    public void startSession() {

        //TODO: make main menu state being dynamic
        menuState = MAIN_MENU_STATE.getStateCommands();
        entries.values().forEach(TextEntry::reset);
        entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    public void returnToMenu() {
        menuState = MAIN_MENU_STATE.getStateCommands();
        entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    public void startWriting(TextEntryType textEntryType) {
        var entry = entries.get(textEntryType);
        entry.startWriting();
        //menuState = entry.getStateCommands();
    }

    public void continueWriting(TextEntryType textEntryType) {
        var entry = entries.get(textEntryType);
        menuState.add(entry.getSubmitCommand());
    }

    public void completeTextEntry(TextEntryType type) {
        entries.get(type).complete();
    }

    public String writeMessage(TextEntryType type) {
        return entries.get(type).getWriteMessage();
    }

    public String continueMessage(TextEntryType type) {
        return entries.get(type).getContinueMessage();
    }

    public String startingMessage() {
        return STARTING_MESSAGE;
    }

    public String menuMessage() {
        var menuMessage = new StringBuffer(MENU_MESSAGE);
        entries.values().forEach(entry -> {
            if (!entry.isComplete()) {
                menuMessage.append(entry.getMenuMessage());
            }
        });
        return menuMessage.toString();
    }

    public List<InlineKeyboardRow> startKeyboard() {
        return inlineKeyboards.getStartKeyboard();
    }

    public List<InlineKeyboardRow> menuKeyboard() {
        var keyboard = inlineKeyboards.getMenuKeyboard(entries.values());
        if (entries.get(VACANCY_TEXT_ENTRY).isComplete()) {
            keyboard.add(GENERATE_ROW.getRow());
        }
        return keyboard;
    }

    public List<InlineKeyboardRow> writingKeyboard() {
        return inlineKeyboards.getReturnMenuKeyboard();
    }

    public List<InlineKeyboardRow> continueKeyboard(TextEntryType type) {
        return inlineKeyboards.getContinueKeyboard(entries.get(type));
    }

    public boolean isOnWork(TextEntryType type) {
        return entries.get(type).isOnWork();
    }

    public void addText(TextEntryType type, String text) {
        entries.get(type).append(text);
        menuState.add(type.getSubmitCommand());
    }
}