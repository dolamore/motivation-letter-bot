package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import com.test.motivationletterbot.entity.keyboard.InlineKeyboards;
import com.test.motivationletterbot.entity.textentry.TextEntry;
import com.test.motivationletterbot.entity.textentry.TextEntryType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.test.motivationletterbot.constants.MessageConstants.*;
import static com.test.motivationletterbot.entity.textentry.TextEntryType.VACANCY_TEXT_ENTRY;
import static com.test.motivationletterbot.entity.commands.BotMenuStateEnum.*;
import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.GENERATE_ROW;
import static com.test.motivationletterbot.entity.commands.CommandsEnum.*;

@Getter
@Setter
@Slf4j
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
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        entries.values().forEach(TextEntry::reset);
        entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    public void returnToMenu() {
        entries.values().forEach(entry -> entry.setOnWork(false));
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    public void startWriting(TextEntryType textEntryType) {
        entries.values().forEach(entry -> entry.setOnWork(false));
        var entry = entries.get(textEntryType);
        entry.startWriting();
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        entries.values().stream().filter(e -> !e.isOnWork()).forEach(e -> e.addButtonIfNotCompleted(menuState));
    }

    public void continueWriting() {
        var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
        if (entry == null) {
            return;
        }
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        menuState.add(SUBMIT_COMMAND);
        entries.values().forEach(e -> e.addButtonIfNotCompleted(menuState));
    }

    public void completeTextEntry() {
        var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
        if (entry == null) {
            return;
        }
        entry.complete();
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        entries.values().forEach(e -> e.addButtonIfNotCompleted(menuState));
    }

    public String writeMessage() {
        var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().get();
        return entry.getWriteMessage();
    }

    public String continueMessage() {
        var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
        if (entry == null) {
            return "This command is not available at the moment.";
        }
        return entry.getContinueMessage();
    }

    public String startingMessage() {
        return STARTING_MESSAGE;
    }

    public String menuMessage() {
        StringBuffer menuMessage = new StringBuffer();
        if (isAllComplete()) {
            menuMessage.append(FULLY_COMPLETED_MENU_MESSAGE);
            return menuMessage.toString();
        }
        if (isAllMandatoryComplete()) {
            menuMessage.append(COMPLETED_MENU_MESSAGE);
        } else {
            menuMessage.append(MENU_MESSAGE);
        }
        entries.values().forEach(entry -> {
            if (!entry.isComplete()) {
                menuMessage.append(entry.getMenuMessage());
            }
        });
        return menuMessage.toString();
    }

    private boolean isAllMandatoryComplete() {
        return entries.values().stream().filter(TextEntry::isMandatory).allMatch(TextEntry::isComplete);
    }

    private boolean isAllComplete() {
        return entries.values().stream().allMatch(TextEntry::isComplete);
    }

    public List<InlineKeyboardRow> startKeyboard() {
        return inlineKeyboards.getStartKeyboard();
    }

    public List<InlineKeyboardRow> greetingKeyboard() {
        return inlineKeyboards.getGreetingKeyboard();
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

    public List<InlineKeyboardRow> continueKeyboard() {
        var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
        if (entry == null) {
            return inlineKeyboards.getReturnMenuKeyboard();
        }
        return inlineKeyboards.getContinueKeyboard();
    }

    public boolean isOnWork(TextEntryType type) {
        return entries.get(type).isOnWork();
    }

    public void addText(TextEntryType type, String text) {
        entries.get(type).append(text);
        menuState.add(SUBMIT_COMMAND);
    }

    public String greetingMessage() {
        return GREETING_MESSAGE;
    }
}