package com.asomal.imagegallery.view.image;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.asomal.imagegallery.R;
import com.asomal.imagegallery.domain.image.GetThumbnailImageCommand;
import com.asomal.imagegallery.domain.image.ImageCache;
import com.asomal.imagegallery.infrastructure.Command;
import com.asomal.imagegallery.infrastructure.CommandExecuter;
import com.asomal.imagegallery.infrastructure.Executer;
import com.asomal.imagegallery.infrastructure.ListViewExecuteManager;

/**
 * 画像をグリッド表示するためのアダプター
 * 
 * @author chuross
 * 
 */
public class ImageGridAdapter extends BaseAdapter {

	private static final int MAX_CACHE = 100;
	LayoutInflater inflater;
	List<String> filePathList;
	Context context;
	ListViewExecuteManager<Bitmap> executeManager;
	ImageCache cache;

	public ImageGridAdapter(Context context, List<String> filePathList) {
		this.context = context;
		this.filePathList = filePathList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		executeManager = new ListViewExecuteManager<Bitmap>();
		cache = new ImageCache(context, MAX_CACHE, ImageCache.Type.THUMBNAIL);
	}

	@Override
	public int getCount() {
		return filePathList.size();
	}

	@Override
	public Object getItem(int position) {
		return filePathList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;

		final ViewHolder holder;
		if (view == null) {
			view = inflater.inflate(R.layout.image_gridview_row, parent, false);
			final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
			final ImageView imageView = (ImageView) view.findViewById(R.id.grid_imageview);

			holder = new ViewHolder();
			holder.progress = progress;
			holder.imageView = imageView;

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
			holder.imageView.setVisibility(View.INVISIBLE);
			holder.progress.setVisibility(View.VISIBLE);
		}

		final String filePath = filePathList.get(position);
		holder.imageView.setTag(filePath);

		if (executeManager.containsKey(position)) {
			return view;
		}

		Executer<Bitmap> task = CommandExecuter.post(new GetThumbnailImageCommand(context, filePath, cache),
				new Command.OnFinishListener<Bitmap>() {

					@Override
					public void onFinished(Bitmap result) {
						if (result == null || !filePath.equals(holder.imageView.getTag().toString())) {
							return;
						}
						holder.imageView.setImageBitmap(result);
						holder.imageView.setVisibility(View.VISIBLE);
						holder.progress.setVisibility(View.GONE);

						executeManager.remove(position);
					}
				});

		executeManager.set(position, task);

		return view;
	}

	public void clean(int firstPosition, int lastPosition) {
		executeManager.clean(firstPosition, lastPosition);
	}

	public void cleanAll() {
		executeManager.cancelAll();
	}

	private class ViewHolder {
		ProgressBar progress;
		ImageView imageView;
	}
}
