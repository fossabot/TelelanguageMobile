package com.telelanguage.tlvx.client;

import java.awt.Event;
import java.util.List;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.telelanguage.tlvx.model.Department;

public class AdditionalQuestion extends FlowPanel {
	TextBox textField = new TextBox();
	MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	SuggestBox departmentCombo = new SuggestBox(oracle);
	List<Department> departments = null;
	String label;
	static public Hint hint = new Hint();

	Button questionMark = new Button();
	
	public AdditionalQuestion(String label, String placeholder, final String question, final List<Department> departments, String value) {
		setStyleName("form-wrap-p");
		this.departments = departments;
		this.label = label;
		textField.getElement().setAttribute("placeholder", placeholder);
		textField.setMaxLength(50);
		add(textField);
		textField.setStyleName("s3");
		questionMark.setStyleName("validicon fa fa-hand-o-left");
		questionMark.setTitle(question);
		add(questionMark);
		if (!"Department".equalsIgnoreCase(label)) {
			if (value != null) textField.setValue(value);
			textField.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					Tlvx.checkCompleteCallStatus();
				}
			});
			textField.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == Event.TAB) {
						Tlvx.checkCompleteCallStatus();
						Tlvx.focusNextInvalidQuestion();
						event.stopPropagation();
						event.preventDefault();
					}
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						Tlvx.focusNextInvalidQuestion();
						event.stopPropagation();
						event.preventDefault();
					}
				}
			});
			textField.addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					hint.setToolTop(textField.getElement(), question);
				}
			});
		} else {
			remove(textField);
			departmentCombo.setStyleName("s3");
			departmentCombo.getElement().setAttribute("placeholder", placeholder);
			insert(departmentCombo, 0);
			for (Department department : departments) {
				oracle.add(department.departmentName);
			}
			if (value != null) setValue(value);
			departmentCombo.addSelectionHandler(new SelectionHandler<Suggestion>() {
				@Override
				public void onSelection(SelectionEvent<Suggestion> event) {
					//Tlvx.checkCompleteCallStatus();
					//Tlvx.focusNextInvalidQuestion();
				}
			});
			departmentCombo.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					Tlvx.checkCompleteCallStatus();
					//Tlvx.focusNextInvalidQuestion();
				}
			});
			departmentCombo.getValueBox().addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					Tlvx.checkCompleteCallStatus();
				}
			});
			departmentCombo.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeEvent().getKeyCode() == 13 || event.getNativeEvent().getKeyCode() == Event.TAB) {
						Tlvx.checkCompleteCallStatus();
						Tlvx.focusNextInvalidQuestion();
						event.stopPropagation();
						event.preventDefault();
					}
				}
			});
			departmentCombo.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeEvent().getKeyCode() == 13 || event.getNativeEvent().getKeyCode() == Event.TAB) {
						event.stopPropagation();
						event.preventDefault();
					} else {
						Tlvx.checkCompleteCallStatus();
						//Tlvx.focusNextInvalidQuestion();
					}
				}
			});
			departmentCombo.getValueBox().addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					hint.setToolTop(departmentCombo.getElement(), question);
				}
			});
		}

	}
	
	public String getText() {
		return textField.getText();
	}
	
	public String getLabel() {
		return label;
	}

	public String getValue() {
		if (departments != null) {
			if (!isValid()) return "";
			String value = departmentCombo.getValue();
			if (value != null && value.contains(" ")) value = value.substring(0, value.indexOf(" "));
			return value;
		}
		return textField.getValue();
	}

	public void setValue(String value) {
		//boolean valid = false;
		if (departments != null && value != null) {
			for (int i=0; i<departments.size(); i++) {
				Department dept = departments.get(i);
				if (value.equalsIgnoreCase(dept.departmentValue)) {
					departmentCombo.setValue(dept.departmentName);
					//deptValue.setText(dept.departmentName);
					//valid = true;
				}
			}
			//if (!valid) deptValue.setText("");
		}
		textField.setValue(value);
	}

	public boolean isValid() {
		//if (departmentCombo != null) return departmentCombo.isValid();
		if (departments != null) {
			//String deptCode = textField.getValue();
			String deptCode = departmentCombo.getValue();
			if (deptCode != null && deptCode.contains(" ")) deptCode = deptCode.substring(0, deptCode.indexOf(" "));
			if (deptCode != null) {
				for (int i=0; i<departments.size(); i++) {
					Department dept = departments.get(i);
					if (deptCode.equalsIgnoreCase(dept.departmentValue)) {
						questionMark.setStyleName("validicon fa fa-check");
						return true;
					}
				}
			}
			questionMark.setStyleName("validicon fa fa-hand-o-left");
			return false;
		}
		if (textField.getValue().length()>0) {
			questionMark.setStyleName("validicon fa fa-check");
			return true;
		} else {
			questionMark.setStyleName("validicon fa fa-hand-o-left");
			return false;
		}
	}

	public void focus() {
		if (departments != null) {
			departmentCombo.setFocus(true);
		} else {
			textField.setFocus(true);
		}
	}
}
