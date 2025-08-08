package com.test.motivationletterbot.entity.textentry;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static com.test.motivationletterbot.entity.textentry.TextEntryTypeParams.*;

@AllArgsConstructor
public enum TextEntryType {

    VACANCY_TEXT_ENTRY(VACANCY_TEXT_ENTRY_PARAMS) {

        @Override
        public String getWriteMessage() {
            return "Please write the role description.";
        }

        @Override
        public String getContinueMessage() {
            return "Please continue writing the role description or submit it.";
        }

        @Override
        public String getWriteCompletedMessage() {
            return getWriteCompletedMessage("role description");
        }

        @Override
        public String getContinueCompletedMessage() {
            return getContinueCompletedMessage("role description");
        }

        @Override
        public String getMenuMessage() {
            return "Vacancy text\n";
        }

        @Override
        public CommandsEnum getMainMenuCommand() {
            return CommandsEnum.WRITE_ROLE_DESCRIPTION_COMMAND;
        }

        @Override
        public CommandsEnum getRewriteCommand() {
            return CommandsEnum.REWRITE_ROLE_DESCRIPTION_COMMAND;
        }
    },

    MOTIVATION_TEXT_ENTRY(MOTIVATION_TEXT_ENTRY_PARAMS) {

        @Override
        public String getWriteMessage() {
            return "Please write your motivation letter.";
        }

        @Override
        public String getContinueMessage() {
            return "Please continue writing your motivation letter or submit it.";
        }

        @Override
        public String getWriteCompletedMessage() {
            return getWriteCompletedMessage("motivation");
        }

        @Override
        public String getContinueCompletedMessage() {
            return getContinueCompletedMessage("motivation");
        }

        @Override
        public String getMenuMessage() {
            return "(OPTIONALLY) your motivation text\n";
        }

        @Override
        public CommandsEnum getMainMenuCommand() {
            return CommandsEnum.WRITE_MOTIVATION_COMMAND;
        }

        @Override
        public CommandsEnum getRewriteCommand() {
            return CommandsEnum.REWRITE_MOTIVATION_COMMAND;
        }
    },

    ADDITIONAL_INFORMATION_TEXT_ENTRY(ADDITIONAL_INFORMATION_TEXT_ENTRY_PARAMS) {

        @Override
        public String getWriteMessage() {
            return "Please write any additional information about the product or company.";
        }

        @Override
        public String getContinueMessage() {
            return "Please continue writing additional information or submit it.";
        }

        @Override
        public String getWriteCompletedMessage() {
            return getWriteCompletedMessage("additional information");
        }

        @Override
        public String getContinueCompletedMessage() {
            return getContinueCompletedMessage("additional information");
        }

        @Override
        public String getMenuMessage() {
            return "(OPTIONALLY) Information about product/company\n";
        }

        @Override
        public CommandsEnum getMainMenuCommand() {
            return CommandsEnum.WRITE_ADDITIONAL_INFORMATION_COMMAND;
        }

        @Override
        public CommandsEnum getRewriteCommand() {
            return CommandsEnum.REWRITE_ADDITIONAL_INFORMATION_COMMAND;
        }
    };

    private final TextEntryTypeParams params;

    public InlineKeyboardRow getKeyboardRow() {
        return params.keyboardRow();
    }

    public boolean isMandatory() {
        return params.isMandatory();
    }

    public abstract CommandsEnum getMainMenuCommand();

    public abstract CommandsEnum getRewriteCommand();

    public abstract String getMenuMessage();

    public abstract String getWriteMessage();

    public abstract String getContinueMessage();

    public abstract String getWriteCompletedMessage();

    public abstract String getContinueCompletedMessage();

    static String getWriteCompletedMessage(String entryName) {
        return "Your " + entryName + " is complete. You are just adding the new version now. You can safely return to main menu or write the new text.";
    }

    static String getContinueCompletedMessage(String entryName) {
        return "You are in the process of rewriting the completed " + entryName + ". You can safely return to main menu, or submit the new version.";
    }
}