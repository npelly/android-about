package org.npelly.android.about;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.PackageDetail;
import org.npelly.android.about.common.PackageDetailManager;

import java.util.Collection;


public class PackageListFragment extends Fragment implements PackageDetailManager.PackageChangeCallback {
    private static final String KEY_TYPE = "t";

    public static PackageListFragment newInstance(int type) {
        PackageListFragment fragment = new PackageListFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TYPE, type);
        fragment.setArguments(bundle);

        return fragment;
    }

    private PackageDetailManager packageDetailManager;
    private PackageDetailAdapter adapter;
    private int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(KEY_TYPE);
        About.logd("PackageListFragment[%d] onCreate()", type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        About.logd("PackageListFragment[%d] onCreateView()", type);
        return inflater.inflate(R.layout.package_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        About.logd("PackageListFragment[%d] onActivityCreated()", type);

        packageDetailManager = About.get().getPackageDetailManager();
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.package_list);
        adapter = PackageDetailAdapter.newInstance(type);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (packageDetailManager.isInitComplete()) {
            onInitComplete(null);
        }
        packageDetailManager.addPackageChangeCallback(this);
        packageDetailManager.addPackageChangeCallback(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        About.logd("PackageListFragment[%d] onStop()", type);

        packageDetailManager.removePackageChangeCallback(this);
        packageDetailManager.removePackageChangeCallback(adapter);
    }

    @Override
    public void onPackageChanged(PackageDetail detail) { }

    @Override
    public void onInitComplete(Collection<PackageDetail> initDetails) {
        TextView loadingText = (TextView) getView().findViewById(R.id.loading_text);
        if (loadingText != null) {
            loadingText.setVisibility(View.GONE);
        }
    }
}
