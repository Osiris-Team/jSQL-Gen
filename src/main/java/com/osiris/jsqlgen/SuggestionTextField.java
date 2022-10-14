package com.osiris.jsqlgen;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author https://stackoverflow.com/a/40369435
 */
public class SuggestionTextField extends TextField {
    //Local variables
    //entries to autocomplete
    private final SortedSet<String> entries;
    //popup GUI
    private final ContextMenu entriesPopup;
    /**
     * The maximum amount of entries to show.
     * No limit if set to a negative number or 0 (default).
     */
    public int maxEntries = 0;


    public SuggestionTextField() {
        super();
        this.entries = new TreeSet<>();
        this.entriesPopup = new ContextMenu();

        setListener();
    }


    public SuggestionTextField(String txt) {
        this();
        setText(txt);
    }

    /**
     * Build TextFlow with selected text. Return "case" dependent.
     *
     * @param text   - string with text
     * @param filter - string to select in text
     * @return - TextFlow
     */
    public static TextFlow buildTextFlow(String text, String filter) {
        int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
        Text textBefore = new Text(text.substring(0, filterIndex));
        Text textAfter = new Text(text.substring(filterIndex + filter.length()));
        Text textFilter = new Text(text.substring(filterIndex, filterIndex + filter.length())); //instead of "filter" to keep all "case sensitive"
        textFilter.setFill(Color.ORANGE);
        textFilter.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        return new TextFlow(textBefore, textFilter, textAfter);
    }

    /**
     * "Suggestion" specific listners
     */
    private void setListener() {
        //Add "suggestions" by changing text
        textProperty().addListener((observable, oldValue, newValue) -> {
            String enteredText = getText();
            //always hide suggestion if nothing has been entered (only "spacebars" are dissalowed in TextFieldWithLengthLimit)
            if (enteredText != null && !enteredText.isEmpty()) {
                showSuggestions(enteredText);
            }
        });

        //Hide always by focus-in (optional) and out
        focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                showSuggestions("");
            } else {
                entriesPopup.hide();
            }
        });
    }

    private void showSuggestions(String enteredText) {
        //filter all possible suggestions depends on "Text", case insensitive
        List<String> filteredEntries = entries.stream()
                .filter(e -> e.toLowerCase().contains(enteredText.toLowerCase()))
                .collect(Collectors.toList());
        //some suggestions are found
        if (!filteredEntries.isEmpty()) {
            //build popup - list of "CustomMenuItem"
            populatePopup(filteredEntries, enteredText);
            if (!entriesPopup.isShowing()) { //optional
                entriesPopup.show(SuggestionTextField.this, Side.BOTTOM, 0, 0); //position of popup
            }
            //no suggestions -> hide
        } else {
            entriesPopup.hide();
        }
    }

    /**
     * Populate the entry set with the given search results. Display is limited to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult, String searchReauest) {
        //List of "suggestions"
        List<CustomMenuItem> menuItems = new LinkedList<>();
        //List size - 10 or founded suggestions count
        int count = searchResult.size();
        if (maxEntries > 0) {
            count = Math.min(searchResult.size(), maxEntries);
        }
        //Build list as set of labels
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            //label with graphic (text flow) to highlight founded subtext in suggestions
            Label entryLabel = new Label();
            entryLabel.setGraphic(buildTextFlow(result, searchReauest));
            entryLabel.setPrefHeight(10);  //don't sure why it's changed with "graphic"
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            menuItems.add(item);

            //if any suggestion is select set it into text and close popup
            item.setOnAction(actionEvent -> {
                setText(result);
                positionCaret(result.length());
                entriesPopup.hide();
            });
        }

        //"Refresh" context menu
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedSet<String> getEntries() {
        return entries;
    }
}