package com.dmm.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {

	private static TasksRepository tasksRepository;

	public MainController(TasksRepository tasksRepository) {
		MainController.tasksRepository = tasksRepository;
	}

	@GetMapping("/main")
	String MainPage(@Validated TaskForm taskForm, BindingResult bindingResult, @AuthenticationPrincipal AccountUserDetails user, Model model) {

		if (bindingResult.hasErrors()) {
			throw new IllegalArgumentException();
		}

		LocalDate thisMonthFirstDay = Objects.isNull(taskForm.getDate()) ? LocalDate.now().withDayOfMonth(1)
				: parseLocalDate(taskForm.getDate()).withDayOfMonth(1);
		ArrayList<ArrayList<LocalDate>> matrix = createCalender(thisMonthFirstDay);

		final ArrayList<LocalDate> matrixFirstRow = matrix.get(0);
		final LocalDate matrixFirst = matrixFirstRow.get(0);
		final ArrayList<LocalDate> matrixLastRow = matrix.get(matrix.size() - 1);
		final LocalDate matrixLast = matrixLastRow.get(matrixLastRow.size() - 1);

		List<Tasks> _tasks = hasAdmin(user.getAuthorities())
				? tasksRepository.findByDateBetween(matrixFirst, matrixLast) 
				: tasksRepository.findByDateBetweenAndName(matrixFirst, matrixLast, user.getName());

		HashMap<LocalDate, List<Tasks>> tasks = new HashMap<LocalDate, List<Tasks>>();

		_tasks.forEach(task -> {
			LocalDate key = LocalDate.from(task.getDate());
			List<Tasks> value = tasks.containsKey(key) ? tasks.get(key) : new ArrayList<Tasks>();
			value.add(task);
			tasks.put(key, value);
		});

		model.addAttribute("prev", thisMonthFirstDay.plusMonths(-1));
		model.addAttribute("month", thisMonthFirstDay.getYear() + "年" + thisMonthFirstDay.getMonthValue() + "月");
		model.addAttribute("next", thisMonthFirstDay.plusMonths(1));
		model.addAttribute("matrix", matrix);
		model.addAttribute("tasks", tasks);

		return "main";
	}

	@GetMapping("/main/create/{date}")
	String MainCreatePage(@Validated TaskForm taskForm, BindingResult bindingResult, Model model) {

		if (bindingResult.hasErrors()) {
			return "redirect:/main/create/" + LocalDate.now();
		}

		LocalDate createDate = parseLocalDate(taskForm.getDate());
		model.addAttribute("date", createDate);
		return "create";
	}

	@GetMapping("/main/edit/{id}")
	String MainEditPage(@Validated TaskForm taskForm, BindingResult bindingResult, Model model) {

		if (bindingResult.hasErrors()) {
			return "redirect:/main";
		}

		Tasks task = tasksRepository.getReferenceById(taskForm.getId());

		model.addAttribute("task", task);

		return "edit";
	}

	@PostMapping("/main/create")
	String CreateTask(@Validated TaskForm taskForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user) {

		if (bindingResult.hasErrors()) {
			return "redirect:/main/create/" + LocalDate.now();
		}

		Tasks task = new Tasks();
		task.setTitle(taskForm.getTitle());
		task.setName(user.getName());
		task.setText(taskForm.getText());
		task.setDate(LocalDateTime.parse(taskForm.getDate() + "T00:00:00.00"));
		task.setDone(false);

		tasksRepository.save(task);

		return "redirect:/main";
	}

	@PostMapping("/main/edit/{id}")
	String EditTask(@Validated TaskForm taskForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user) {

		if (bindingResult.hasErrors()) {
			return "redirect:/main";
		}

		Tasks task = new Tasks();
		task.setId(taskForm.getId());
		task.setTitle(taskForm.getTitle());
		task.setName(user.getName());
		task.setText(taskForm.getText());
		task.setDate(LocalDateTime.parse(taskForm.getDate() + "T00:00:00.00"));
		task.setDone(taskForm.getDone());

		tasksRepository.save(task);

		return "redirect:/main";
	}

	@PostMapping("/main/delete/{id}")
	String DeleteTask(@Validated TaskForm taskForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return "redirect:/main";
		}

		tasksRepository.deleteById(taskForm.getId());
		
		return "redirect:/main";
	}
	
	static boolean hasAdmin(Collection roles) {
		boolean result = false;
		for(Object role : roles) {
			if (String.valueOf(role).equals("ROLE_ADMIN") ) result = true; 
		}
		return result;
	}

	static LocalDate parseLocalDate(String date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(date, dateTimeFormatter);
	}

	static ArrayList<ArrayList<LocalDate>> createCalender(LocalDate thisMonthFirstDay) {

		final int oneWeek = 7;
		ArrayList<ArrayList<LocalDate>> matrix = new ArrayList<ArrayList<LocalDate>>();

		LocalDate aDay = thisMonthFirstDay;
		int dayCount = 0;

		while (!aDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).equals("日")) {
			aDay = aDay.plusDays(-1);
			dayCount--;
		}
		// LocalDate.from(thisMonthFirstDay)
		while (dayCount < thisMonthFirstDay.lengthOfMonth()) {
			ArrayList<LocalDate> week = new ArrayList<LocalDate>();

			int countOneWeek = 0;
			while (oneWeek != countOneWeek) {
				week.add(aDay);
				aDay = aDay.plusDays(1);
				countOneWeek++;
				dayCount++;
			}

			matrix.add(week);
		}

		return matrix;

	}

}
