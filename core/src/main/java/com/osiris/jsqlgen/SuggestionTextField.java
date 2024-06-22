package com.osiris.jsqlgen;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Overlay;
import com.osiris.desku.ui.layout.Popup;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.desku.ui.utils.NoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.osiris.desku.Statics.*;

public class SuggestionTextField extends Component<SuggestionTextField, NoValue> {
    // Local variables
    public final SortedSet<String> entries;
    public final Overlay entriesPopup;
    public final TextField textField;
    public int maxEntries = 0;

    public SuggestionTextField() {
        super(NoValue.GET);
        this.entries = new TreeSet<>();
        this.entriesPopup = new Overlay(null);
        this.textField = textfield();
        this.add(textField);
        setListener();
    }

    public SuggestionTextField(String txt) {
        this();
        this.textField.setValue(txt);
    }

    private void setListener() {
        // Add "suggestions" by changing text
        textField.onValueChange(event -> {
            String enteredText = textField.getValue();
            if (enteredText != null && !enteredText.isEmpty()) {
                showSuggestions(enteredText);
            } else {
                entriesPopup.visible(false);
            }
        });

        // Hide always by focus-out
        UI.get().registerJSListener("focusout", textField, e -> entriesPopup.visible(false));
    }

    private void showSuggestions(String enteredText) {
        List<String> filteredEntries = entries.stream()
            .filter(e -> e.toLowerCase().contains(enteredText.toLowerCase()))
            .collect(Collectors.toList());

        if (!filteredEntries.isEmpty()) {
            populatePopup(filteredEntries, enteredText);
            if (!entriesPopup.isVisible()) {
                entriesPopup.visible(true);
            }
        } else {
            entriesPopup.visible(false);
        }
    }

    private void populatePopup(List<String> searchResult, String searchRequest) {
        List<Component<?,?>> menuItems = new ArrayList<>();
        int count = searchResult.size();
        if (maxEntries > 0) {
            count = Math.min(searchResult.size(), maxEntries);
        }

        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            Text entryText = text(buildHighlightedText(result, searchRequest)).onClick(event -> {
                textField.setValue(result);
                //textField.setCaretPosition(result.length());
                textField.executeJS("" +
                    "let elem = comp; let caretPos = "+result.length()+";\n" +
                    "    if(elem != null) {\n" +
                    "        if(elem.createTextRange) {\n" +
                    "            var range = elem.createTextRange();\n" +
                    "            range.move('character', caretPos);\n" +
                    "            range.select();\n" +
                    "        }\n" +
                    "        else {\n" +
                    "            if(elem.selectionStart) {\n" +
                    "                elem.focus();\n" +
                    "                elem.setSelectionRange(caretPos, caretPos);\n" +
                    "            }\n" +
                    "            else\n" +
                    "                elem.focus();\n" +
                    "        }\n" +
                    "    }");
                entriesPopup.visible(false);
            });
            menuItems.add(entryText);
        }

        entriesPopup.removeAll();
        entriesPopup.add(vertical().add(menuItems.toArray(new Component[0])));
    }

    private String buildHighlightedText(String text, String filter) {
        int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
        String textBefore = text.substring(0, filterIndex);
        String textAfter = text.substring(filterIndex + filter.length());
        String textFilter = "<span style='color: orange; font-weight: bold;'>" + text.substring(filterIndex, filterIndex + filter.length()) + "</span>";
        return textBefore + textFilter + textAfter;
    }

    public SortedSet<String> getEntries() {
        return entries;
    }
}
