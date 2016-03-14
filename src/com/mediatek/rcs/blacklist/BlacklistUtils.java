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

import android.content.ContentResolver;
//import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
//import android.widget.Toast;

import com.mediatek.rcs.blacklist.BlacklistData.BlacklistTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * BlacklistUtils.
 */
public class BlacklistUtils {

    private static final String TAG = "Blacklist";

    private static final Uri BLACKLIST_URI = BlacklistData.AUTHORITY_URI;
    private static final Uri CONTACTS_URI = Data.CONTENT_URI;
    private static final String[] BLACKLIST_PROJECTION = {BaseColumns._ID,
                                                            BlacklistTable.DISPLAY_NAME,
                                                            BlacklistTable.PHONE_NUMBER};

    // regular expression for removing '.' '-' and ' '
    private static final String SPLIT_CHARS_EXP = new String("[.-]");
    private static final String WHITESPACE_CHAR_EXP = new String("\\s");

    private static final String[] CONTACTS_PROJECTION = new String[] {
        Phone.MIMETYPE,
        Phone.NUMBER,
        Phone.DISPLAY_NAME};
    
    private static final String[] CALLLOGS_PROJECTION = new String[] {
        Calls.NUMBER,
        Contacts.DISPLAY_NAME};

    private final static ArrayList<SyncWithContactsCallback> mSyncCallbacks =
                                new ArrayList<SyncWithContactsCallback>();
    private static SyncWithContactsTask sSyncTask = null;

    protected static void insertNumber(ContentResolver resolver, String name, String number) {
        ContentValues values = new ContentValues();

        values.put(BlacklistTable.PHONE_NUMBER, number);

        if (name != null && !name.isEmpty()) {
            values.put(BlacklistTable.DISPLAY_NAME, name);
        }

        resolver.insert(BLACKLIST_URI, values);
        log("insertNumber: " + name + ", " + number);
    }

    protected static void importFromContacts(ContentResolver resolver, final long[] ids) {
        if (ids == null || ids.length <= 0) {
            log("ids is null!");
            return;
        }

        /*StringBuilder selection = new StringBuilder(Phone._ID + " in (");
        for (long id : ids) {
            selection.append(Long.toString(id));
            selection.append(',');
        }
        selection.deleteCharAt(selection.length() - 1);
        selection.append(')');

        log(selection.toString());
        Cursor cursorContact = resolver.query(CONTACTS_URI, CONTACTS_PROJECTION,
                                        selection.toString(), null, null);*/
        
        StringBuilder sb = new StringBuilder("");
        for (long contactId : ids) {
            if (contactId == ids[ids.length - 1]) {
                sb.append(contactId);
            } else {
                sb.append(contactId + ",");
            }
        }
        String selection = Data.CONTACT_ID + " in (" + sb.toString() + ")" + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";

        log("compose.addTextVCard(): selection = " + selection);
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        Cursor cursorContact = resolver.query(dataUri, // URI
                CONTACTS_PROJECTION, // projection
                selection, // selection
                null, // selection args
                RawContacts.SORT_KEY_PRIMARY + ", " + Data.CONTACT_ID); // RawContacts.SORT_KEY_PRIMARY + ", " + Data.CONTACT_ID sortOrder
        
        if (cursorContact == null) {
            log("cursorContact is null!");
            return;
        }
        log("cursorContact count:"+cursorContact.getCount());
        String number;
        String name;
        String mimeType;
        try {
            cursorContact.moveToFirst();
            while (!cursorContact.isAfterLast()) {
                mimeType = cursorContact
                        .getString(cursorContact.getColumnIndexOrThrow(CONTACTS_PROJECTION[0]));
                number = cursorContact
                        .getString(cursorContact.getColumnIndexOrThrow(CONTACTS_PROJECTION[1]));
                name = cursorContact
                        .getString(cursorContact.getColumnIndexOrThrow(CONTACTS_PROJECTION[2]));
                if (number == null || number.isEmpty() || !CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    log("cursor is null or empty !");
                } else {
                    ContentValues values = new ContentValues();
                    values.put(BlacklistTable.PHONE_NUMBER, number);
                    if (name != null && !name.isEmpty()) {
                        values.put(BlacklistTable.DISPLAY_NAME, name);
                    }

                    resolver.insert(BLACKLIST_URI, values);
                }
                cursorContact.moveToNext();
           }
        } finally {
            cursorContact.close();
        }
    }
    
    protected static void importFromCallLogs(ContentResolver resolver, HashMap<String,String> map) {
        if (map == null || map.size() <= 0) {
            log("map is null!");
            return;
        }

        /*StringBuilder selection = new StringBuilder(Calls._ID + " in (");
        for (int id : callLogIds) {
            selection.append(Long.toString(id));
            selection.append(',');
        }
        selection.deleteCharAt(selection.length() - 1);
        selection.append(')');

        log(selection.toString());
        
        Uri uri = Uri.parse("content://call_log/callsjoindataview").buildUpon()
                .appendQueryParameter(Calls.ALLOW_VOICEMAILS_PARAM_KEY, "true")
                .appendQueryParameter(Calls.LIMIT_PARAM_KEY, Integer.toString(1000))
                .build();
        
        Cursor cursorCallLogs = resolver.query(uri, CALLLOGS_PROJECTION,
                                        selection.toString(), null, Calls.DEFAULT_SORT_ORDER);
        if (cursorCallLogs == null) {
            log("cursorCallLogs is null!");
            return;
        }

        try {
            cursorCallLogs.moveToFirst();
            while (!cursorCallLogs.isAfterLast()) {
                String number = cursorCallLogs
                        .getString(cursorCallLogs.getColumnIndexOrThrow(CALLLOGS_PROJECTION[0]));
                String name = cursorCallLogs
                        .getString(cursorCallLogs.getColumnIndexOrThrow(CALLLOGS_PROJECTION[1]));
                if (number == null || number.isEmpty()) {
                    log("cursor is null or empty !");
                } else {
                    ContentValues values = new ContentValues();
                    values.put(BlacklistTable.PHONE_NUMBER, number);
                    if (name != null && !name.isEmpty()) {
                        values.put(BlacklistTable.DISPLAY_NAME, name);
                    }
                    resolver.insert(BLACKLIST_URI, values);
                }
                cursorCallLogs.moveToNext();
           }
        } finally {
            cursorCallLogs.close();
        }*/
        
        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext())
        {
            String number = iter.next();
            String name = map.get(number);
            ContentValues values = new ContentValues();
            if (number == null || number.isEmpty()) {
                log("cursor is null or empty !");
            }
            
            values.put(BlacklistTable.PHONE_NUMBER, number);
            if (name != null && !name.isEmpty()) {
                values.put(BlacklistTable.DISPLAY_NAME, name);
            }

            resolver.insert(BLACKLIST_URI, values);
            log("insertNumber: " + name + ", " + number);
        }
    }

    protected static void deleteMembers(ContentResolver resolver, String where) {
        resolver.delete(BLACKLIST_URI, where, null);
    }

    /**
     * SyncWithContactsCallback.
     */
    public interface SyncWithContactsCallback {
        /**
         * onUpdatedWithContacts.
         * @param result boolean
         */
        void onUpdatedWithContacts(boolean result);
    }

    protected static void startSyncWithContacts(ContentResolver resolver,
                                                SyncWithContactsCallback cb) {
        if (sSyncTask != null && sSyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            sSyncTask.cancel(true);
        }

        if (!mSyncCallbacks.contains(cb)) {
            log("add sync callback");
            if (mSyncCallbacks.size() > 0) {
                log("remove all sync callback");
                mSyncCallbacks.clear();
            }

            mSyncCallbacks.add(cb);
        }

        if (sSyncTask == null || sSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            sSyncTask = new SyncWithContactsTask();

            log("Start to sync with Contacts");
            sSyncTask.execute(resolver);
        }
    }

    protected static void cancelSyncWithContacts(SyncWithContactsCallback cb) {
        if (mSyncCallbacks.contains(cb)) {
            log("cancel to sync with Contacts, remove callback");
            mSyncCallbacks.remove(cb);
        }

        if (mSyncCallbacks.size() == 0) {
            if (sSyncTask != null && sSyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                sSyncTask.cancel(true);
            }
        }
    }

    /**
     * SyncWithContactsTask.
     * used to sync with contacts
     */
    public static class SyncWithContactsTask extends AsyncTask<ContentResolver, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(ContentResolver... params) {
            log("SyncWithContactsTask doInBackground");
            Integer ret = 0;
            if (syncwithContacts(params[0])) {
                ret = 1;
            }

            return ret;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (!this.isCancelled()) {
                log("sync contacts done: " + result);

                for (SyncWithContactsCallback cb : mSyncCallbacks) {
                    cb.onUpdatedWithContacts(result == 1 ? true : false);
                }
            }
        }
    }

    private static boolean syncwithContacts(ContentResolver resolver) {
        boolean ret = false;
        ArrayList<String> contactsNames = new ArrayList<String>();

        Cursor blackListCusror = resolver.query(BLACKLIST_URI, BLACKLIST_PROJECTION,
                                            null, null, null);

        log("syncwithContacts ++");

        try {
            if (blackListCusror == null || blackListCusror.getCount() == 0) {
                log("blacklist is empty");
                return ret;
            }

            //blackListCusror.moveToFirst();
            while (blackListCusror.moveToNext()) {
                String number = blackListCusror
                        .getString(blackListCusror.getColumnIndexOrThrow(BLACKLIST_PROJECTION[2]));
                String name = blackListCusror
                        .getString(blackListCusror.getColumnIndexOrThrow(BLACKLIST_PROJECTION[1]));
                String id = blackListCusror
                        .getString(blackListCusror.getColumnIndexOrThrow(BLACKLIST_PROJECTION[0]));

                Cursor contactsCursor = resolver.query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)),
                                new String[] {PhoneLookup.DISPLAY_NAME}, null, null, null);
                try {
                    if (contactsCursor == null || contactsCursor.getCount() == 0) {
                        log("Contacts does not contain the number: " + number);
                        continue;
                    }

                    contactsCursor.moveToFirst();

                    contactsNames.clear();
                    while (!contactsCursor.isAfterLast()) {
                        String contactName = contactsCursor.getString(0);
                        contactsNames.add(contactName);
                        log("contacts name: " + contactName + ", " + number);
                        contactsCursor.moveToNext();
                    }

                    if (!contactsNames.contains(name)) {
                        name = contactsNames.get(0);
                        ContentValues values = new ContentValues();
                        values.put(BLACKLIST_PROJECTION[1], name);
                        log("contacts name: " + name + ", " + number + ",  " + id);

                        resolver.update(Uri.withAppendedPath(BLACKLIST_URI, Uri.encode(id)),
                                        values, null, null);
                        ret = true;
                    }
                } finally {
                    contactsCursor.close();
                }

            }
        } finally {
            blackListCusror.close();
            log("syncwithContacts --");
        }

        return ret;
    }

    /**
     * buildQueryNubmer.
     * @param number String
     * @return String
     */
    public static String buildQueryNubmer(String number) {
        StringBuilder sb = new StringBuilder();

        sb.append("\'");
        sb.append(number);
        sb.append("\'");

        log("buildQueryNubmer:" + number);

        return sb.toString();
    }

    /**
     * removeSpeicalChars.
     * @param number String
     * @return String
     */
    public static String removeSpeicalChars(String number) {
        log("removeSpeicalChars, befor:" + number);

        String ret = number.replaceAll(WHITESPACE_CHAR_EXP, "");
        ret = ret.replaceAll(SPLIT_CHARS_EXP, "");
        log("removeSpeicalChars, after:" + ret);

        return ret;
    }

    private static void log(String message) {
        Log.d(TAG, "[BlacklistUtils] " + message);
    }
}
