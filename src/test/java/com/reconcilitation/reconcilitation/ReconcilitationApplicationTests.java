package com.reconcilitation.reconcilitation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.reconcilitation.reconcilitation.controllers.ScheduleController;
import com.reconcilitation.reconcilitation.model.interfaces.IScheduleReader;

@SpringBootTest
class ReconcilitationApplicationTests {

	@Autowired
	private ScheduleController controller;

	@Autowired
	private IScheduleReader reader;

	@Test
	void contextLoads() {
		assertNotNull(controller);
		assertNotNull(reader);
	}

}
