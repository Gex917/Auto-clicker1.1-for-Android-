package com.autoclicker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 点击位置列表适配器
 */
public class ClickPointAdapter extends RecyclerView.Adapter<ClickPointAdapter.ViewHolder> {

    private List<ClickPoint> clickPoints;
    private OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public ClickPointAdapter(List<ClickPoint> clickPoints, OnDeleteListener onDeleteListener) {
        this.clickPoints = clickPoints;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_click_point, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClickPoint point = clickPoints.get(position);

        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.etX.setText(String.valueOf(point.getX()));
        holder.etY.setText(String.valueOf(point.getY()));
        holder.etRepeatCount.setText(String.valueOf(point.getRepeatCount()));
        holder.etClickInterval.setText(String.valueOf(point.getClickInterval()));
        holder.etRoundInterval.setText(String.valueOf(point.getRoundInterval()));

        // X 坐标监听
        holder.etX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    point.setX(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // Y 坐标监听
        holder.etY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    point.setY(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // 重复次数监听
        holder.etRepeatCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    point.setRepeatCount(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // 点击间隔监听
        holder.etClickInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    point.setClickInterval(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // 轮次间隔监听
        holder.etRoundInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    point.setRoundInterval(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // 删除按钮
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteListener != null) {
                onDeleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clickPoints.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        EditText etX, etY, etRepeatCount, etClickInterval, etRoundInterval;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            etX = itemView.findViewById(R.id.etX);
            etY = itemView.findViewById(R.id.etY);
            etRepeatCount = itemView.findViewById(R.id.etRepeatCount);
            etClickInterval = itemView.findViewById(R.id.etClickInterval);
            etRoundInterval = itemView.findViewById(R.id.etRoundInterval);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
