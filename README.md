# BlackList
BlacklistFragment:

(1)MultiConferenceCallsPickerFragment因为有联系人选择个数限制，超过9个会出现空指针异常，所以切换到MultiContactsPickerBaseFragment
在联系人ContactListMultiChoiceActivity中onCreate根据接收的intent来加载不同的fragment

(2注意BlacklistUtils：!CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)

加载fragment可以有几种方式：

(1)继承自普通的Fragment
BlacklistFragment listFragment = new BlacklistFragment();
getFragmentManager().beginTransaction()
            .replace(R.id.blacklistActivity, listFragment, BlacklistFragment.FRAGMENT_TAG)
            //.commit();
            .commitAllowingStateLoss();

(2)继承自DialogFragment
BlacklistAddFragment extends DialogFragment
public static final String ADD_DIALOG_TAG = "blacklist_add_fragment";
BlacklistAddFragment mAddFragment = new BlacklistAddFragment();
Fragment f = getFragmentManager().findFragmentByTag(BlacklistAddFragment.ADD_DIALOG_TAG);
if (f != null) {
    log("showAddFragment, press multi times, skip it");
    return;
}
mAddFragment.show(getFragmentManager());
if (!mShowing) {
    mShowing = true;
    super.show(manager, ADD_DIALOG_TAG);//调用父类DialogFragment的方法显示
}

(3)在layout中定义
InCallActivity

if (mCallCardFragment == null) {
    mCallCardFragment = (CallCardFragment) getFragmentManager().findFragmentById(R.id.callCardFragment); //callCardFragment在layout：incall_screen.xml中定义
}

mChildFragmentManager = mCallCardFragment.getChildFragmentManager(); //mCallCardFragment

if (mCallButtonFragment == null) {
    mCallButtonFragment = (CallButtonFragment) mChildFragmentManager.findFragmentById(R.id.callButtonFragment); //在call_card_content.xml中定义，所以要用mChildFragmentManager
    mCallButtonFragment.getView().setVisibility(View.INVISIBLE);
}

if (mAnswerFragment == null) {
    mAnswerFragment = (AnswerFragment) mChildFragmentManager.findFragmentById(R.id.answerFragment);
}

