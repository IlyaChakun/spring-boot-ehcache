package ch.chakun.testingflow.apm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@ToString
public class TestApmEvent implements Serializable {
    private String key;
}
