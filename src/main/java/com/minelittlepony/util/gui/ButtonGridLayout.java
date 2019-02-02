package com.minelittlepony.util.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.gui.GuiButton;

public class ButtonGridLayout {

    private List<GuiButton> elements;

    private Map<Integer, List<GuiButton>> leftLookup = new HashMap<>();
    private Map<Integer, List<GuiButton>> topLookup = new HashMap<>();

    private Map<Integer, List<GuiButton>> idLookup = new HashMap<>();

    private int nextButtonId = -1;

    public ButtonGridLayout(List<GuiButton> elements) {
        elements.forEach(this::addElement);
        this.elements = elements;
    }

    public int getNextButtonId() {
        return nextButtonId++;
    }

    public GuiButton addElement(GuiButton button) {
        if (elements != null) {
            elements.add(button);
        }
        getColumn(button.x).add(button);
        getRow(button.y).add(button);
        getButton(button.id).add(button);

        while (button.id >= nextButtonId) {
            getNextButtonId();
        }

        return button;
    }

    public List<GuiButton> getElements() {
        return elements;
    }

    public List<GuiButton> getRow(int y) {
        return topLookup.computeIfAbsent(y, ArrayList::new);
    }

    public List<GuiButton> getColumn(int x) {
        return leftLookup.computeIfAbsent(x, ArrayList::new);
    }

    public List<GuiButton> getButton(int id) {
        return idLookup.computeIfAbsent(id, ArrayList::new);
    }

    public Stream<Integer> getButtonIds() {
        return idLookup.keySet().stream().sorted();
    }

    public Stream<Integer> getColumns() {
        return leftLookup.keySet().stream().sorted();
    }

    public Stream<Integer> getRows() {
        return topLookup.keySet().stream().sorted();
    }

    public Stream<Integer> getIds() {
        return idLookup.keySet().stream().sorted();
    }

    public static <T> List<T> list(Stream<T> s) {
        return s.collect(Collectors.toList());
    }

    public static <T> T first(List<T> c, int i) {
        return c.get(i);
    }

    public static <T> T last(List<T> c, int i) {
        return first(c, (c.size() - 1) - i);
    }
}
