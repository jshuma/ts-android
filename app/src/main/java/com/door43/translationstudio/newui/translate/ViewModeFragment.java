package com.door43.translationstudio.newui.translate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.door43.tools.reporting.Logger;
import com.door43.translationstudio.R;
import com.door43.translationstudio.core.Library;
import com.door43.translationstudio.core.SourceTranslation;
import com.door43.translationstudio.core.TargetTranslation;
import com.door43.translationstudio.core.TranslationNote;
import com.door43.translationstudio.core.TranslationWord;
import com.door43.translationstudio.core.Translator;
import com.door43.translationstudio.newui.BaseFragment;
import com.door43.translationstudio.util.AppContext;

import org.json.JSONException;

import java.security.InvalidParameterException;

/**
 * Created by joel on 9/18/2015.
 */
public abstract class ViewModeFragment extends BaseFragment implements ViewModeAdapter.OnEventListener, ChooseSourceTranslationDialog.OnClickListener {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ViewModeAdapter mAdapter;
    private boolean mFingerScroll = false;
    private OnEventListener mListener;
    private TargetTranslation mTargetTranslation;
    private Translator mTranslator;
    private Library mLibrary;
    private String mSourceTranslationId;

    /**
     * Returns an instance of the adapter
     * @param activity
     * @param targetTranslationId
     * @param sourceTranslationId
     * @return
     */
    abstract ViewModeAdapter generateAdapter(Activity activity, String targetTranslationId, String sourceTranslationId, String chapterId, String frameId);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stacked_card_list, container, false);

        mLibrary = AppContext.getLibrary();
        mTranslator = AppContext.getTranslator();

        Bundle args = getArguments();
        String targetTranslationId = args.getString(TargetTranslationActivity.EXTRA_TARGET_TRANSLATION_ID, null);
        mTargetTranslation = mTranslator.getTargetTranslation(targetTranslationId);
        if(mTargetTranslation == null) {
            throw new InvalidParameterException("a valid target translation id is required");
        }

        String chapterId = args.getString(TargetTranslationActivity.EXTRA_CHAPTER_ID, AppContext.getLastFocusChapterId(targetTranslationId));
        String frameId = args.getString(TargetTranslationActivity.EXTRA_FRAME_ID, AppContext.getLastFocusFrameId(targetTranslationId));

        // open selected tab
        mSourceTranslationId = AppContext.getSelectedSourceTranslationId(targetTranslationId);

        if(mSourceTranslationId == null) {
            mListener.onNoSourceTranslations(targetTranslationId);
        } else {
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter = generateAdapter(this.getActivity(), targetTranslationId, mSourceTranslationId, chapterId, frameId);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    mFingerScroll = true;
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mFingerScroll) {
                        int position = mLayoutManager.findFirstVisibleItemPosition();
                        mListener.onScrollProgress(position);
                    }
                }
            });

            mListener.onItemCountChanged(mAdapter.getItemCount(), 0);

            mAdapter.setOnClickListener(this);

            if(savedInstanceState == null) {
                mLayoutManager.scrollToPosition(mAdapter.getListStartPosition());
                mListener.onScrollProgress(mAdapter.getListStartPosition());
            }
        }

        // let child classes modify the view
        onPrepareView(rootView);

        return rootView;
    }

    /**
     * Scrolls to the given frame
     * @param chapterId
     * @param frameId
     */
    protected void scrollToFrame(String chapterId, String frameId) {
        int position = mAdapter.getItemPosition(chapterId, frameId);
        if(position != -1) {
            mLayoutManager.scrollToPosition(position);
            mListener.onScrollProgress(position);
        }
    }

    protected void onPrepareView(View rootView) {
        // place holder so child classes can modify the view
    }

    protected ViewModeAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null) {
            mAdapter.rebuild();
        }
    }

    /**
     * Forces the software keyboard to close
     */
    public void closeKeyboard() {
        if(getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public void onTranslationWordClick(String translationWordId, int width) {

    }

    @Override
    public void onTranslationNoteClick(String chapterId, String frameId, String translationNoteId, int width) {

    }

    /**
     * Require correct interface
     * @param activity
     */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ViewModeFragment.OnEventListener");
        }
    }

    /**
     * Called when the scroll progress manually changes
     * @param scrollProgress
     */
    public void onScrollProgressUpdate(int scrollProgress) {
        mFingerScroll = false;
        mRecyclerView.scrollToPosition(scrollProgress);
    }

    @Override
    public void onSourceTranslationTabClick(String sourceTranslationId) {
        AppContext.setSelectedSourceTranslation(mTargetTranslation.getId(), sourceTranslationId);
        mAdapter.setSourceTranslation(sourceTranslationId);
    }

    @Override
    public void onNewSourceTranslationTabClick() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("tabsDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ChooseSourceTranslationDialog dialog = new ChooseSourceTranslationDialog();
        Bundle args = new Bundle();
        args.putString(ChooseSourceTranslationDialog.ARG_TARGET_TRANSLATION_ID, mTargetTranslation.getId());
        dialog.setOnClickListener(this);
        dialog.setArguments(args);
        dialog.show(ft, "tabsDialog");
    }

    @Override
    public void onCancelTabsDialog(String targetTranslationId) {

    }

    @Override
    public void onConfirmTabsDialog(String targetTranslationId, String[] sourceTranslationIds) {
        String[] oldSourceTranslationIds = AppContext.getOpenSourceTranslationIds(targetTranslationId);
        for(String id:oldSourceTranslationIds) {
            AppContext.removeOpenSourceTranslation(targetTranslationId, id);
        }

        if(sourceTranslationIds.length > 0) {
            // save open source language tabs
            for(String id:sourceTranslationIds) {
                SourceTranslation sourceTranslation = mLibrary.getSourceTranslation(id);
                AppContext.addOpenSourceTranslation(targetTranslationId, sourceTranslation.getId());
                TargetTranslation targetTranslation = mTranslator.getTargetTranslation(targetTranslationId);
                if(targetTranslation != null) {
                    try {
                        targetTranslation.useSourceTranslation(sourceTranslation);
                    } catch (JSONException e) {
                        Logger.e(this.getClass().getName(), "Failed to record source translation (" + sourceTranslation.getId() + ") usage in the target translation " + targetTranslation.getId(), e);
                    }
                }

            }
            String selectedSourceTranslationId = AppContext.getSelectedSourceTranslationId(targetTranslationId);
            mAdapter.setSourceTranslation(selectedSourceTranslationId);
        } else {
            mListener.onNoSourceTranslations(targetTranslationId);
        }
    }

    @Override
    public void onDestroy() {
        // save position state
        if(mLayoutManager != null) {
            int lastItemPosition = mLayoutManager.findFirstVisibleItemPosition();
            String chapterId = mAdapter.getFocusedChapterId(lastItemPosition);
            String frameId = mAdapter.getFocusedFrameId(lastItemPosition);
            AppContext.setLastFocus(mTargetTranslation.getId(), chapterId, frameId);
        }
        super.onDestroy();
    }

    /**
     * Receives touch events directly from the activity
     * Some times click events can get consumed by a list view or some other object.
     * Receiving events directly from the activity avoids these issues
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public interface OnEventListener {

        /**
         * Called when the user scrolls with their finger
         * @param progress
         */
        void onScrollProgress(int progress);

        /**
         * Called when the number of items in the fragment adapter changes
         * @param itemCount
         * @param progress
         */
        void onItemCountChanged(int itemCount, int progress);

        /**
         * No source translation has been chosen for this target translation
         * @param targetTranslationId
         */
        void onNoSourceTranslations(String targetTranslationId);
    }
}
