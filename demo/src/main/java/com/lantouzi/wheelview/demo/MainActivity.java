package com.lantouzi.wheelview.demo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lantouzi.wheelview.R;
import com.view.picker.HorizontalWheelPicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private HorizontalWheelPicker mWheelView, mWheelView2, mWheelView3, mWheelView4, mWheelView5;
	private TextView mSelectedTv, mChangedTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mWheelView = findViewById(R.id.wheelview);
		mWheelView2 = findViewById(R.id.wheelview2);
		mWheelView3 = findViewById(R.id.wheelview3);
		mWheelView4 = findViewById(R.id.wheelview4);
		mWheelView5 = findViewById(R.id.wheelview5);
		mSelectedTv = findViewById(R.id.selected_tv);
		mChangedTv = findViewById(R.id.changed_tv);

		final List<String> items = new ArrayList<>();
		for (int i = 1; i <= 50; i++) {
			items.add(String.valueOf(i * 1000));
		}

		mWheelView.setItems(items);
		mWheelView.selectIndex(49);
//		mWheelView.setAdditionCenterMark(WheelView.RUPEE);

		List<String> items2 = new ArrayList<>();
		items2.add("一月");
		items2.add("二月");
		items2.add("三月");
		items2.add("四月");
		items2.add("五月");
		items2.add("六月");
		items2.add("七月");
		items2.add("八月");
		items2.add("九月");
		items2.add("十月");
		items2.add("十一月");
		items2.add("十二月");

		mWheelView2.setItems(items2);

		List<String> items3 = new ArrayList<>();
		items3.add("1");
		items3.add("2");
		items3.add("3");
		items3.add("5");
		items3.add("7");
		items3.add("11");
		items3.add("13");
		items3.add("17");
		items3.add("19");
		items3.add("23");
		items3.add("29");
		items3.add("31");

		mWheelView3.setItems(items3);
		mWheelView3.setAdditionCenterMark("m");

//		mWheelView4.setItems(items);
//		mWheelView4.setEnabled(false);

		mWheelView5.setItems(items);
		mWheelView5.setMinSelectableIndex(3);
		mWheelView5.setMaxSelectableIndex(items.size() - 3);

		items.remove(items.size() - 1);
		items.remove(items.size() - 2);
		items.remove(items.size() - 3);
		items.remove(items.size() - 4);

		mSelectedTv.setText(String.format("onWheelItemSelected：%1$s", ""));
		mChangedTv.setText(String.format("onWheelItemChanged：%1$s", ""));

		mWheelView5.setOnWheelItemSelectedListener(new HorizontalWheelPicker.OnWheelItemSelectedListener() {
			@Override
			public void onWheelItemSelected(HorizontalWheelPicker wheelView, int position) {
				mSelectedTv.setText(String.format("onWheelItemSelected：%1$s", wheelView.getItems().get(position)));
			}

			@Override
			public void onWheelItemChanged(HorizontalWheelPicker wheelView, int position) {
				mChangedTv.setText(String.format("onWheelItemChanged：%1$s", wheelView.getItems().get(position)));
			}
		});

		mWheelView4.postDelayed(new Runnable() {
			@Override
			public void run() {
				mWheelView4.setItems(items);
			}
		}, 3000);

	}
}
