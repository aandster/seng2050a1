package me.aandster.html;

import java.util.List;

public abstract class BaseHtmlGen {

    public final String PATH_TO_JS = "/c3259753_assignment1/js/script.js";
    public final String PATH_TO_CSS = "/c3259753_assignment1/css/style.css";
    public final String PAGE_NAME;
    protected final String WEBSITE_NAME = "ABC Booking System";

    protected BaseHtmlGen(String pageName) {
        PAGE_NAME = pageName;
    }

    public String document(String bodyContentsAfterHeader) {
        return document(head(), bodyContentsAfterHeader);
    }

    public String document(String head, String innerHtml) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                head +
                body(innerHtml) +
                "</html>";
    }

    public String head() {
        return head(PAGE_NAME, PATH_TO_JS, PATH_TO_CSS);
    }

    public String head(String pageTitle, String scriptPath, String stylePath) {

        /* Generate Title tag */
        String titleTag = "";
        if (!pageTitle.isEmpty()) titleTag = "<title>" + pageTitle + "</title>";

        /* Generate Script tag for .js */
        String scriptTag = "";
        if (!scriptPath.isEmpty()) scriptTag = "<script src=\"" + scriptPath + "\"></script>";

        /* Generate Link tag for .css */
        String styleTag = "";
        if (!stylePath.isEmpty()) styleTag = "<link rel=\"stylesheet\" href=\"" + stylePath + "\">";

        /* Return, wrapped in Head tags */
        return "<head>" + titleTag + scriptTag + styleTag + "</head>";
    }

    public String body(String innerHtml) {
        String bodyHeader = heading(1, WEBSITE_NAME) + heading(2, PAGE_NAME);
        return "<body>" + bodyHeader + innerHtml + "\n</body>\n";
    }

    public String bodySection(String name, String content) {
        return heading(3, name) + "\n" + content + "\n";
    }

    public String heading(int level, String contents) {

        /* Convert heading level to String */
        String levelString = String.valueOf(level);

        /* Check heading level is 1, 2, 3, 4, 5, or 6. */
        if (level < 1 || level > 6)
            throw new IllegalArgumentException("Heading level invalid. Value was: " + levelString);

        /* Return HTML */
        return "<h" + levelString + ">" + contents + "</h" + levelString + ">\n";
    }

    public String form(String requestUrl, String requestMethod, String onSubmitJsMethod, String innerHtml) {
        return "<form action=\"" + requestUrl +
                "\" method=\"" + requestMethod +
                "\" onsubmit=\"" + onSubmitJsMethod +
                "\">" + innerHtml +
                "</form>\n";
    }

    public String formHiddenField(String name, String value, String id) {
        String html = "<input type=\"hidden\"";
        if (id != null) html += " id=\"" + id + "\"";
        html += " name=\"" + name + "\" value=\"" + value + "\">";
        return html;
    }

    //public String formTextField(String name, String label) {
    //    return "<label for=\"" + name + "\">" + label + "</label>" + "<input type=\"text\" name=\"" + name + "\" id=\"" + name + "\">\n<br>\n";
    //}

    public String formTextField(String name, String label, String value, String id, boolean isRequired) {
        String htmlOut = "<label for=\"" + id + "\">" + label + "</label>" + "<input type=\"text\" name=\"" + name + "\" id=\"" + id + "\" value=\"" + value + "\"";
        if (isRequired) htmlOut += " required";
        htmlOut += ">\n<br>\n";
        return htmlOut;
    }

    //public String formTextField(String name, String label, String value, String id) {
    //    return formTextField(name, label, value, id, false);
    //}

    public String formTextFieldDisabled(String name, String label, String value, String id) {
        return "<label for=\"" + id + "\">" + label + "</label>" + "<input type=\"text\" id=\"" + id + "\" name=\"" + name + "\" value=\"" + value + "\" disabled>\n<br>\n";
    }

    //public String formDropdownBox(String name, String label, List<String> options) {
    //    return formDropdownBox(name, label, options, "");
    //}

    public String formDropdownBox(String name, String label, List<String> options, String selected, String id) {
        if (options == null) throw new IllegalArgumentException("options argument cannot be null");

        /* Generate HTML for dropdown box options */
        StringBuilder optionsInnerHtml = new StringBuilder();
        for (String optionValue : options) {
            boolean isSelected = selected != null && !selected.isEmpty() && selected.equals(optionValue);
            optionsInnerHtml.append(formDropdownBoxOption(optionValue, optionValue, isSelected));
        }

        /* Generate the dropdownbox and label, and inject the options */
        return "<label for=\"" + id + "\">" + label + "</label>\n"
                + "<select name=\"" + name
                + "\" id=\"" + name + "\">\n"
                + optionsInnerHtml
                + "</select>\n";
    }

    public String formDropdownBoxOption(String value, String label) {
        return formDropdownBoxOption(value, label, false);
    }

    public String formDropdownBoxOption(String value, String label, boolean isSelected) {
        String out = "<option value=\"" + value + "\"";
        if (isSelected) out += " selected";
        out += ">" + label + "</option>\n";
        return out;
    }

    public String formDatePicker(String name, String label) {
        return "<label for=\"" + name + "\">" + label + "</label>\n<input type=\"date\" id=\"" + name + "\" name=\"" + name + "\">\n";
    }

    public String formDatePicker(String name, String label, String value) {
        return "<label for=\"" + name + "\">" + label + "</label>\n<input type=\"date\" id=\"" + name + "\" name=\"" + name + "\" value=\"" + value + "\">\n";
    }

    public String formSubmitButton(String label) {
        return "<input type=\"submit\" value=\"" + label + "\">\n";
    }

    public String formResetButton(String label) {
        return "<input type=\"reset\" value=\"" + label + "\">\n";
    }

    public String paragraph(String contents) {
        return "<p>" + contents + "</p>\n";
    }

    public String lineBreak() {
        return "<br>\n";
    }

    public String unorderedList(String... elements) {

        String output = "<ul>\n";
        for (String e : elements) output += "<li>" + e + "</li>\n";
        output += "</ul>\n";

        return output;
    }

    public String link(String text, String url) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    public String bold(String text) {
        return "<b>" + text + "</b>";
    }

    public String table(String innerHtml) {
        return "<table>\n" + innerHtml + "</table>\n";
    }

    public String tableRow(String innerHtml) {
        return "<tr>" + innerHtml + "</tr>\n";
    }

    public String tableDataCell(String innerHtml, String cssClass) {
        return "<td class=\"" + cssClass + "\">" + innerHtml + "</td>";
    }

    public String tableHeaderCell(String innerHtml) {
        return "<th>" + innerHtml + "</th>";
    }


}
