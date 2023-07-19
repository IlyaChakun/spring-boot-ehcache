package ch.chakun.testingflow.notification_v1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@ToString
public class NotificationTestEvent implements Serializable {

    private String eventData;
}
