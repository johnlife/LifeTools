package ru.johnlife.lifetools.ui;

import ru.johnlife.lifetools.R;
import android.view.View;
import android.widget.Button;

public class ErrorPane extends ActionPane {

	public ErrorPane(View anySibling, final int titleResourceId, final String message) {
		super(anySibling);
		Button button = getActionButton();
		button.setText(R.string.button_dismiss);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		getMessage().setText(titleResourceId);
		getDetails().setText(message);
	}
}
