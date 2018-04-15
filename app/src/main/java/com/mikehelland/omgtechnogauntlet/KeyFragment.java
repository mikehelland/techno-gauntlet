package com.mikehelland.omgtechnogauntlet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mikehelland.omgtechnogauntlet.jam.KeyHelper;

import java.util.Arrays;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class KeyFragment extends OMGFragment {

    private Button mKeyButton;
    private Button mScaleButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.key_fragment,
                container, false);

        ViewGroup keysLayout = (ViewGroup)view.findViewById(R.id.list_of_keys);
        ViewGroup scalesLayout = (ViewGroup)view.findViewById(R.id.list_of_scales);

        makeKeyButtons(keysLayout);
        makeScaleButtons(scalesLayout);

        return view;
    }

    private void makeKeyButtons(ViewGroup list) {
        for (int i = 0; i <  KeyHelper.KEY_CAPTIONS.length; i++) {
            list.addView(makeKeyButton(i, KeyHelper.KEY_CAPTIONS[i]));
        }
    }
    private void makeScaleButtons(ViewGroup list) {
        for (int i = 0; i <  KeyHelper.SCALE_CAPTIONS.length; i++) {
            list.addView(makeScaleButton(i, KeyHelper.SCALE_CAPTIONS[i]));
        }
    }
    private View makeKeyButton(final int i, String caption) {
        final Button button = new Button(getActivity());
        button.setBackgroundColor(Color.WHITE);
        button.setTextColor(Color.BLACK);
        button.setTextSize(22);
        button.setText(caption);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mKeyButton != null) {
                    mKeyButton.setBackgroundColor(Color.WHITE);
                }
                mKeyButton = button;
                mKeyButton.setBackgroundColor(Color.GREEN);
                getJam().setKey(i, null);
            }
        });

        if (getJam().getKey() == i) {
            mKeyButton = button;
            mKeyButton.setBackgroundColor(Color.GREEN);
        }

        return button;
    }
    private View makeScaleButton(final int i, String caption) {
        final Button button = new Button(getActivity());
        button.setBackgroundColor(Color.WHITE);
        button.setTextColor(Color.BLACK);
        button.setTextSize(24);
        button.setText(caption);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScaleButton != null)
                    mScaleButton.setBackgroundColor(Color.WHITE);
                mScaleButton = button;
                mScaleButton.setBackgroundColor(Color.GREEN);
                getJam().setScale(KeyHelper.SCALES[i], null);
            }
        });

        if (Arrays.equals(getJam().getScale(), KeyHelper.SCALES[i])) {
        //if (getPeerJam().getScaleString().equals(KeyHelper.SCALES[i])) {
            mScaleButton = button;
            mScaleButton.setBackgroundColor(Color.GREEN);
        }

        return button;
    }
}