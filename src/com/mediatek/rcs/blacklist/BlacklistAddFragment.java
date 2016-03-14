/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.rcs.blacklist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.KeyEvent;

/**
 * Input dialog.
 */
public class BlacklistAddFragment extends DialogFragment {

    private static final String TAG = "BlacklistAddFragment";

    public static final String ADD_DIALOG_TAG = "blacklist_add_fragment";

    private AlertDialog mDialog;
    private InputAction mAction;
    private boolean mShowing = false;

    /**
     * For Dialog action call back.
     */
    public interface InputAction {
        /**
         * Dialog positive button pressed.
         */
        void onClickCallLogBtn();

        /**
         * Dialog import button pressed.
         */
        void onClickImportBtn();

        void onClickInputNumberBtn();
    }

    /**
     * Constructor.
     */
    public BlacklistAddFragment() {
        super();
    }

    /**
     * To show this dialog.
     * 
     * @param manager
     *            FragmentManager
     */
    public void show(FragmentManager manager) {
        log("[Add]show, mShowing: " + mShowing);

        if (!mShowing) {
            mShowing = true;
            super.show(manager, ADD_DIALOG_TAG);
        }
    }

    /**
     * Set Action handler for button pressed.
     * 
     * @param action
     *            InputAction
     */
    public void setInputAction(InputAction action) {
        mAction = action;
        log("[Add]setInputAction: " + action);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("[Add]onCreate: " + this);
        if (mAction == null) {
            // dismiss();
        }

        Fragment inputDialog = getFragmentManager().findFragmentByTag(
                BlacklistInputDialog.INPUT_DIALOG_TAG);
        if (inputDialog != null) {
            getFragmentManager().beginTransaction().remove(inputDialog);
            log("showInputDialog remove already fragment, because of state missed");
        }

        if (savedInstanceState != null) {
            Fragment f = getFragmentManager().findFragmentByTag(
                    BlacklistFragment.FRAGMENT_TAG);
            log("[Add]onCreate f: " + f);
            setInputAction((BlacklistFragment) f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        log("[Add]onResume: " + this);
        // mDialog.setTitle(getResources().getString(R.string.add_number));

        /*
         * Button posBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
         * posBtn.setText(android.R.string.ok); Button negBtn =
         * mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
         * negBtn.setText(android.R.string.cancel);
         */
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null) {
            mDialog.dismiss();
        }
        log("[Add]onPause: " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("[Add]onDestroy: " + this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("[Add]onCreateDialog: " + this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        BlacklistAddAdapter adapter = new BlacklistAddAdapter(
                getActivity(), R.layout.blacklist_add_item);
        builder.setTitle(R.string.add_to_block_list_title);

        final DialogInterface.OnClickListener selectionListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                case 0:
                    mAction.onClickImportBtn();
                    break;
                case 1:
                    mAction.onClickCallLogBtn();
                    break;
                case 2:
                    mAction.onClickInputNumberBtn();
                    break;
                default:
                    break; // do nothing
                }
            }
        };

        mDialog = builder.setAdapter(adapter, selectionListener).create();
        Dialog.OnKeyListener keyListener = new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
                return true;
            }
        };
        mDialog.setOnKeyListener(keyListener);

        return mDialog;

        /*
         * View v = View.inflate(getActivity(), R.layout.blacklist_input_dialog,
         * null);
         * 
         * final ImageButton importBtn = (ImageButton)
         * v.findViewById(R.id.contact_selector); if (importBtn != null) {
         * importBtn.setOnClickListener(new OnClickListener() {
         * 
         * @Override public void onClick(View v) { if (mAction != null) {
         * log("[Add] onClickImportBtn"); mAction.onClickImportBtn(); }
         * mDialog.dismiss(); } }); }
         * 
         * final EditText numberEdit = (EditText) v.findViewById(R.id.editor);
         * if (numberEdit != null) { numberEdit.setText("");
         * numberEdit.requestFocus(); }
         * numberEdit.setInputType(InputType.TYPE_CLASS_PHONE);
         * numberEdit.setKeyListener(PhoneNumberKeyListener.getInstance());
         * 
         * AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
         * .setTitle(R.string.add_number) .setView(v)
         * .setNegativeButton(android.R.string.cancel, null)
         * .setPositiveButton(android.R.string.ok, new
         * DialogInterface.OnClickListener() {
         * 
         * @Override public void onClick(DialogInterface dialog, int which) {
         * final String number = numberEdit.getText().toString(); if
         * (number.isEmpty()) { log("insert number return"); return; } String
         * phoneNumber = BlacklistUtils.removeSpeicalChars(number);
         * log("[Add] insert number:" + phoneNumber);
         * 
         * dialog.dismiss();
         * BlacklistUtils.insertNumber(getActivity().getContentResolver(), null,
         * phoneNumber); if (mAction != null) { log("[Add] OnClickPositiveBtn");
         * mAction.onClickPositiveBtn(); } } });
         * 
         * mDialog = builder.create();
         * 
         * mDialog.getWindow()
         * .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
         * );
         */

    }

    @Override
    public void onDestroyView() {
        log("[Add]onDestroyView: " + this);
        super.onDestroyView();
        // setInputAction(null);
        setTargetFragment(null, 0);
        mShowing = false;
    }

    /**
     * DialerKeyListener, to limit input data.
     */
    private static class PhoneNumberKeyListener extends DialerKeyListener {
        private static PhoneNumberKeyListener sKeyListener;
        /**
         * The characters that can be inputted.
         * 
         * @see KeyEvent#getMatch
         * @see #getAcceptedChars
         */
        public static final char[] CHARACTERS = new char[] { '0', '1', '2',
                '3', '4', '5', '6', '7', '8', '9', '+', '*', '#', 'P', 'W',
                'p', 'w', ',', ';' };

        @Override
        protected char[] getAcceptedChars() {
            return CHARACTERS;
        }

        public static PhoneNumberKeyListener getInstance() {
            if (sKeyListener == null) {
                sKeyListener = new PhoneNumberKeyListener();
            }
            return sKeyListener;
        }
    }

    private void log(String message) {
        Log.d(TAG, "[" + getClass().getSimpleName() + "] " + message);
    }
}
