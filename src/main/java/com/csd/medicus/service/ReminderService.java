package com.csd.medicus.service;

import java.util.List;

public interface ReminderService {
	List<String> sendRemindersNext24h();
}
