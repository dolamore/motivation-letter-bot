//package com.test.motivationletterbot;
//
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class KeyboardFactory {
//    public ReplyKeyboardMarkup buildSingleButtonKeyboard(String buttonText) {
//        KeyboardButton button = new KeyboardButton(buttonText);
//        KeyboardRow row = new KeyboardRow();
//        row.add(button);
//        List<KeyboardRow> keyboard = new ArrayList<>();
//        keyboard.add(row);
//
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        keyboardMarkup.setKeyboard(keyboard);
//        keyboardMarkup.setResizeKeyboard(true);
//        keyboardMarkup.setOneTimeKeyboard(true);
//        return keyboardMarkup;
//    }
//}
//
