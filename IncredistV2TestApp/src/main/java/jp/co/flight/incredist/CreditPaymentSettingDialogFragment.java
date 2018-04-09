package jp.co.flight.incredist;

import android.app.Dialog;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.CompoundButton;

import java.util.EnumSet;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.android.IncredistV2TestApp.databinding.FragmentDialogCreditSettingBinding;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;

/**
 * クレジット決済設定ダイアログ
 */
public class CreditPaymentSettingDialogFragment extends DialogFragment {
    private ViewModel mData = new ViewModel();

    public CreditPaymentSettingDialogFragment() {
        // Required empty public constructor
    }

    public static CreditPaymentSettingDialogFragment newInstance() {
        CreditPaymentSettingDialogFragment fragment = new CreditPaymentSettingDialogFragment();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        FragmentDialogCreditSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_dialog_credit_setting, null, false);
        binding.setFragment(this);
        binding.setViewModel(mData);

        builder.setView(binding.getRoot());

        builder.setPositiveButton(R.string.ok, (dialogInterface, button) -> {
            Fragment target = getTargetFragment();
            int requestCode = getTargetRequestCode();

            if (target != null && target instanceof Listener) {
                ((Listener) target).onSetCreditPaymentSetting(requestCode, mData.mCardTypeSet.get(), mData.mAmount.get(), mData.mTagType.get(), mData.mAidSetting.get(), mData.mTransactionType.get(), mData.mFallback.get(), mData.mTimeout.get());
            }
        });

        return builder.create();
    }

    public void onCardTypeCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        CreditCardType cardType;
        switch (checkBox.getId()) {
            case R.id.check_card_type_msr:
                cardType = CreditCardType.MSR;
                break;
            case R.id.check_card_type_contact_emv:
                cardType = CreditCardType.ContactEMV;
                break;
            case R.id.check_card_type_contactless_emv:
                cardType = CreditCardType.ContactlessEMV;
                break;
            default:
                cardType = null;
                break;
        }

        if (cardType != null) {
            EnumSet<CreditCardType> cardTypeSet = mData.mCardTypeSet.get();
            if (isChecked) {
                cardTypeSet.remove(cardType);
            } else {
                cardTypeSet.add(cardType);
            }
            mData.mCardTypeSet.set(cardTypeSet);
        }
    }

    public static class ViewModel extends BaseObservable {
        public final ObservableField<EnumSet<CreditCardType>> mCardTypeSet = new ObservableField<>();
        public final ObservableLong mAmount = new ObservableLong();
        public final ObservableField<EmvTagType> mTagType = new ObservableField<>();
        public final ObservableInt mAidSetting = new ObservableInt();
        public final ObservableField<EmvTransactionType> mTransactionType = new ObservableField<>();
        public final ObservableBoolean mFallback = new ObservableBoolean();
        public final ObservableLong mTimeout = new ObservableLong();

        public final ObservableBoolean mMsrChecked = new ObservableBoolean();
        public final ObservableBoolean mContactEmvChecked = new ObservableBoolean();
        public final ObservableBoolean mContactlessEmvChecked = new ObservableBoolean();


        ViewModel() {
            mCardTypeSet.set(EnumSet.allOf(CreditCardType.class));
            mAmount.set(10000);
            mTagType.set(EmvTagType.AllTag);
            mAidSetting.set(0);
            mTransactionType.set(EmvTransactionType.Purchase);
            mFallback.set(true);
            mTimeout.set(20000);
        }
    }

    public interface Listener {
        void onSetCreditPaymentSetting(int requestCode, EnumSet<CreditCardType> cardTypeSet, long amount,
                                       EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout);
    }
}
