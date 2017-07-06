package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WelcomeFragment extends OMGFragment {

    private View mView;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mHandler = new Handler();

        getActivityMembers();

        mView = inflater.inflate(R.layout.welcome,
                container, false);

        if (!mJam.isSoundPoolInitialized()) {


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim = new AlphaAnimation(0, 1);
                    anim.setDuration(1200);
                    View view = mView.findViewById(R.id.omg_presents_1);
                    view.setVisibility(View.VISIBLE);
                    view.startAnimation(anim);
                }
            }, 800);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim = new AlphaAnimation(0, 1);
                    anim.setDuration(1200);
                    View view = mView.findViewById(R.id.omg_presents_2);
                    view.setVisibility(View.VISIBLE);
                    view.startAnimation(anim);

                }
            }, 2300);


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim = new AlphaAnimation(0, 1);
                    anim.setDuration(2700);

                    View view = mView.findViewById(R.id.omg_bananas);
                    view.setVisibility(View.VISIBLE);
                    view.startAnimation(anim);

                    Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                    view.startAnimation(turnin);

                }
            }, 5200);


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim = new AlphaAnimation(0, 1);
                    anim.setDuration(2000);
                    anim.setFillAfter(true);

                    mView.findViewById(R.id.txt_press_banana).startAnimation(anim);
                    View view = mView.findViewById(R.id.img_press_banana);
                    view.startAnimation(anim);

                    Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                    view.startAnimation(turnin);
                    view.setVisibility(View.VISIBLE);

                    final AlphaAnimation anim2 = new AlphaAnimation(1, 0);
                    anim2.setFillAfter(true);
                    anim2.setDuration(2700);

                    View view2 = mView.findViewById(R.id.omg_bananas);
                    view2.startAnimation(anim2);

                    mView.findViewById(R.id.omg_presents_1).startAnimation(anim2);
                    mView.findViewById(R.id.omg_presents_2).startAnimation(anim2);

                }
            }, 8500);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim = new AlphaAnimation(0, 1);
                    anim.setDuration(2000);
                    anim.setFillAfter(true);
                    mView.findViewById(R.id.txt_press_monkey).startAnimation(anim);
                    View view = mView.findViewById(R.id.img_press_monkey);
                    view.startAnimation(anim);

                    Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                    view.startAnimation(turnin);
                    view.setVisibility(View.VISIBLE);

                }
            }, 10500);


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim2 = new AlphaAnimation(1, 0);
                    anim2.setFillAfter(true);
                    anim2.setDuration(2700);

                    mView.findViewById(R.id.txt_press_banana).startAnimation(anim2);
                    mView.findViewById(R.id.img_press_banana).startAnimation(anim2);

                }
            }, 13000);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPool.isCanceled()) return;

                    final AlphaAnimation anim2 = new AlphaAnimation(1, 0);
                    anim2.setFillAfter(true);
                    anim2.setDuration(2700);

                    mView.findViewById(R.id.txt_press_monkey).startAnimation(anim2);
                    mView.findViewById(R.id.img_press_monkey).startAnimation(anim2);

                }
            }, 15800);

            setupSoundPool();

        }
        else {
            hideWelcome();
        }

        mView.findViewById(R.id.return_to_omg_bananas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainFragment mainFragment = new MainFragment();
                showFragment(mainFragment);
            }
        });


        mView.findViewById(R.id.bt_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mJam.isSoundPoolInitialized()) {
                            mPool.cancelLoading();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        BluetoothRemoteFragment f = new BluetoothRemoteFragment();

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in_up,
                                R.anim.slide_out_up,
                                R.anim.slide_in_down,
                                R.anim.slide_out_down
                        );
                        ft.add(R.id.main_layout, f);
                        ft.remove(WelcomeFragment.this);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.addToBackStack(null);
                        ft.commit();

                    }
                }).start();


            }
        });


        return mView;
    }

    public void showFragment(Fragment f) {


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right,
                R.animator.slide_out_left,
                R.animator.slide_in_left,
                R.animator.slide_out_right
        );
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void setupSoundPool() {
        final ProgressBar progressBar = (ProgressBar)mView.findViewById(R.id.loading_progress);

        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                MainFragment mainFragment = new MainFragment();
                showFragment(mainFragment);

            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                mJam.makeChannels(progressBar, callback);
            }
        }).start();


    }

    private void hideWelcome() {
        mView.findViewById(R.id.welcome_info).setVisibility(View.GONE);
        mView.findViewById(R.id.loading_info).setVisibility(View.GONE);
        mView.findViewById(R.id.goback).setVisibility(View.VISIBLE);

        TextView source = (TextView)mView.findViewById(R.id.source_link);
        source.setText(Html.fromHtml(getString(R.string.cap_source_code)));
        source.setMovementMethod(LinkMovementMethod.getInstance());
        source.setClickable(true);
        TextView issues = (TextView)mView.findViewById(R.id.issues_link);
        issues.setText(Html.fromHtml(getString(R.string.cap_suggestions)));
        issues.setMovementMethod(LinkMovementMethod.getInstance());
        issues.setClickable(true);

    }
}
