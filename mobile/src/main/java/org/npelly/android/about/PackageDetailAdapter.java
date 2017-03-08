package org.npelly.android.about;

import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.PackageDetail;
import org.npelly.android.about.common.PackageDetailManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


public class PackageDetailAdapter extends RecyclerView.Adapter<PackageDetailAdapter.ViewHolder>
        implements PackageDetailManager.PackageChangeCallback {
    public static final int TYPE_PINNED = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_SYSTEM = 2;
    public static final int TYPE_ALL = 3;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView packageIcon;
        public TextView packageText;
        public ImageView pinIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            packageIcon = (ImageView) itemView.findViewById(R.id.package_icon);
            packageText = (TextView) itemView.findViewById(R.id.package_text);
            pinIcon = (ImageView) itemView.findViewById(R.id.pin_icon);
            pinIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            PackageDetail packageDetail = packageDetails.get(position);
            if (packageDetail.isPinned) {
                About.get().getPackageDetailManager().unpin(packageDetail);
                Snackbar snackbar =
                        Snackbar.make(v, "Unstarred " + packageDetail.readableName, Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.RED);
                snackbar.setAction("Undo", new UndoClickListener(true, packageDetail));
                snackbar.show();
            } else {
                About.get().getPackageDetailManager().pin(packageDetail);
                Snackbar snackbar =
                        Snackbar.make(v, "Starred " + packageDetail.readableName, Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.RED);
                snackbar.setAction("Undo", new UndoClickListener(true, packageDetail));
                snackbar.show();
            }
        }

        private class UndoClickListener implements View.OnClickListener {
            private boolean wasPin;
            private PackageDetail packageDetail;
            public UndoClickListener(boolean wasPin, PackageDetail packageDetail) {
                this.wasPin = wasPin;
                this.packageDetail = packageDetail;
            }
            @Override
            public void onClick(View v) {
                if (wasPin) {
                    About.get().getPackageDetailManager().pin(packageDetail);
                } else {
                    About.get().getPackageDetailManager().unpin(packageDetail);
                }
            }
        }
    }

    public static PackageDetailAdapter newInstance(int type) {
        PackageDetailAdapter adapter = new PackageDetailAdapter(type);
        if (About.get().getPackageDetailManager().isInitComplete()) {
            adapter.onInitComplete(About.get().getPackageDetailManager().getPackageDetails());
        }
        return adapter;
    }

    private final int type;
    private final ArrayList<PackageDetail> packageDetails; // owned & sorted by this adapter

    private PackageDetailAdapter(int type) {
        this.type = type;
        packageDetails = new ArrayList<>();
    }

    @Override
    public PackageDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.package_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PackageDetailAdapter.ViewHolder viewHolder, int position) {
        PackageDetail detail = packageDetails.get(position);

        viewHolder.packageText.setText(detail.verboseSpan);
        viewHolder.packageIcon.setImageDrawable(detail.icon);
        if (detail.isPinned) {
            viewHolder.pinIcon.setImageResource(R.drawable.ic_star_20dp);
        } else {
            viewHolder.pinIcon.setImageResource(R.drawable.ic_star_outline_20dp);
        }
    }

    @Override
    public int getItemCount() {
        return packageDetails.size();
    }

    @Override
    public void onPackageChanged(PackageDetail detail) {
        // calculate old & new positions
        int oldPosition = indexOfPackageName(detail.packageName);
        int newPosition;
        if (isType(detail)) {
            newPosition = Collections.binarySearch(packageDetails, detail,
                    PackageDetailManager.DEFAULT_SORT_ORDER);
            if (newPosition < 0) {
                newPosition = -(newPosition + 1);
            }
        } else {
            newPosition = -1;
        }

        // apply
        if (oldPosition >= 0 && newPosition >= 0) {  // moving within list
            if (oldPosition == newPosition) {  // in-place
                packageDetails.set(newPosition, detail);
                notifyItemChanged(newPosition);
            } else {  // move
                packageDetails.remove(oldPosition);
                if (newPosition > oldPosition) {
                    newPosition--;  // shift new position back one due to removal
                }
                packageDetails.add(newPosition, detail);
                notifyItemMoved(oldPosition, newPosition);
                notifyItemChanged(newPosition);
            }
        } else if (oldPosition < 0 && newPosition >= 0) {  // added to list
            packageDetails.add(newPosition, detail);
            notifyItemInserted(newPosition);
        } else if (oldPosition >= 0 && newPosition < 0) {  // removed from list
            packageDetails.remove(oldPosition);
            notifyItemRemoved(oldPosition);
        }
    }

    @Override
    public void onInitComplete(Collection<PackageDetail> initDetails) {
        packageDetails.clear();
        for (PackageDetail detail : initDetails) {
            if (isType(detail)) {
                packageDetails.add(detail);
            }
        }
        Collections.sort(packageDetails, PackageDetailManager.DEFAULT_SORT_ORDER);
        notifyDataSetChanged();
    }

    private boolean isType(PackageDetail detail) {
        switch (type) {
            case TYPE_PINNED:
                return detail.isPinned;
            case TYPE_USER:
                if (detail == null || detail.packageInfo == null || detail.packageInfo.applicationInfo == null) {
                    return false;
                }
                return (detail.packageInfo.applicationInfo.flags &
                        (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
                        == 0;
            case TYPE_SYSTEM:
                if (detail == null || detail.packageInfo == null || detail.packageInfo.applicationInfo == null) {
                    return false;
                }
                return (detail.packageInfo.applicationInfo.flags &
                        (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
                        != 0;
            case TYPE_ALL:
                return true;
            default:
                return false;
        }
    }

    private int indexOfPackageName(String packageName) {
        for (int i = 0; i < packageDetails.size(); i++) {
            if (packageDetails.get(i).packageName.equals(packageName)) {
                return i;
            }
        }
        return -1;
    }
}
