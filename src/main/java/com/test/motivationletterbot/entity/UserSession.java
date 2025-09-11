package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import com.test.motivationletterbot.entity.keyboard.InlineKeyboards;
import com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum;
import com.test.motivationletterbot.entity.textentry.TextEntry;
import com.test.motivationletterbot.entity.textentry.TextEntryType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

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
        return new ArrayList<>(menuState.stream()
                .map(CommandsEnum::getBotCommand)
                .toList());
    }

    public void resetLastMessageKeyboardInfo() {
        lastKeyboardMessageId = 0;
        lastMessageHadKeyboard = false;
    }

    public void setLastMessageKeyboardInfo(int messageId) {
        lastKeyboardMessageId = messageId;
        lastMessageHadKeyboard = true;
    }

    public void newSession() {
        menuState.clear();
        menuState.add(RESTART_COMMAND);
    }

    public void startSession() {
        updateMainMenuState();
        entries.values().forEach(TextEntry::reset);
        addTextEntryButtons();
    }

    public void returnToMenu() {
        setAllEntriesOnWorkFalse();
        updateMainMenuState();
        addTextEntryButtons();
    }

    public void startWriting(TextEntryType textEntryType) {
        setAllEntriesOnWorkFalse();
        var entry = entries.get(textEntryType);
        entry.startWriting();
        updateMainMenuState();
        addUncompletedTextEntryButtons();
    }

    public void continueWriting() {
        var entry = getCurrentTextEntry();
        if (entry == null) {
            return;
        }
        updateMainMenuState();
        menuState.add(SUBMIT_COMMAND);
        addTextEntryButtons();
    }

    public void completeTextEntry() {
        var entry = getCurrentTextEntry();
        if (entry == null) {
            return;
        }
        entry.complete();
        updateMainMenuState();
        addTextEntryButtons();
    }

    public String writeMessage() {
        var entry = getCurrentTextEntry();
        if (entry == null) {
            return ERROR_MESSAGE;
        }
        return entry.getWriteMessage();
    }

    public String continueMessage() {
        var entry = getCurrentTextEntry();
        if (entry == null) {
            return ERROR_MESSAGE;
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

    public String generatedText() {
        return "generated text";
    }

    public List<InlineKeyboardRow> startKeyboard() {
        return inlineKeyboards.getStartKeyboard();
    }

    public List<InlineKeyboardRow> greetingKeyboard() {
        return inlineKeyboards.getGreetingKeyboard();
    }

    public List<InlineKeyboardRow> menuKeyboard() {
        var keyboard = inlineKeyboards.getMenuKeyboard(entries.values());
        // add generate row only if VACANCY is complete and not already present
        if (entries.get(VACANCY_TEXT_ENTRY).isComplete()) {
            boolean hasGenerate = keyboard.stream().flatMap(row -> row.stream())
                    .anyMatch(button -> KeyboardRowEnum.GENERATE_ROW.getCallbackData().equals(button.getCallbackData()));
            if (!hasGenerate) {
                keyboard.add(KeyboardRowEnum.GENERATE_ROW.getRow());
            }
        }
        return keyboard;
    }

    public List<InlineKeyboardRow> writingKeyboard() {
        return inlineKeyboards.getReturnMenuKeyboard();
    }

    public List<InlineKeyboardRow> continueKeyboard() {
        var entry = getCurrentTextEntry();
        if (entry == null) {
            return inlineKeyboards.getReturnMenuKeyboard();
        }
        return inlineKeyboards.getContinueKeyboard();
    }

    public List<InlineKeyboardRow> restartKeyboard() {
        return inlineKeyboards.getRestartKeyboard();
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

    private void updateMainMenuState() {
        menuState.clear();
        menuState.addAll(MAIN_MENU_STATE.getStateCommands());
        if (isAllMandatoryComplete()) {
            menuState.add(GENERATE_COMMAND);
        }
    }

    private void addTextEntryButtons() {
        entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    private void addUncompletedTextEntryButtons() {
        entries.values().stream().filter(entry -> !entry.isComplete()).forEach(entry -> entry.addButtonIfNotCompleted(menuState));
    }

    private void setAllEntriesOnWorkFalse() {
        entries.values().forEach(entry -> entry.setOnWork(false));
    }

    public boolean isAllMandatoryComplete() {
        return entries.values().stream().filter(TextEntry::isMandatory).allMatch(TextEntry::isComplete);
    }

    private boolean isAllComplete() {
        return entries.values().stream().allMatch(TextEntry::isComplete);
    }

    private TextEntry getCurrentTextEntry() {
        return entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
    }
}