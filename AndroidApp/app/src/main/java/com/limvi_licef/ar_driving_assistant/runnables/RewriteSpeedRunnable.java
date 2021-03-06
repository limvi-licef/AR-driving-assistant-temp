package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.models.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.List;

public class RewriteSpeedRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.SpeedData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedData.TIMESTAMP + " BETWEEN ? AND ?";

    public RewriteSpeedRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats, String userId, String column) {
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.SpeedData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.SpeedData.TIMESTAMP, td.timestamp);
            values.put(column, td.value);
            db.insert(DatabaseContract.SpeedData.TABLE_NAME, null, values);
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.SpeedStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.SpeedStats.START_TIMESTAMP, firstTimestamp);
        stats.put(DatabaseContract.SpeedStats.END_TIMESTAMP, lastTimestamp);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_AVERAGE, extremaStats.positiveAverage);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_STD_DEVIATION, extremaStats.positiveStandardDeviation);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_COUNT, extremaStats.positiveCount);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_COUNT, extremaStats.negativeCount);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.SpeedStats.TABLE_NAME, null, stats);
    }

    @Override
    protected List<TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String column) {
        return Database.getSensorData(fromTimestamp, toTimestamp, DatabaseContract.SpeedData.TABLE_NAME, column, context);
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.SpeedData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.SpeedStats.TABLE_NAME,
                DatabaseContract.SpeedStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.SpeedStats.END_TIMESTAMP + " <= ?",
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.SpeedData.TABLE_NAME;
    }

    @Override
    protected String[] getColumns() {
        return new String[]{DatabaseContract.SpeedData.SPEED};
    }
}
