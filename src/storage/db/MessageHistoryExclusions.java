/*
 * Title: MessageHistoryExclusions.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package storage.db;

import org.telegram.api.dialog.TLDialog;

public class MessageHistoryExclusions {

    private Integer minId; // min msg id in db
    private Integer maxId; // max msg id in db
    private Integer maxDate; // max date
    private Integer minDate; // min date

    public MessageHistoryExclusions(DBStorage dbStorage, TLDialog dialog){
        this.minId = dbStorage.getMessageMinId(dialog);
        this.minDate = dbStorage.getMessageMinIdDate(dialog);
        this.maxId = dbStorage.getMessageMaxId(dialog);
        this.maxDate = dbStorage.getMessageMaxIdDate(dialog);
    }

    public boolean exist(){
        return ((this.maxId != null) && (this.minId != null) && (this.maxDate != null) && (this.minDate != null));
    }

    public Integer getMinId() {
        return minId;
    }

    public void setMinId(Integer minId) {
        this.minId = minId;
    }

    public Integer getMaxId() {
        return maxId;
    }

    public void setMaxId(Integer maxId) {
        this.maxId = maxId;
    }

    public Integer getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Integer maxDate) {
        this.maxDate = maxDate;
    }

    public Integer getMinDate() {
        return minDate;
    }

    public void setMinDate(Integer minDate) {
        this.minDate = minDate;
    }
}
