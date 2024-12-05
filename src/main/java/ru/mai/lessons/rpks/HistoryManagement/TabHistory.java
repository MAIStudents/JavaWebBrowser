package ru.mai.lessons.rpks.HistoryManagement;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class TabHistory {
    private final Deque<String> history = new LinkedList<>();
    private final Deque<String> futureHistory = new LinkedList<>();

    // after something can go back
    // when go back until new action can go future


}
