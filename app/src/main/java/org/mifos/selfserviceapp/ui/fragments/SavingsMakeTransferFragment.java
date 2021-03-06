package org.mifos.selfserviceapp.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.mifos.selfserviceapp.R;
import org.mifos.selfserviceapp.models.payload.TransferPayload;
import org.mifos.selfserviceapp.models.templates.account.AccountOption;
import org.mifos.selfserviceapp.models.templates.account.AccountOptionsTemplate;
import org.mifos.selfserviceapp.presenters.SavingsMakeTransferPresenter;
import org.mifos.selfserviceapp.ui.activities.base.BaseActivity;
import org.mifos.selfserviceapp.ui.enums.TransferType;
import org.mifos.selfserviceapp.ui.fragments.base.BaseFragment;
import org.mifos.selfserviceapp.ui.views.SavingsMakeTransferMvpView;
import org.mifos.selfserviceapp.utils.Constants;
import org.mifos.selfserviceapp.utils.DateHelper;
import org.mifos.selfserviceapp.utils.MFDatePicker;
import org.mifos.selfserviceapp.utils.ProcessView;
import org.mifos.selfserviceapp.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rajan Maurya on 10/03/17.
 */
public class SavingsMakeTransferFragment extends BaseFragment implements
        SavingsMakeTransferMvpView, AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_pay_to)
    Spinner spPayTo;

    @BindView(R.id.sp_pay_from)
    Spinner spPayFrom;

    @BindView(R.id.et_amount)
    EditText etAmount;

    @BindView(R.id.process_one)
    ProcessView pvOne;

    @BindView(R.id.process_two)
    ProcessView pvTwo;

    @BindView(R.id.process_three)
    ProcessView pvThree;

    @BindView(R.id.process_four)
    ProcessView pvFour;

    @BindView(R.id.btn_pay_to)
    AppCompatButton btnPayTo;

    @BindView(R.id.btn_pay_from)
    AppCompatButton btnPayFrom;

    @BindView(R.id.btn_amount)
    AppCompatButton btnAmount;

    @BindView(R.id.ll_review)
    LinearLayout llReview;

    @BindView(R.id.tv_select_pay_from)
    TextView tvSelectPayFrom;

    @BindView(R.id.tv_select_amount)
    TextView tvEnterAmount;

    @BindView(R.id.tv_enter_remark)
    TextView tvEnterRemark;

    @BindView(R.id.et_remark)
    EditText etRemark;

    @BindView(R.id.ll_make_transfer)
    LinearLayout layoutMakeTransfer;

    @Inject
    SavingsMakeTransferPresenter savingsMakeTransferPresenter;

    View rootView;

    private List<String> listPayTo = new ArrayList<>();
    private List<String> listPayFrom = new ArrayList<>();

    private ArrayAdapter<String> payToAdapter;
    private ArrayAdapter<String> payFromAdapter;

    private TransferPayload transferPayload;
    private String transferDate;
    private AccountOption toAccountOption, fromAccountOption;
    private AccountOptionsTemplate accountOptionsTemplate;
    private String transferType;
    private long accountId;

    /**
     * Provides an instance of {@link SavingsMakeTransferFragment}, use {@code transferType} as
     * {@code Constants.TRANSFER_PAY_TO} when we want to deposit and
     * {@code Constants.TRANSFER_PAY_FROM} when we want to make a transfer
     * @param accountId Saving account Id
     * @param transferType Type of transfer i.e. {@code Constants.TRANSFER_PAY_TO} or
     * {@code Constants.TRANSFER_PAY_FROM}
     * @return Instance of {@link SavingsMakeTransferFragment}
     */
    public static SavingsMakeTransferFragment newInstance(long accountId, String transferType) {
        SavingsMakeTransferFragment transferFragment = new SavingsMakeTransferFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ACCOUNT_ID, accountId);
        args.putString(Constants.TRANSFER_TYPE, transferType);
        transferFragment.setArguments(args);
        return transferFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getLong(Constants.ACCOUNT_ID);
            transferType = getArguments().getString(Constants.TRANSFER_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_savings_make_transfer, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.transfer));
        ButterKnife.bind(this, rootView);
        savingsMakeTransferPresenter.attachView(this);

        showUserInterface();
        savingsMakeTransferPresenter.loanAccountTransferTemplate();

        return rootView;
    }

    /**
     * Checks validation of {@code etRemark} and then opens {@link TransferProcessFragment} for
     * initiating the transfer
     */
    @OnClick(R.id.btn_review_transfer)
    void reviewTransfer() {

        if (etRemark.getText().toString().equals("")) {
            showToaster(getString(R.string.remark_is_mandatory));
            return;
        }

        transferPayload = new TransferPayload();
        transferPayload.setFromAccountId(fromAccountOption.getAccountId());
        transferPayload.setFromClientId(fromAccountOption.getClientId());
        transferPayload.setFromAccountType(fromAccountOption.getAccountType().getId());
        transferPayload.setFromOfficeId(fromAccountOption.getOfficeId());
        transferPayload.setToOfficeId(toAccountOption.getOfficeId());
        transferPayload.setToAccountId(toAccountOption.getAccountId());
        transferPayload.setToClientId(toAccountOption.getClientId());
        transferPayload.setToAccountType(toAccountOption.getAccountType().getId());
        transferPayload.setTransferDate(transferDate);
        transferPayload.setTransferAmount(Double.parseDouble(etAmount.getText().toString()));
        transferPayload.setTransferDescription(etRemark.getText().toString());


        ((BaseActivity) getActivity()).replaceFragment(TransferProcessFragment.
                newInstance(transferPayload, TransferType.SELF), true, R.id.container);
    }

    /**
     * Cancels the transfer by poping current Fragment
     */
    @OnClick(R.id.btn_cancel_transfer)
    void cancelTransfer() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Setting up basic components
     */
    @Override
    public void showUserInterface() {
        pvOne.setCurrentActive();
        payFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listPayFrom);
        payFromAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPayFrom.setAdapter(payFromAdapter);
        spPayFrom.setOnItemSelectedListener(this);

        payToAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listPayTo);
        payToAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPayTo.setAdapter(payToAdapter);
        spPayTo.setOnItemSelectedListener(this);
        transferDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.getDatePickedAsString());
    }

    /**
     * Provides with {@code accountOptionsTemplate} fetched from server which is used to update
     * {@code listPayFrom} and {@code listPayTo}
     * @param accountOptionsTemplate Template for account transfer
     */
    @Override
    public void showSavingsAccountTemplate(AccountOptionsTemplate accountOptionsTemplate) {
        this.accountOptionsTemplate = accountOptionsTemplate;
        listPayFrom.addAll(savingsMakeTransferPresenter.getAccountNumbers(
                accountOptionsTemplate.getFromAccountOptions()));
        listPayTo.addAll(savingsMakeTransferPresenter.getAccountNumbers(
                accountOptionsTemplate.getToAccountOptions()));
        payToAdapter.notifyDataSetChanged();
        payFromAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a {@link android.support.design.widget.Snackbar} with {@code message}
     * @param message String to be shown
     */
    @Override
    public void showToaster(String message) {
        Toaster.show(rootView, message);
    }

    /**
     * It is called whenever any error occurs while executing a request
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showError(String message) {
        Toaster.show(rootView, message);
    }

    @Override
    public void showProgressDialog() {
        showMifosProgressDialog(getString(R.string.making_transfer));
    }

    @Override
    public void hideProgressDialog() {
        hideMifosProgressDialog();
    }

    @Override
    public void showProgress() {
        layoutMakeTransfer.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        layoutMakeTransfer.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    /**
     * Callback for {@code spPayFrom} and {@code spPayTo}
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_pay_to:
                toAccountOption = accountOptionsTemplate.getToAccountOptions().get(position);
                break;
            case R.id.sp_pay_from:
                fromAccountOption = accountOptionsTemplate.getFromAccountOptions().get(position);
                break;
        }

        switch (transferType) {
            case Constants.TRANSFER_PAY_TO:
                setToolbarTitle(getString(R.string.deposit));
                toAccountOption = savingsMakeTransferPresenter
                        .searchAccount(accountOptionsTemplate.getToAccountOptions(), accountId);
                spPayTo.setSelection(accountOptionsTemplate.getToAccountOptions()
                        .indexOf(toAccountOption));
                spPayTo.setEnabled(false);
                pvOne.setCurrentCompeleted();
                break;
            case Constants.TRANSFER_PAY_FROM:
                setToolbarTitle(getString(R.string.transfer));
                fromAccountOption = savingsMakeTransferPresenter
                        .searchAccount(accountOptionsTemplate.getFromAccountOptions(), accountId);
                spPayFrom.setSelection(accountOptionsTemplate.getFromAccountOptions()
                        .indexOf(fromAccountOption));
                spPayFrom.setEnabled(false);
                spPayFrom.setVisibility(View.VISIBLE);
                tvSelectPayFrom.setVisibility(View.GONE);
                pvTwo.setCurrentCompeleted();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Disables {@code spPayTo} {@link Spinner} and sets {@code pvOne} to completed and make
     * {@code pvTwo} active
     */
    @OnClick(R.id.btn_pay_to)
    public void payToSelected() {
        pvOne.setCurrentCompeleted();
        pvTwo.setCurrentActive();

        btnPayTo.setVisibility(View.GONE);
        tvSelectPayFrom.setVisibility(View.GONE);
        btnPayFrom.setVisibility(View.VISIBLE);
        spPayFrom.setVisibility(View.VISIBLE);
        spPayTo.setEnabled(false);
    }

    /**
     * Checks validation of {@code spPayTo} {@link Spinner}.<br>
     *  Disables {@code spPayFrom} {@link Spinner} and sets {@code pvTwo} to completed and make
     * {@code pvThree} active
     */
    @OnClick(R.id.btn_pay_from)
    public void payFromSelected() {
        if (spPayTo.getSelectedItem().toString().equals(spPayFrom.getSelectedItem().toString())) {
            showToaster(getString(R.string.error_same_account_transfer));
            return;
        }
        pvTwo.setCurrentCompeleted();
        pvThree.setCurrentActive();

        btnPayFrom.setVisibility(View.GONE);
        tvEnterAmount.setVisibility(View.GONE);
        etAmount.setVisibility(View.VISIBLE);
        btnAmount.setVisibility(View.VISIBLE);
        spPayFrom.setEnabled(false);
    }

    /**
     * Checks validation of {@code etAmount} {@link EditText}.<br>
     * Disables {@code etAmount} and sets {@code pvThree} to completed and make
     * {@code pvFour} active
     */
    @OnClick(R.id.btn_amount)
    public void amountSet() {

        if (etAmount.getText().toString().equals("")) {
            showToaster(getString(R.string.enter_amount));
            return;
        }

        if (etAmount.getText().toString().equals(".")) {
            showToaster(getString(R.string.invalid_amount));
            return;
        }

        if (etAmount.getText().toString().matches("^0*")) {
            showToaster(getString(R.string.amount_greater_than_zero));
            return;
        }

        pvThree.setCurrentCompeleted();
        pvFour.setCurrentActive();

        btnAmount.setVisibility(View.GONE);
        tvEnterRemark.setVisibility(View.GONE);
        etRemark.setVisibility(View.VISIBLE);
        llReview.setVisibility(View.VISIBLE);
        etAmount.setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgress();
        hideMifosProgressDialog();
        savingsMakeTransferPresenter.detachView();
    }
}

