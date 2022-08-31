package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.stream.Stream;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Chapter;
import com.minelittlepony.unicopia.container.SpellbookChapterList.TabSide;

import net.minecraft.util.Identifier;

public class SpellbookTabBar {

    private final SpellbookScreen screen;
    private final SpellbookChapterList chapters;

    private final Map<TabSide, List<Tab>> bars = new EnumMap<>(TabSide.class);

    public SpellbookTabBar(SpellbookScreen screen, SpellbookChapterList chapters) {
        this.screen = screen;
        this.chapters = chapters;
    }

    public void init() {
        bars.clear();
        Stream.of(TabSide.values()).forEach(side -> {
            bars.put(side, buildTabList(side));
        });
    }

    public Stream<Tab> getAllTabs() {
        return bars.values().stream().flatMap(l -> l.stream());
    }

    private List<Tab> buildTabList(TabSide side) {
        List<Tab> tabs = new ArrayList<>();
        int backgroundHeight = screen.getBackgroundHeight();
        int backgroundWidth = screen.getBackgroundWidth();

        int tabInset = 14;

        int top = (screen.height - backgroundHeight) / 2 + 20;
        int left = (screen.width - backgroundWidth) / 2;

        List<Chapter> leftTabs = chapters.getTabs(side)
                .sorted(Comparator.comparing(Chapter::tabY))
                .toList();


        int tabWidth = 35;
        int tabHeight = 23;

        int totalTabs = leftTabs.size();
        float squashFactor = Math.min(1, (float)(backgroundHeight - 40) / ((tabHeight + 8) * leftTabs.size()));

        for (int i = 0; i < totalTabs; i++) {
            int tabY = (int)((leftTabs.get(i).tabY() * tabHeight + (i * 5)) * squashFactor);
            int width = tabWidth;
            if (leftTabs.get(i) == chapters.getCurrentChapter()) {
                width += 3;
            }
            int xPosition = side == TabSide.LEFT ? left - width + tabInset : left + backgroundWidth - tabInset;

            tabs.add(new Tab(leftTabs.get(i), new Bounds(top + tabY, xPosition, width, tabHeight), leftTabs.get(i).icon()));
        }

        return tabs;
    }

    record Tab (Chapter chapter, Bounds bounds, Identifier icon) {}
}
