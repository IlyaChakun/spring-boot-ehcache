package ch.chakun.testingflow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class EventBusSubscriberSettings implements Serializable {
    private String contextKey;
    private List<String> expectedEventKeys = new ArrayList<>();
}
