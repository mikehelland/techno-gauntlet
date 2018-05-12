package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class AddTagsFragment extends OMGFragment {

    //private Jam mJam;
    //private MainFragment mMainFragment;

    private View mView;

    public AddTagsFragment() {}

    private EditText mTagText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_tags_fragment,
                container, false);

        mView = view;

        setup();
        return view;
    }

    private void setup() {

        mTagText = (EditText) mView.findViewById(R.id.add_tags_text);
        mTagText.setText(getJam().getTags());

        int[] tagIds = new int[]{R.id.popular_tag_button_1,
                R.id.popular_tag_button_2,
                R.id.popular_tag_button_3,
                R.id.popular_tag_button_4,
                R.id.popular_tag_button_5,
                R.id.popular_tag_button_6,
                R.id.popular_tag_button_7,
                R.id.popular_tag_button_8
        };

        for (int tagId : tagIds) {
            mView.findViewById(tagId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mTagText.getText().toString().contains(((Button) view).getText())) {
                        mTagText.append(" " + ((Button) view).getText());
                    }
                }
            });
        }

        mView.findViewById(R.id.finish_and_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(true);
            }
        });

        mView.findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(false);
            }
        });

        Activity activity = getActivity(); if (activity == null)  return;

        InputMethodManager imgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mTagText.requestFocus();
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        mTagText.setSelection(mTagText.getText().length());

    }

    private void finish(boolean shareAfter) {
        String tags = mTagText.getText().toString();
        getJam().setTags(tags);

        Activity activity = getActivity();
        if (activity == null)
            return;

        OMGHelper omgHelper = new OMGHelper(activity, getJam());
        omgHelper.submit(shareAfter);

        FragmentManager fm = getFragmentManager();
        if (fm != null)
            fm.popBackStack();

    }

}
