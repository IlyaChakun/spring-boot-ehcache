package ch.chakun.testweb.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Event implements AbstractDto {

    private String key;
    private String value;

}
