package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class AddTagsFragment extends OMGFragment {

    //private Jam mJam;
    //private MainFragment mMainFragment;

    private View mView;

    private OMGHelper mOMGHelper;

    public AddTagsFragment() {}

    private boolean isSetup = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_tags_fragment,
                container, false);

        mView = view;


        if (mOMGHelper != null) {
            setup();
        }
        else
            isSetup = true;

        return view;
    }

    private void setup() {

        final TextView tagText = (TextView) mView.findViewById(R.id.add_tags_text);

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
                    if (!tagText.getText().toString().contains(((Button) view).getText())) {
                        tagText.append(" " + ((Button) view).getText());
                    }
                }
            });
        }

        mView.findViewById(R.id.finish_and_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOMGHelper.updateTags(tagText.getText().toString());

                getActivity().getFragmentManager().popBackStack();

                mOMGHelper.shareLastSaved();
            }
        });

        mView.findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOMGHelper.updateTags(tagText.getText().toString());

                getActivity().getFragmentManager().popBackStack();
            }
        });
    }

    /*public void setJam(Jam jam, MainFragment mainFragment) {
        mJam = jam;
        mMainFragment = mainFragment;

    }*/

    public void setOMGHelper(OMGHelper omgHelper) {
        mOMGHelper = omgHelper;

        if (isSetup)
            setup();
    }

}
