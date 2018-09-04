package ru.sendto.rest.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.sendto.dto.Dto;
import ru.sendto.dto.RequestInfo;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonTypeName("sync")
public class ResponseDto extends RequestInfo{
	@JsonProperty("l")
	List<Dto> list;
	
}
