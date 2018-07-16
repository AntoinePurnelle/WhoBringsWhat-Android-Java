package net.ouftech.whobringswhat.model;

public abstract class BaseModel {

    long creationDate;

    public BaseModel() {
    }

    public BaseModel(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
