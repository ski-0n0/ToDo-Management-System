package com.dmm.task.form;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TaskForm {
	@Positive()
	private Integer id;
	@Size(min = 1, max = 200)
	private String title;
	@Size(min = 1, max = 200)
	private String text;
	@Pattern(regexp="^\\d{4}-\\d{2}-\\d{2}$")
	private String date;
	private Boolean done;
}
