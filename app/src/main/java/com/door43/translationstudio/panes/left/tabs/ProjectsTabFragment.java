package com.door43.translationstudio.panes.left.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.door43.translationstudio.MainActivity;
import com.door43.translationstudio.Project;
import com.door43.translationstudio.R;
import com.door43.translationstudio.panes.left.LeftPaneSlidingTabsFragment;
import com.door43.translationstudio.util.TabsFragmentAdapterNotification;
import com.door43.translationstudio.util.TranslatorBaseFragment;

/**
 * Created by joel on 8/29/2014.
 */
public class ProjectsTabFragment extends TranslatorBaseFragment implements TabsFragmentAdapterNotification {
    private ProjectsTabFragment me = this;
    private ProjectItemAdapter mProjectItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pane_left_projects, container, false);
        ListView listView = (ListView)view.findViewById(R.id.projects_list_view);

        // create adapter
        if(mProjectItemAdapter == null) mProjectItemAdapter = new ProjectItemAdapter(app());

        // connect adapter
        listView.setAdapter(mProjectItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // select the project
                app().getSharedProjectManager().setSelectedProject(i);
                // open up the chapters tab
                ((MainActivity)me.getActivity()).getLeftPane().selectTab(1);
            }
        });

        return view;
    }

    @Override
    public void NotifyAdapterDataSetChanged() {
        mProjectItemAdapter.notifyDataSetChanged();
    }
}
