package com.windanesz.sisyphus.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiConfigEntries;

public final class ConfigScreenLayout {

    static final int MARGIN = 8;

    static final int MIN_CONTROL_WIDTH = 80;

    static final int SCROLL_BAR_WIDTH = 6;

    static final int LABEL_GAP = 8;

    static final int CONTROL_END_TO_SCROLL_BAR = 50;

    private ConfigScreenLayout() {
    }

    static Columns fit(int screenWidth, int labelX, int maxLabelWidth, int scrollBarX) {
        int rightEdge = scrollBarX + SCROLL_BAR_WIDTH;
        if (labelX >= MARGIN && rightEdge <= screenWidth - MARGIN) {
            return null;
        }
        int shift = screenWidth - MARGIN - rightEdge;
        int controlEnd = scrollBarX + shift - CONTROL_END_TO_SCROLL_BAR;
        int controlX = Math.min(MARGIN + maxLabelWidth + LABEL_GAP, controlEnd - MIN_CONTROL_WIDTH);
        return new Columns(MARGIN, controlX, shift);
    }

    public static void apply(GuiConfigEntries list) {
        Columns columns = fit(list.owningScreen.width, list.labelX, list.maxLabelTextWidth, list.scrollBarX);
        if (columns == null) {
            return;
        }
        list.labelX = columns.labelX;
        list.controlX = columns.controlX;
        list.resetX += columns.shift;
        list.scrollBarX += columns.shift;
        list.maxEntryRightBound += columns.shift;
        // The width of the buttons as Forge works it out from the same bounds.
        list.controlWidth = list.maxEntryRightBound - list.controlX - 45;
    }

    public static String trimLabel(FontRenderer font, String label, GuiConfigEntries list) {
        int room = list.controlX - LABEL_GAP - list.labelX;
        if (room <= 0 || font.getStringWidth(label) <= room) {
            return label;
        }
        String ellipsis = "...";
        return font.trimStringToWidth(label, room - font.getStringWidth(ellipsis)) + ellipsis;
    }

    static final class Columns {

        final int labelX;
        final int controlX;
        final int shift;

        Columns(int labelX, int controlX, int shift) {
            this.labelX = labelX;
            this.controlX = controlX;
            this.shift = shift;
        }
    }
}
