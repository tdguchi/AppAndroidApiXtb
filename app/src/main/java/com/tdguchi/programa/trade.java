package com.tdguchi.programa;


import androidx.annotation.NonNull;

public class trade implements Comparable<trade> {
    private int comment;
    private double sl;
    private double tp;
    private int position;
    private int cmd;
    private boolean checked = false;

    public trade(int cmd, int comment, double sl, double tp, int position) {
        this.cmd = cmd;
        this.comment = comment;
        this.sl = sl;
        this.tp = tp;
        this.position = position;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean getChecked() {
        return checked;
    }

    public int getCmd() {
        return cmd;
    }

    public int getPosition() {
        return position;
    }

    public int getComment() {
        return comment;
    }

    public double getSl() {
        return sl;
    }

    public double getTp() {
        return tp;
    }

    @NonNull
    @Override
    public String toString() {
        return "trade [comment=" + comment + ", sl=" + sl + ", tp=" + tp + ", position=" + position + ", cmd=" + cmd
                + ", checked=" + checked + "]";
    }

    @Override
    public int compareTo(trade o) {
        if (this.position > o.position) {
            return 1;
        } else if (this.position < o.position) {
            return -1;
        }
        return 0;
    }
}