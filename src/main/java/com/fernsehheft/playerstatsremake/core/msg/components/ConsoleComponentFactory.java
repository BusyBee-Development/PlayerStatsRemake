package com.fernsehheft.playerstatsremake.core.msg.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public final class ConsoleComponentFactory extends ComponentFactory {

    public ConsoleComponentFactory() {
        super();
    }

    @Override
    public boolean isConsoleFactory() {
        return true;
    }

    @Override
    public TextComponent heart() {
        return Component.text()
                .content(String.valueOf('\u2665'))
                .color(HEARTS)
                .build();
    }
}