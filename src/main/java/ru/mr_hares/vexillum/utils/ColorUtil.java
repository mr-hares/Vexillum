package ru.mr_hares.vexillum.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.mr_hares.vexillum.Vexillum.*;

public final class ColorUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern AMP_HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern BRACE_HEX = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    private static final Pattern AMP_X_HEX = Pattern.compile("&x(?:&([A-Fa-f0-9])){6}", Pattern.CASE_INSENSITIVE);
    private static final Pattern BARE_HEX = Pattern.compile("(?<![&\\w#<:/])#([A-Fa-f0-9]{6})(?![A-Fa-f0-9])");
    private static final Pattern LEGACY = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    private ColorUtil() {
    }

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        if (getML().getYaml() != null)
            text = text.replace("{prefix}", getML().getString("prefix"));

        return MINI.deserialize(toMiniMessage(text));
    }

    public static List<Component> parse(List<String> lines) {
        List<Component> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (getML().getYaml() != null)
                line = line.replace("{prefix}", getML().getString("prefix"));
            result.add(parse(line));
        }
        return result;
    }

    public static Component parseNoItalic(String text) {
        return Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(parse(text));
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(parse(text));
    }

    public static List<String> colorize(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }

    public static String strip(String text) {
        return PlainTextComponentSerializer.plainText().serialize(parse(text));
    }

    public static String toMiniMessage(String text) {
        if (text == null) return "";

        text = text.replace('\u00A7', '&');

        Matcher m = AMP_X_HEX.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String raw = m.group().substring(2).replace("&", "");
            m.appendReplacement(sb, Matcher.quoteReplacement(hexTag(raw)));
        }
        m.appendTail(sb);
        text = sb.toString();

        text = replaceHex(text, AMP_HEX);
        text = replaceHex(text, BRACE_HEX);
        text = replaceHex(text, BARE_HEX);

        m = LEGACY.matcher(text);
        sb = new StringBuffer();
        while (m.find()) {
            char code = Character.toLowerCase(m.group(1).charAt(0));
            m.appendReplacement(sb, Matcher.quoteReplacement(legacyToTag(code)));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    private static String replaceHex(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(hexTag(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String hexTag(String hex) {
        return "<reset><#" + hex.toLowerCase() + ">";
    }

    private static String legacyToTag(char code) {
        switch (code) {
            case '0':
                return "<reset><black>";
            case '1':
                return "<reset><dark_blue>";
            case '2':
                return "<reset><dark_green>";
            case '3':
                return "<reset><dark_aqua>";
            case '4':
                return "<reset><dark_red>";
            case '5':
                return "<reset><dark_purple>";
            case '6':
                return "<reset><gold>";
            case '7':
                return "<reset><gray>";
            case '8':
                return "<reset><dark_gray>";
            case '9':
                return "<reset><blue>";
            case 'a':
                return "<reset><green>";
            case 'b':
                return "<reset><aqua>";
            case 'c':
                return "<reset><red>";
            case 'd':
                return "<reset><light_purple>";
            case 'e':
                return "<reset><yellow>";
            case 'f':
                return "<reset><white>";
            case 'k':
                return "<obfuscated>";
            case 'l':
                return "<bold>";
            case 'm':
                return "<strikethrough>";
            case 'n':
                return "<underlined>";
            case 'o':
                return "<italic>";
            case 'r':
                return "<reset>";
            default:
                return "";
        }
    }
}
