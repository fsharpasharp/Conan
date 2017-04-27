package view;

import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;

// Only GUI no logic"
public class RulePane extends FlowPane {
    private TextField rule;
    private ArrayList<TextField> rulePrompts;
    public TextField getRulePrompt(int i) {
        return rulePrompts.get(i);
    }
    public TextField getRule() {
        return rule;
    }
    public RulePane() {
        rulePrompts = new ArrayList<TextField>(3);
        this.setMaxWidth(340);
        rule = new TextField();
        rule.getStyleClass().add("myText");
        rule.setPromptText("Rule");
        rule.setId("rightTextField");
        rule.setMaxWidth(100);
        for (int i = 0; i < 3; i++) {
            TextField temp = new TextField();
            temp.setVisible(false);
            temp.getStyleClass().add("myText");
            temp.setMaxWidth(80);
            rulePrompts.add(temp);
        }
        this.getChildren().addAll(rule, rulePrompts.get(0), rulePrompts.get(1), rulePrompts.get(2));
    }
}
