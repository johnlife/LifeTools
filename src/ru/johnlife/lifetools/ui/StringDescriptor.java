package ru.johnlife.lifetools.ui;

import android.content.Context;
import android.widget.TextView;

public class StringDescriptor {
	private int id;
	private String content;
	private boolean expanded = false;
	
	public StringDescriptor(String string) {
		this.content = string;
		this.expanded = true;
	}
	
	public StringDescriptor(int resourceId) {
		this.id = resourceId;
	}
	
	public void apply(TextView view) {
		if (expanded) {
			view.setText(content);
		} else {
			view.setText(id);
		}
	}
	
	
	public String toString(Context context) {
		if (expanded) {
			return content;
		} else {
			return context.getString(id);
		}
		
	}
}
