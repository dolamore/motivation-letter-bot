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
    private final ReentrantLock lock = new ReentrantLock();

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
        lock.lock();
        try {
            menuState.clear();
            menuState.addAll(MAIN_MENU_STATE.getStateCommands());
            entries.values().forEach(TextEntry::reset);
            entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
        } finally {
            lock.unlock();
        }
    }

    public void returnToMenu() {
        lock.lock();
        try {
            entries.values().forEach(entry -> entry.setOnWork(false));
            menuState.clear();
            menuState.addAll(MAIN_MENU_STATE.getStateCommands());
            entries.values().forEach(entry -> entry.addButtonIfNotCompleted(menuState));
        } finally {
            lock.unlock();
        }
    }

    public void startWriting(TextEntryType textEntryType) {
        lock.lock();
        try {
            entries.values().forEach(entry -> entry.setOnWork(false));
            var entry = entries.get(textEntryType);
            entry.startWriting();
            menuState.clear();
            menuState.addAll(MAIN_MENU_STATE.getStateCommands());
            entries.values().stream().filter(e -> !e.isOnWork()).forEach(e -> e.addButtonIfNotCompleted(menuState));
        } finally {
            lock.unlock();
        }
    }

    public void continueWriting() {
        lock.lock();
        try {
            var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
            if (entry == null) {
                return;
            }
            menuState.clear();
            menuState.addAll(MAIN_MENU_STATE.getStateCommands());
            menuState.add(SUBMIT_COMMAND);
            entries.values().forEach(e -> e.addButtonIfNotCompleted(menuState));
        } finally {
            lock.unlock();
        }
    }

    public void completeTextEntry() {
        lock.lock();
        try {
            var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
            if (entry == null) {
                return;
            }
            entry.complete();
            menuState.clear();
            menuState.addAll(MAIN_MENU_STATE.getStateCommands());
            entries.values().forEach(e -> e.addButtonIfNotCompleted(menuState));
        } finally {
            lock.unlock();
        }
    }

    public String writeMessage() {
        lock.lock();
        try {
            var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().get();
            return entry.getWriteMessage();
        } finally {
            lock.unlock();
        }
    }

    public String continueMessage() {
        lock.lock();
        try {
            var entry = entries.values().stream().filter(TextEntry::isOnWork).findFirst().orElse(null);
            if (entry == null) {
                return "This command is not available at the moment.";
            }
            return entry.getContinueMessage();
        } finally {
            lock.unlock();
        }
    }

    public String startingMessage() {
        return STARTING_MESSAGE;
    }

    public String menuMessage() {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    private boolean isAllMandatoryComplete() {
        lock.lock();
        try {
            return entries.values().stream().filter(TextEntry::isMandatory).allMatch(TextEntry::isComplete);
        } finally {
            lock.unlock();
        }
    }

    private boolean isAllComplete() {
        lock.lock();
        try {
            return entries.values().stream().allMatch(TextEntry::isComplete);
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            return entries.get(type).isOnWork();
        } finally {
            lock.unlock();
        }
    }

    public void addText(TextEntryType type, String text) {
        lock.lock();
        try {
            entries.get(type).append(text);
            menuState.add(SUBMIT_COMMAND);
        } finally {
            lock.unlock();
        }
    }

    public String greetingMessage() {
        return GREETING_MESSAGE;
    }
}