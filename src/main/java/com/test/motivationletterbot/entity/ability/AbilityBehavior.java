package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import java.util.function.Consumer;
import java.util.function.Function;

public record AbilityBehavior(
    Consumer<UserSession> sessionAction,
    Function<UserSession, String> message,
    Function<UserSession, java.util.List<InlineKeyboardRow>> inlineKeyboardSupplier
) {}

