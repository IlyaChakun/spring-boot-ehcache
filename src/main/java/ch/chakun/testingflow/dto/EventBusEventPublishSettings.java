package ch.chakun.testingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class EventBusEventPublishSettings implements Serializable {
    private String contextKey;
    private String eventKey;
}
