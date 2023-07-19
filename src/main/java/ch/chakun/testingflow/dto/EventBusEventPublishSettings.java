package ch.chakun.testingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@ToString
public class EventBusEventPublishSettings implements Serializable {
    private String contextKey;
    private String eventKey;
}
