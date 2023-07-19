package ch.chakun.testingflow.notification_v1;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 3836137794878460545L;

    long dateCreated;
    long dateReceived;
    Serializable eventNotification;

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(long dateReceived) {
        this.dateReceived = dateReceived;
    }

    public Serializable getEventNotification() {
        return eventNotification;
    }

    public void setEventNotification(Serializable eventNotification) {
        this.eventNotification = eventNotification;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "dateCreated=" + dateCreated +
                ", dateReceived=" + dateReceived +
                ", eventNotification=" + eventNotification +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification that)) {
            return false;
        }
        return dateCreated == that.dateCreated &&
                dateReceived == that.dateReceived &&
                Objects.equals(eventNotification, that.eventNotification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateCreated, dateReceived, eventNotification);
    }
}