package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class UpgradeFragment extends OMGFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upgrade_fragment,
                container, false);

        TextView textView =(TextView)view.findViewById(R.id.upgradet2);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        String text = "<a href='https://play.google.com/store/apps/details?id=com.mikehelland.omgtechnogauntlet'>TECHNO GAUNTLET</a>";

        Activity activity = getActivity();
        if (activity != null) {
            String installer = activity.getPackageManager().getInstallerPackageName(
                    "com.mikehelland.omgtechnogauntletdemo");
            if (installer != null && installer.contains("amazon")) {
                text = "<a href='https://www.amazon.com/MonadPad-com-Techno-Gauntlet/dp/B075W3WWGR/'>TECHNO GAUNTLET</a>";
            }
        }
        textView.setText(Html.fromHtml(text));

        return view;
    }

}
