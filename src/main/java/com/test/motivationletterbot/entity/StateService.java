//package com.test.motivationletterbot.entity;
//
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.generics.TelegramClient;
//
//@Service
//@AllArgsConstructor
//public class StateService {
//    private final TelegramClient telegramClient;
//    private final CommandService commandService;
//
//    public void setNewState(long chatId, BotStatesEnum state) {
//        commandService.setBotCommands(chatId, state.getCommands());
////        try {
////            telegramClient.execute();
////        } catch (TelegramApiException e) {
////            throw new RuntimeException("Failed to set bot state", e);
////        }
//    }
//}